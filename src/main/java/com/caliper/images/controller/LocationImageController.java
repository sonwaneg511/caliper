package com.caliper.images.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.caliper.images.dto.request.ImageFilterRequest;
import com.caliper.images.dto.request.UploadImageRequest;
import com.caliper.images.dto.response.ImageDataPageResponse;
import com.caliper.images.entity.LocationImageMap;
import com.caliper.images.service.LocationImageService;
import com.caliper.post.dto.Response.CaliperResponse;
import com.caliper.utils.exception.customException.ImageUploadException;
import com.caliper.utils.exception.customException.InvalidRequestException;

@RestController
@RequestMapping("/location-image")
public class LocationImageController {

	@Autowired
	private LocationImageService locationImageService;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CaliperResponse> uploadImage(@RequestPart("data") UploadImageRequest request,
			@RequestPart("file") List<MultipartFile> imageFiles) {
		try {
			CaliperResponse response = locationImageService.uploadImage(request, imageFiles);
			return ResponseEntity.ok(response);
		} catch (InvalidRequestException | ImageUploadException e) {
			throw e;
		} catch (Exception e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(
					new CaliperResponse(request.getClientId(), "Failed", "Something went wrong. Please try again."));
		}
	}

	@PostMapping("/image-data")
	public ResponseEntity<ImageDataPageResponse> getImageData(@RequestBody ImageFilterRequest request) {
		ImageDataPageResponse response = locationImageService.getImageData(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/current")
	public ResponseEntity<LocationImageMap> getCurrentImage(@RequestParam String dealerId, @RequestParam String category) {
		return locationImageService.getCurrentImage(dealerId, category)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.noContent().build());
	}
}
