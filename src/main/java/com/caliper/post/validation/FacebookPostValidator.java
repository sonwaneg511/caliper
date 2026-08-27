package com.caliper.post.validation;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.caliper.post.dto.Request.CreatePostRequest;
import com.caliper.post.entity.Post;
import com.caliper.utils.exception.customException.InvalidRequestException;

@Component
public class FacebookPostValidator {

    private static final Set<String> VALID_FACEBOOK_POST_TYPES = Set.of(
            Post.POST_TYPE_LINK.toLowerCase(),
            Post.POST_TYPE_PHOTO.toLowerCase(),
            Post.POST_TYPE_TEXT.toLowerCase(),
            Post.POST_TYPE_CAROUSEL.toLowerCase());

    private static final Set<String> VALID_MEDIA_FORMATS = Set.of(
            Post.MEDIA_TYPE_PHOTO, Post.MEDIA_TYPE_VIDEO);

    public void validate(CreatePostRequest req) {
        validateCommon(req);
        String postType = req.getPostType().toLowerCase();
        switch (postType) {
            case "link"     -> validateLink(req);
            case "photo"    -> validatePhoto(req);
            case "text"     -> validateText(req);
            case "carousel" -> validateCarousel(req);
        }
    }

    private void validateCommon(CreatePostRequest req) {
        if (isBlank(req.getClientId()))
            throw new InvalidRequestException("client_id is required");
        if (isBlank(req.getUserId()))
            throw new InvalidRequestException("user_id is required");
        if (req.getDealerId() == null || req.getDealerId().isEmpty())
            throw new InvalidRequestException("dealer_id must contain at least one dealer");
        if (isBlank(req.getPostType()))
            throw new InvalidRequestException("post_type is required");
        if (isBlank(req.getLabel()))
            throw new InvalidRequestException("label is required");
        if (!VALID_FACEBOOK_POST_TYPES.contains(req.getPostType().toLowerCase()))
            throw new InvalidRequestException(
                    "post_type '" + req.getPostType() + "' is not valid for Facebook. Allowed: Link, Photo, Text, Carousel");
        if (isBlank(req.getCreatedBy()))
            throw new InvalidRequestException("created_by is required");
        if (req.getCreatedDate() == null)
            throw new InvalidRequestException("created_date is required");
    }

    private void validateLink(CreatePostRequest req) {
        if (isBlank(req.getSummary()))
            throw new InvalidRequestException("summary (message) is required for Link posts");
        if (isBlank(req.getActionType()))
            throw new InvalidRequestException("action_type is required for Link posts");
        if (isBlank(req.getActionURL()))
            throw new InvalidRequestException("action_url (link) is required for Link posts");
    }

    private void validatePhoto(CreatePostRequest req) {
        if (isBlank(req.getMediaFormat()))
            throw new InvalidRequestException("media_format is required for Photo posts");
        if (!VALID_MEDIA_FORMATS.contains(req.getMediaFormat().toUpperCase()))
            throw new InvalidRequestException(
                    "media_format '" + req.getMediaFormat() + "' is not valid. Allowed: PHOTO, VIDEO");
        if (isBlank(req.getImageURL()))
            throw new InvalidRequestException("image_url is required for Photo posts");
        if (isBlank(req.getSummary()))
            throw new InvalidRequestException("summary (caption) is required for Photo posts");
    }

    private void validateText(CreatePostRequest req) {
        if (isBlank(req.getSummary()))
            throw new InvalidRequestException("summary (message) is required for Text posts");
    }

    private void validateCarousel(CreatePostRequest req) {
        if (isBlank(req.getImageURL()))
            throw new InvalidRequestException("image_url is required for Carousel posts");
        String[] images = req.getImageURL().split(",");
        long validImageCount = java.util.Arrays.stream(images)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .count();
        if (validImageCount < 2)
            throw new InvalidRequestException("image_url must contain at least 2 comma-separated image URLs for Carousel posts");
        if (isBlank(req.getActionURL()))
            throw new InvalidRequestException("action_url is required for Carousel posts");
        if (isBlank(req.getSummary()))
            throw new InvalidRequestException("summary (caption/message) is required for Carousel posts");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
