package com.caliper.images.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.images.entity.LocationImage;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.service.GMBLocationService;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.api.services.mybusiness.v4.model.LocationAssociation;
import com.google.api.services.mybusiness.v4.model.ListMediaItemsResponse;
import com.google.api.services.mybusiness.v4.model.MediaItem;

@Component
public class GmbImageAPI {

    @Autowired
    private GMBLocationService gmbLocationService;

    /**
     * Uploads an image to the GMB location using the Media API.
     * The image must already be publicly accessible (e.g. hosted on GCS).
     *
     * @return the GMB media resource name, e.g. "accounts/xxx/locations/yyy/media/zzz"
     */
    public String uploadImageToGMB(String clientId, MyBusiness business, LocationImage image, String dealerId)
            throws IOException {

        GMBLocation gmbLoc = gmbLocationService.getAllGmbLocationByClientIdAndDealerId(clientId, dealerId);
        String locName = gmbLoc.getGmbLocationId();

        System.out.println("GmbImageAPI :: uploadImageToGMB :: clientId=" + clientId
                + " dealerId=" + dealerId + " locName=" + locName
                + " imageId=" + image.getImageId() + " category=" + image.getImageCategory());

        MediaItem mediaItem = new MediaItem();
        mediaItem.setSourceUrl(image.getImageUrl());
        mediaItem.setMediaFormat(image.getImageFormat().toUpperCase());

        LocationAssociation locationAssociation = new LocationAssociation();
        locationAssociation.setCategory(image.getImageCategory().toUpperCase());
        mediaItem.setLocationAssociation(locationAssociation);

        if (image.getDescription() != null && !image.getDescription().isBlank()) {
            mediaItem.setDescription(image.getDescription());
        }

        MediaItem result = business.accounts().locations().media()
                .create(locName, mediaItem)
                .execute();

        System.out.println("GmbImageAPI :: uploadImageToGMB :: result name=" + result.getName());
        return result.getName();
    }

    /**
     * Lists media items already present on a GMB location's profile.
     *
     * @param gmbLocationName the GMB location resource name, e.g. "accounts/xxx/locations/yyy"
     */
    public ListMediaItemsResponse listMediaForLocation(MyBusiness business, String gmbLocationName,
            String pageToken, int pageSize) throws IOException {
        return business.accounts().locations().media()
                .list(gmbLocationName)
                .setPageSize(pageSize)
                .setPageToken(pageToken)
                .execute();
    }
}
