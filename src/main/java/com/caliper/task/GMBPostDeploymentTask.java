package com.caliper.task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.entity.Client;
import com.caliper.location.gmb.service.GMBSessionFactory;
import com.caliper.location.service.ClientService;
import com.caliper.post.api.PostAPI;
import com.caliper.post.entity.Platform;
import com.caliper.post.entity.Post;
import com.caliper.post.entity.PostLocationMap;
import com.caliper.post.service.PostService;
import com.google.api.services.mybusiness.v4.MyBusiness;
@Service
public class GMBPostDeploymentTask extends ParameterizedJob{

	@Autowired
	private PostService postService;

	@Autowired
	private PostAPI postAPI;

	@Autowired
	private GMBSessionFactory gmbSessionFactory;

	@Autowired
	private ClientService clientService;

	private boolean processPost;

	private MyBusiness business;

	@Override
	public void run() {
		try {
			init();
			processPost();
		} catch (Exception ex) {
			log("Fatal error in GMBPostDeploymentTask: " + ex.getMessage());
		}
	}

	private void processPost(){
		if(!processPost) {
			return;
		}

		log("Processing GMB Post");

			List<PostLocationMap> allPostLocationMap = postService.getAllPostLocationMapByStatus(Post.STATUS_SUBMIT, String.valueOf(Platform.GMB));
			postService.updatePostLocationMapStatus(Post.STATUS_PROCESSING, Post.STATUS_SUBMIT);
			for (PostLocationMap postLocationMap : allPostLocationMap) {
				Post post = postService.getPostByclientIdAndPostIdAndPlatform(postLocationMap.getClientId(), postLocationMap.getPostId(), postLocationMap.getPlatform());
				if (post == null) {
					log("Post not found for postId=" + postLocationMap.getPostId() + ", clientId=" + postLocationMap.getClientId() + " — skipping");
					postService.updatePostLocationMapconsolePostIdAndStatusByPostId("-1", Post.STATUS_ERROR, postLocationMap.getDealerId(), postLocationMap.getPostId());
					continue;
				}
				try {
					log("Uploading GMB Post for client id - " + post.getClientId()+" and post id :: "+post.getPostId());

					String localPostName = "";

					if (post.getPostType().equalsIgnoreCase(Post.POST_TYPE_OFFER) || post.getPostType().equalsIgnoreCase(Post.POST_TYPE_EVENT)) {
						localPostName = postAPI.createGMBPostUpdated(postLocationMap.getClientId(), business, post, postLocationMap.getDealerId());
					} else if (post.getPostType().equalsIgnoreCase(Post.POST_TYPE_WHATS_NEW)) {
						localPostName = postAPI.createGMBWhatsNewPostUpdated(postLocationMap.getClientId(), business, post, postLocationMap.getDealerId());
					}
					int index = localPostName.lastIndexOf("/");
					String gmbConsolePostId = localPostName.substring(index + 1);
					log("Post Uploaded with Response - " + gmbConsolePostId);
					postService.updatePostLocationMapconsolePostIdAndStatusByPostId(gmbConsolePostId, Post.STATUS_DEPLOYED,
							postLocationMap.getDealerId(), postLocationMap.getPostId());

				} catch (Exception e) {
					log("Error deploying offer - " + post.getPostId() + " : Error Message - " + e.getMessage());
					System.out.println("Error deploying offer - " + post.getPostId() + " : Error Message - " + e.getMessage());
					postService.updatePostLocationMapconsolePostIdAndStatusByPostId("-1", Post.STATUS_ERROR,
							postLocationMap.getDealerId(), postLocationMap.getPostId());
				}
			}
	}

	private void init() throws FileNotFoundException, IOException {
		this.processPost = parameters.getBoolean("process-gmb-post",false);
		this.business = gmbSessionFactory.getGMBSession();

	}

}
