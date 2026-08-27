package com.caliper.images.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caliper.images.entity.LocationImage;
import com.caliper.location.facebook.entity.FacebookLocation;
import com.caliper.location.facebook.service.FacebookLocationService;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonObject;
import com.restfb.types.Photo;

@Component
public class FacebookImageAPI {

    @Autowired
    private FacebookLocationService facebookLocationService;

    /**
     * Uploads an image to a Facebook Page using the Graph API.
     * The image must already be publicly accessible (e.g. hosted on GCS).
     *
     * Supported categories:
     *   PROFILE  — sets the page profile picture (type=profile)
     *   COVER    — uploads photo and sets it as the page cover
     *   Others   — publishes photo to the page timeline
     *
     * @return the Facebook photo ID
     */
    public String uploadImageToFacebook(String clientId, LocationImage image, String dealerId) {
        FacebookLocation fbLocation = facebookLocationService.getFacebookLocationByClientIdAndDealerId(clientId, dealerId);
        if (fbLocation == null) {
            throw new RuntimeException("Facebook location not found for clientId=" + clientId + " dealerId=" + dealerId);
        }

        String accessToken = fbLocation.getAccessToken();
        String pageId = fbLocation.getFacebookPageId();

        System.out.println("FacebookImageAPI :: uploadImageToFacebook :: clientId=" + clientId
                + " dealerId=" + dealerId + " pageId=" + pageId
                + " imageId=" + image.getImageId() + " category=" + image.getImageCategory());

        FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.LATEST);
        String category = image.getImageCategory();
        String imageUrl = image.getImageUrl();
        String caption = image.getDescription() != null ? image.getDescription() : "";

        String photoId;
        if (LocationImage.CATEGORY_PROFILE.equalsIgnoreCase(category)) {
            photoId = uploadProfilePhoto(fbClient, pageId, imageUrl);
        } else if (LocationImage.CATEGORY_COVER.equalsIgnoreCase(category)) {
            photoId = uploadCoverPhoto(fbClient, pageId, imageUrl);
        } else {
            photoId = uploadTimelinePhoto(fbClient, pageId, imageUrl, caption);
        }

        System.out.println("FacebookImageAPI :: uploadImageToFacebook :: photoId=" + photoId);
        return photoId;
    }

    private String uploadProfilePhoto(FacebookClient fbClient, String pageId, String imageUrl) {
        Photo photo = fbClient.publish(pageId + "/photos", Photo.class,
                Parameter.with("url", imageUrl),
                Parameter.with("type", "profile"));
        return photo.getId();
    }

    private String uploadCoverPhoto(FacebookClient fbClient, String pageId, String imageUrl) {
        // Step 1: Upload photo as unpublished to get its ID
        Photo photo = fbClient.publish(pageId + "/photos", Photo.class,
                Parameter.with("url", imageUrl),
                Parameter.with("published", false));
        String photoId = photo.getId();

        // Step 2: Set the uploaded photo as the page cover
        fbClient.publish(pageId, JsonObject.class,
                Parameter.with("cover", photoId),
                Parameter.with("no_feed_story", true));

        return photoId;
    }

    private String uploadTimelinePhoto(FacebookClient fbClient, String pageId, String imageUrl, String caption) {
        Photo photo = fbClient.publish(pageId + "/photos", Photo.class,
                Parameter.with("url", imageUrl),
                Parameter.with("caption", caption));
        return photo.getId();
    }
}
