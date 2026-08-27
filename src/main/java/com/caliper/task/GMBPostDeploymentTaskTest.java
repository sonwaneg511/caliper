//package com.caliper.task;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.caliper.job.runtime.ExecutableJob;
//import com.caliper.location.gmb.service.GMBSessionFactory;
//import com.caliper.post.api.PostAPI;
//import com.caliper.post.entity.Platform;
//import com.caliper.post.entity.Post;
//import com.caliper.post.entity.PostLocationMap;
//import com.caliper.post.service.PostService;
//import com.google.api.services.mybusiness.v4.MyBusiness;
//
//public class GMBPostDeploymentTaskTest implements ExecutableJob {
//
//	@Autowired
//	private PostService postService;
//
//	@Autowired
//	private PostAPI postAPI;
//	
//	@Autowired
//	private GMBSessionFactory gmbSessionFactory;
//	
//
//	@Override
//	public void run(Map<String, String> params) throws FileNotFoundException, IOException {
//		MyBusiness business = gmbSessionFactory.getGMBSession();
//		processPost(params, business);
//
//	}
//
//	private void processPost(Map<String, String> params, MyBusiness business) throws FileNotFoundException, IOException {
//		String clientId = params.get("client-id");
//
//		System.out.println("Processing Offers");
//		List<PostLocationMap> allPostLocationMap = postService.getAllPostLocationMapByStatus(clientId,
//				Post.STATUS_SUBMIT, String.valueOf(Platform.GMB));
//		for (PostLocationMap postLocationMap : allPostLocationMap) {
//			Post post = postService.getPostByclientIdAndPostIdAndPlatform(clientId, postLocationMap.getPostId(), postLocationMap.getPlatform());
//			try {
//				System.out.println("Uploading Offer - " + post.getPostId());
//
//				String localPostName = "";
//
//				if (post.getPostType().equalsIgnoreCase(Post.POST_TYPE_OFFER)
//						|| post.getPostType().equalsIgnoreCase(Post.POST_TYPE_EVENT)) {
//
//					localPostName = postAPI.createGMBPostUpdated(clientId, business, post,
//							postLocationMap.getDealerId());
//
//				} else if (post.getPostType().equalsIgnoreCase(Post.POST_TYPE_WHATS_NEW)) {
//
//					localPostName = postAPI.createGMBWhatsNewPostUpdated(clientId, business, post,
//							postLocationMap.getDealerId());
//
//				}
//				int index = localPostName.lastIndexOf("/");
//				String gmbConsolePostId = localPostName.substring(index + 1);
//				System.out.println("Post Uploaded with Response - " + gmbConsolePostId);
//				postService.updatePostLocationMapconsolePostIdAndStatusByPostId(gmbConsolePostId, Post.STATUS_DEPLOYED,
//						postLocationMap.getDealerId(), postLocationMap.getPostId());
//
//			} catch (Exception e) {
//				System.out.println(
//						"Error deploying offer - " + post.getPostId() + " : Error Message - " + e.getMessage());
//				postService.updatePostLocationMapconsolePostIdAndStatusByPostId("-1", Post.STATUS_ERROR,
//						postLocationMap.getDealerId(), postLocationMap.getPostId());
//			}
//		}
//	}
//
//}
