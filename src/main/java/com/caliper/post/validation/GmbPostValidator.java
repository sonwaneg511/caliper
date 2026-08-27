package com.caliper.post.validation;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.caliper.post.dto.Request.CreatePostRequest;
import com.caliper.post.entity.Post;
import com.caliper.utils.exception.customException.InvalidRequestException;

@Component
public class GmbPostValidator {

    private static final Set<String> VALID_GMB_POST_TYPES = Set.of(
            Post.POST_TYPE_EVENT, Post.POST_TYPE_OFFER, Post.POST_TYPE_WHATS_NEW);

    private static final Set<String> VALID_MEDIA_FORMATS = Set.of(
            Post.MEDIA_TYPE_PHOTO, Post.MEDIA_TYPE_VIDEO);

    private static final Set<String> VALID_ACTION_TYPES = Set.of(
            Post.ACTION_TYPE_BOOK, Post.ACTION_TYPE_ORDER, Post.ACTION_TYPE_SHOP,
            Post.ACTION_TYPE_LEARN_MORE, Post.ACTION_TYPE_SIGN_UP,
            Post.ACTION_TYPE_CALL, Post.ACTION_TYPE_UNSPECIFIED);

    public void validate(CreatePostRequest req) {
        validateCommon(req);
        String postType = req.getPostType().toLowerCase();
        if (postType.equals(Post.POST_TYPE_EVENT) || postType.equals(Post.POST_TYPE_OFFER)) {
            validateEventOrOffer(req, postType);
        } else {
            validateWhatsNew(req);
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
        if (!VALID_GMB_POST_TYPES.contains(req.getPostType().toLowerCase()))
            throw new InvalidRequestException(
                    "post_type '" + req.getPostType() + "' is not valid for GMB. Allowed: event, offer, whats_new");
        if (isBlank(req.getCreatedBy()))
            throw new InvalidRequestException("created_by is required");
        if (req.getCreatedDate() == null)
            throw new InvalidRequestException("created_date is required");
        if (isBlank(req.getMediaFormat()))
            throw new InvalidRequestException("media_format is required");
        if (!VALID_MEDIA_FORMATS.contains(req.getMediaFormat().toUpperCase()))
            throw new InvalidRequestException(
                    "media_format '" + req.getMediaFormat() + "' is not valid. Allowed: PHOTO, VIDEO");
        if (isBlank(req.getImageURL()))
            throw new InvalidRequestException("image_url is required");
    }

    private void validateEventOrOffer(CreatePostRequest req, String postType) {
        if (isBlank(req.getOfferTitle()))
            throw new InvalidRequestException("offer_title is required for " + postType + " posts");
        if (req.getOfferTitle().length() > 58)
            throw new InvalidRequestException("offer_title must not exceed 58 characters (Google API limit)");
        if (req.getStartDate() == null)
            throw new InvalidRequestException("start_date is required for " + postType + " posts");
        if (req.getEndDate() == null)
            throw new InvalidRequestException("end_date is required for " + postType + " posts");
        if (!req.getEndDate().after(req.getStartDate()))
            throw new InvalidRequestException("end_date must be after start_date");
        if (req.getSummary() != null && req.getSummary().length() > 1500)
            throw new InvalidRequestException("summary must not exceed 1500 characters");

        if (postType.equals(Post.POST_TYPE_EVENT)) {
            if (isBlank(req.getActionType()))
                throw new InvalidRequestException("action_type is required for event posts");
            if (!VALID_ACTION_TYPES.contains(req.getActionType().toUpperCase()))
                throw new InvalidRequestException(
                        "action_type '" + req.getActionType() + "' is not valid");
            boolean needsUrl = !req.getActionType().equalsIgnoreCase(Post.ACTION_TYPE_CALL)
                    && !req.getActionType().equalsIgnoreCase(Post.ACTION_TYPE_UNSPECIFIED);
            if (needsUrl && isBlank(req.getActionURL()))
                throw new InvalidRequestException(
                        "action_url is required for event posts when action_type is not CALL or ACTION_TYPE_UNSPECIFIED");
        }
    }

    private void validateWhatsNew(CreatePostRequest req) {
        if (isBlank(req.getSummary()))
            throw new InvalidRequestException("summary is required for whats_new posts");
        if (req.getSummary().length() > 1500)
            throw new InvalidRequestException("summary must not exceed 1500 characters");
        if (!isBlank(req.getActionType())
                && !req.getActionType().equalsIgnoreCase(Post.ACTION_TYPE_UNSPECIFIED)
                && !req.getActionType().equalsIgnoreCase(Post.ACTION_TYPE_CALL)
                && isBlank(req.getActionURL()))
            throw new InvalidRequestException(
                    "action_url is required when action_type is set and is not CALL or ACTION_TYPE_UNSPECIFIED");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
