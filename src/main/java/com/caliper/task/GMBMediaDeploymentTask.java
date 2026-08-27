package com.caliper.task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.images.api.GmbImageAPI;
import com.caliper.images.entity.LocationImage;
import com.caliper.images.entity.LocationImageMap;
import com.caliper.images.service.LocationImageService;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.google.api.services.mybusiness.v4.MyBusiness;

@Service
public class GMBMediaDeploymentTask extends ParameterizedJob {

    @Autowired
    private LocationImageService locationImageService;

    @Autowired
    private GmbImageAPI gmbImageAPI;

    @Autowired
    private GMBSessionFactory gmbSessionFactory;

    private boolean processImage;
    private MyBusiness business;

    @Override
    public void run() {
        try {
            init();
            processImages();
        } catch (Exception ex) {
            ex.printStackTrace();
            log("Fatal error in GMBMediaDeploymentTask: " + ex.getMessage());
        }
    }

    private void init() throws FileNotFoundException, IOException {
        this.processImage = parameters.getBoolean("process-gmb-media", false);
        this.business = gmbSessionFactory.getGMBSession();
    }

    private void processImages() {
        if (!processImage) {
            return;
        }

        log("Processing GMB Images");

        List<LocationImageMap> pendingMaps =
                locationImageService.getAllLocationImageMapByStatusAndPlatform(
                        LocationImage.STATUS_SUBMIT, LocationImage.PLATFORM_GMB);

        if (pendingMaps.isEmpty()) {
            log("No pending GMB images to process");
            return;
        }

        locationImageService.updateLocationImageMapStatusByPlatform(
                LocationImage.STATUS_PROCESSING, LocationImage.STATUS_SUBMIT, LocationImage.PLATFORM_GMB);

        // Batch-fetch all required LocationImage records in one query instead of
        // querying inside the loop (avoids N+1).
        Set<Long> imageIds = pendingMaps.stream()
                .map(LocationImageMap::getImageId)
                .collect(Collectors.toSet());

        Map<Long, LocationImage> imageById = locationImageService.getImageMapByIds(imageIds);

        for (LocationImageMap map : pendingMaps) {
            LocationImage image = imageById.get(map.getImageId());

            if (image == null) {
                log("No LocationImage found for imageId=" + map.getImageId() + ", skipping");
                locationImageService.updateConsoleImageIdAndStatus(
                        "-1", LocationImage.STATUS_ERROR, map.getDealerId(), map.getImageId());
                continue;
            }

            try {
                log("Uploading GMB image for clientId=" + image.getClientId()
                        + " imageId=" + image.getImageId()
                        + " dealerId=" + map.getDealerId());

                String imageName = gmbImageAPI.uploadImageToGMB(
                        map.getClientId(), business, image, map.getDealerId());

                int lastSlash = imageName.lastIndexOf("/");
                String consoleImageId = imageName.substring(lastSlash + 1);

                log("Image uploaded successfully, consoleImageId=" + consoleImageId);

                locationImageService.updateConsoleImageIdAndStatus(
                        consoleImageId, LocationImage.STATUS_DEPLOYED, map.getDealerId(), map.getImageId());

            } catch (Exception e) {
                log("Error uploading image - imageId=" + map.getImageId()
                        + " dealerId=" + map.getDealerId()
                        + " : " + e.getMessage());
                locationImageService.updateConsoleImageIdAndStatus(
                        "-1", LocationImage.STATUS_ERROR, map.getDealerId(), map.getImageId());
            }
        }
    }
}
