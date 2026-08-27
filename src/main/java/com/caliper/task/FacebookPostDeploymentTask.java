package com.caliper.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.post.api.PostAPI;
import com.caliper.post.entity.Platform;
import com.caliper.post.entity.Post;
import com.caliper.post.entity.PostLocationMap;
import com.caliper.post.service.PostService;

@Service
public class FacebookPostDeploymentTask extends ParameterizedJob {

	@Autowired
	private PostService postService;

	@Autowired
	private PostAPI postAPI;

	private boolean processPost;


	@Override
	public void run() {
		init();
		processPosts();

	}

	private void processPosts() {
		if(!processPost) {
			return;
		}
		log("Processing Facebook Posts");
		List<PostLocationMap> allPostLocationMap = postService.getAllPostLocationMapByStatus(Post.STATUS_SUBMIT, String.valueOf(Platform.FACEBOOK));
		postService.updatePostLocationMapStatus(Post.STATUS_PROCESSING, Post.STATUS_SUBMIT);
		for (PostLocationMap postLocationMap : allPostLocationMap) {
			Post post = postService.getPostByclientIdAndPostIdAndPlatform(postLocationMap.getClientId(), postLocationMap.getPostId(), postLocationMap.getPlatform());
			try {

				String facebookConsolePostId = "";

				log("Uploading Facebook Post for client id - " + post.getClientId()+" and post id :: "+post.getPostId());
				String postType = post.getPostType();

				if (postType.equalsIgnoreCase(Post.POST_TYPE_LINK)) {

					facebookConsolePostId = postAPI.uploadFacebookLinkPost(postLocationMap.getClientId(), post,
							postLocationMap.getDealerId());

				} else if (post.getPostType().equalsIgnoreCase(Post.POST_TYPE_PHOTO)) {

					facebookConsolePostId = postAPI.uploadFacebookMediaPost(postLocationMap.getClientId(), post,
							postLocationMap.getDealerId());

				} else if (post.getPostType().equalsIgnoreCase(Post.POST_TYPE_TEXT)) {

					facebookConsolePostId = postAPI.uploadFacebookTextPost(postLocationMap.getClientId(), post,
							postLocationMap.getDealerId());
				} else if (post.getPostType().equalsIgnoreCase(Post.POST_TYPE_CAROUSEL)) {
					facebookConsolePostId = postAPI.uploadFacebookCarouselPost(postLocationMap.getClientId(), post,
							postLocationMap.getDealerId());
				}

				log("Post Uploaded with Response - " + facebookConsolePostId);
				postService.updatePostLocationMapconsolePostIdAndStatusByPostId(facebookConsolePostId,
						Post.STATUS_DEPLOYED, postLocationMap.getDealerId(), post.getPostId());

			} catch (Exception e) {
				System.out
				.println("Error deploying post - " + post.getPostId() + " : Error Message - " + e.getMessage());
				log(e.getMessage());
				postService.updatePostLocationMapconsolePostIdAndStatusByPostId("-1", Post.STATUS_ERROR,
						postLocationMap.getDealerId(), post.getPostId());
			}
		}
	}

	private void init() {
		processPost = parameters.getBoolean("process-facebook-post");

	}
}
