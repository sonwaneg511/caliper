package com.caliper.post.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.caliper.location.dto.request.LocationFilterRequest;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.service.DealerLocationService;
import com.caliper.post.Specification.PostSpecification;
import com.caliper.post.Specification.PostLocationMapSpecification.PostLocationMapSpecification;
import com.caliper.post.dto.Request.CreatePostRequest;
import com.caliper.post.dto.Request.PostRequest;
import com.caliper.post.dto.Request.PostLocationDetailsRequest;
import com.caliper.post.dto.Response.CaliperResponse;
import com.caliper.post.dto.Response.PostDataPageResponse;
import com.caliper.post.dto.Response.PostDataResponse;
import com.caliper.post.dto.Response.PostGraphResponse;
import com.caliper.post.dto.Response.PostLocationDetailsResponse;
import com.caliper.post.dto.Response.PostViewDetailsResponse;
import com.caliper.post.entity.Platform;
import com.caliper.post.entity.Post;
import com.caliper.post.entity.PostLocationMap;
import com.caliper.post.repository.PostLocationMapRepository;
import com.caliper.post.repository.PostRepository;
import com.caliper.post.validation.FacebookPostValidator;
import com.caliper.post.validation.GmbPostValidator;
import com.caliper.utils.GoogleBucket;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

	private static final Logger log = LoggerFactory.getLogger(PostService.class);

	private static final String GCS_BUCKET_NAME = "caliper-test-bucket";

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PostLocationMapRepository postLocationMapRepository;

	@Autowired
	private DealerLocationService dealerLocationService;

	@Autowired
	private GmbPostValidator gmbPostValidator;

	@Autowired
	private FacebookPostValidator facebookPostValidator;

	@Autowired
	private GoogleBucket googleBucket;

	// Self-injection so @Transactional on persistPost() is honoured when called
	// from within this class (Spring proxy is bypassed otherwise).
	@Autowired
	@Lazy
	private PostService self;

	public List<Post> getAllPost(){
		return postRepository.findAll();
	}

	public void insertAllPost(List<Post> post) {
		postRepository.saveAll(post);
	}

	public Post getPostById (long id) {
		return postRepository.findById(id).orElse(null);
	}

	public Post getPostByclientIdAndPostIdAndPlatform (String clientId, long id, String platform) {
		return postRepository.getPostByclientIdAndPostIdAndPlatform(clientId, id, platform);
	}
	
	public List<Post> getPostByPostIdsAndPlatform(String clientId, Set<Long> postIds){
		return postRepository.getPostByPostIdsAndPlatform(clientId, postIds);
		
	}

	public List<Post> getAllPostByStatus(String status, String platform, Date startDate, Date endDate){
		return postRepository.getAllPostByStatus(status, platform, startDate, endDate);
	}

	public void updateStatusAndCommentById(String status, String comment, long id) {
		postRepository.updateStatusAndCommentById(status, comment, id);
	}

	public void deletePostById(long id) {
		postRepository.deleteById(id);
	}
	
	public List<PostLocationMap> getPostLocationMapByPostIds(List<Long> postIds){
		return postLocationMapRepository.getPostLocationMapByPostIds(postIds);
	}

	public List<PostLocationMap> getPostLocationMapByPostId(long postId, String platform) {
		return postLocationMapRepository.getPostLocationMapByPostIdAndPlatform(postId,platform);
	}

	public List<PostLocationMap> getAllPostLocationMapByStatus(String status, String platform){
		return postLocationMapRepository.getAllPostLocationMapByStatus(status, platform);
	}
	
	public void updatePostLocationMapStatus (String setStatus, String status) {
		postLocationMapRepository.updatePostLocationMapStatus(setStatus, status);
	}

	public List<PostLocationMap> getAllPostLocationMapByClientIdAndPlatform (String clientId, String platform){
		return postLocationMapRepository.getAllPostLocationMapByClientIdAndPlatform(clientId, platform);
	}
	
	public void updatePostLocationMapconsolePostIdAndStatusByPostId (String consolePostId, String status, String dealerId, long postId) {
		postLocationMapRepository.updatePostLocationMapconsolePostIdAndStatusByPostId(consolePostId, status, dealerId, postId);
	}

	public List<PostLocationMap> getPostLocationMapByPostIdsAndPlatform(List<Long> postIds, String platform){
		return postLocationMapRepository.getPostLocationMapByPostIdsAndPlatform(postIds, platform);
	}


	//-------------------------------------------------------------------REQUESTS---------------------------------------------------------------

	public LocationFilterRequest locationFilterRequestCreate(PostRequest req) {

		LocationFilterRequest locationRequest = LocationFilterRequest.builder()
				.clientId(req.getClientId())
				.userId(req.getUserId())
				.state(req.getState())
				.city(req.getCity())
				.dealerId(req.getDealerId())            
				.build();

		return locationRequest;
	}

	public PostLocationDetailsRequest locationFilterRequestViewDetails(PostLocationDetailsRequest req) {

		PostLocationDetailsRequest locationRequest = PostLocationDetailsRequest.builder()
				.clientId(req.getClientId())
				.userId(req.getUserId())
				.postId(req.getPostId())
				.platform(req.getPlatform())          
				.build();

		return locationRequest;
	}


	//--------------------------------------------------------------------POST GRAPH DATA-------------------------------------------------------

	//API which gives data for graph totalPosts, pendingDeployment, deployed.
	public PostGraphResponse getPostGraphData(PostRequest postGraphRequest) {

		LocationFilterRequest locationRequest = locationFilterRequestCreate(postGraphRequest);
		List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
		List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

		List<Post> allPostByClientIdAndSourceAndCreatedDate = postRepository.getAllPostByClientIdAndSourceAndCreatedDate(postGraphRequest.getClientId(), postGraphRequest.getPlatform(), postGraphRequest.getStartDate(), postGraphRequest.getEndDate());
		List<Long> postIds = allPostByClientIdAndSourceAndCreatedDate.stream()
		        .map(Post::getPostId)
		        .collect(Collectors.toList());
		List<PostLocationMap> filterPostLocationMap = postLocationMapRepository
				.findAll(PostLocationMapSpecification.filterPostLocationMapForGarphData(postGraphRequest, dealerIds, postIds));
//		Set<Long> postIds = filterPostLocationMap.stream()
//				.map(PostLocationMap::getPostId)
//				.collect(Collectors.toSet());
//
//		Specification<Post> baseSpec = PostSpecification.filterPost(postGraphRequest, postIds);
		long totalPosts      = filterPostLocationMap.size();
		long deployed = filterPostLocationMap.stream()
		        .filter(postLocationMap -> Post.STATUS_DEPLOYED.equals(postLocationMap.getStatus()))
		        .count();
		long pendingDeployment = filterPostLocationMap.stream()
		        .filter(postLocationMap -> Post.STATUS_SUBMIT.equals(postLocationMap.getStatus()))
		        .count();
//		long pendingDeployment = postRepository.count(
//				filterPostLocationMap.and((root, q, cb) -> cb.equal(root.get("status"), Post.STATUS_SUBMIT)));
//		long deployed        = postRepository.count(
//				filterPostLocationMap.and((root, q, cb) -> cb.equal(root.get("status"), Post.STATUS_DEPLOYED)));

		return PostGraphResponse.builder()
				.totalPosts(totalPosts)
				.pendingPosts(pendingDeployment)
				.deployedPosts(deployed)
				.build();
	}
	
	//filtered post
		public List<Post> getFilteredGraphPost(PostRequest req) {

			LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
			List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
			List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

			List<PostLocationMap> filterPostLocationMap = postLocationMapRepository.findAll(PostLocationMapSpecification.filterPostLocationMap(req, dealerIds));
			Set<Long> postId = filterPostLocationMap.stream().map(PostLocationMap :: getPostId).collect(Collectors.toSet());
			List<Post> postPage = postRepository.findAll(PostSpecification.filterPost(req, postId));
			return postPage;	  
		}
		
		public List<PostLocationMap> getFilteredGraphPostLocationMap(PostRequest req) {

			LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
			List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
			List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

			List<PostLocationMap> filterPostLocationMap = postLocationMapRepository.findAll(PostLocationMapSpecification.filterPostLocationMap(req, dealerIds));
			return filterPostLocationMap;	  
		}

	//----------------------------------------------------------------POST DATA-------------------------------------------------------------------

	//API which gives data to show data of post
		public PostDataPageResponse getPostData(PostRequest request) {

	        Page<Post> filteredPost = getFilteredPost(request);

	        // Safety check
	        if (filteredPost.isEmpty()) {
	            return PostDataPageResponse.builder()
	                    .postDataResponseList(List.of())
	                    .totalNoOfPages(0)
	                    .totalNoOfRecords(0L)
	                    .build();
	        }

	        // Extract postIds
	        Set<Long> postIdSet = filteredPost.getContent()
	                .stream()
	                .map(Post::getPostId)
	                .collect(Collectors.toSet());

	        List<PostLocationMap> allPostLocationMapByClientIdAndPlatform = postLocationMapRepository
	                .getPostLocationMapByPostIdsAndPlatform(new ArrayList<>(postIdSet), String.valueOf(request.getPlatform()));
	        Map<Long, List<PostLocationMap>> postLocationMapByPostId =
	                allPostLocationMapByClientIdAndPlatform.stream()
	                        .collect(Collectors.groupingBy(PostLocationMap::getPostId));

	        // Build response
	        List<PostDataResponse> responseList = filteredPost.getContent()
	                .stream()
	                .map(post -> {

	                    List<PostLocationMap> maps =
	                            postLocationMapByPostId.getOrDefault(post.getPostId(), List.of());

	                    int dealerCount = maps.size();

	                    long likes = maps.stream().mapToLong(PostLocationMap::getLikes).sum();
	                    long comments = maps.stream().mapToLong(PostLocationMap::getComments).sum();
	                    long shares = maps.stream().mapToLong(PostLocationMap::getShares).sum();

	                    return PostDataResponse.builder()
	                            .postId(post.getPostId())
	                            .status(post.getStatus())
	                            .image(post.getImageUrl())
	                            .title(post.getOfferTitle())
	                            .description(post.getSummary())
	                            .likes(likes)
	                            .comments(comments)
	                            .shares(shares)
	                            .date(post.getCreatedDate())
	                            .dealers(dealerCount)
	                            .build();
	                })
	                .toList();

	        return PostDataPageResponse.builder()
	                .postDataResponseList(responseList)
	                .totalNoOfPages(filteredPost.getTotalPages())
	                .totalNoOfRecords(filteredPost.getTotalElements())
	                .build();
	    }


	//filtered post
	public Page<Post> getFilteredPost(PostRequest req) {

		Pageable pageable = PageRequest.of(req.getPageNo(), 10, Sort.by("postId").descending());
		LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
		List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
		List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());

		List<PostLocationMap> filterPostLocationMap = postLocationMapRepository.findAll(PostLocationMapSpecification.filterPostLocationMap(req, dealerIds));
		Set<Long> postId = filterPostLocationMap.stream().map(PostLocationMap :: getPostId).collect(Collectors.toSet());
		Page<Post> postPage = postRepository.findAll(PostSpecification.filterPost(req, postId), pageable);
		return postPage;	  
	}

	//--------------------------------------------------------------POST LOCATION DETAILS--------------------------------------------------

	//API which gives data to show locations details of post
	public PostLocationDetailsResponse getPostLocationDetails(PostRequest req) {
		PostLocationDetailsResponse postLocationDetailsResponse = null;

		LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
		List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
		Map<String, DealerLocation> dealerIdVsLocationMap = locations.stream().collect(Collectors.toMap(l -> l.getDealerId(), l -> l));

		
		List<PostViewDetailsResponse> postViewDetailsResponseList = new ArrayList<PostViewDetailsResponse>();
		Post postByclientIdAndPostId = getPostByclientIdAndPostIdAndPlatform(req.getClientId(), req.getPostId(), req.getPlatform());
		List<PostLocationMap> postLocationMapByPostId = getPostLocationMapByPostId(req.getPostId(), req.getPlatform());

		if (postLocationMapByPostId.size() > 0) {
			for(PostLocationMap postLocationMap : postLocationMapByPostId) {
				String dealerId = postLocationMap.getDealerId();
				DealerLocation dealerLocation = dealerIdVsLocationMap.get(dealerId);
				if(dealerLocation!=null) {
				String name = dealerLocation.getDealerName();
				String area = dealerLocation.getArea();
				String city = dealerLocation.getCity();
				String status = postLocationMap.getStatus();
				long likes = postLocationMap.getLikes();
				long comments = postLocationMap.getComments();
				long shares = postLocationMap.getShares();
				PostViewDetailsResponse postViewDetailsResponse = PostViewDetailsResponse.builder()
						.dealerId(dealerId)
						.locName(name)
						.area(area)
						.city(city)
						.status(status)
						.likes(likes)
						.comments(comments)
						.shares(shares)
						.build();
				postViewDetailsResponseList.add(postViewDetailsResponse);
			}
			}

		}
		
		if(postByclientIdAndPostId!=null) {
		postLocationDetailsResponse = PostLocationDetailsResponse.builder()
				.postId(postByclientIdAndPostId.getPostId())
				.postType(postByclientIdAndPostId.getPostType())
				.offerTitle(postByclientIdAndPostId.getOfferTitle())
				.summary(postByclientIdAndPostId.getSummary())
				.startDate(postByclientIdAndPostId.getStartDate())
				.endDate(postByclientIdAndPostId.getEndDate())
				.imageUrl(postByclientIdAndPostId.getImageUrl())
				.mediaFormat(postByclientIdAndPostId.getMediaFormat())
				.actionType(postByclientIdAndPostId.getActionType())
				.actionUrl(postByclientIdAndPostId.getActionUrl())
				.couponCode(postByclientIdAndPostId.getCouponCode())
				.redeemUrl(postByclientIdAndPostId.getRedeemUrl())
				.termsConditions(postByclientIdAndPostId.getTermsConditions())
				.createdBy(postByclientIdAndPostId.getCreatedBy())
				.createdDate(postByclientIdAndPostId.getCreatedDate())
				.comment(postByclientIdAndPostId.getComment())
				.postViewDetailsResponseList(postViewDetailsResponseList)
				.build(); 
		}else {
			throw new ResourceNotFoundException("Post Not Found for Given Post Id : "+req.getPostId()+" and platform : "+req.getPlatform());
		}
				
		return postLocationDetailsResponse;
	}


	//-----------------------------------------------------------CREATE POST------------------------------------------------------------------

	private void validatePlatform(CreatePostRequest req) {
		if (Platform.GMB.name().equalsIgnoreCase(req.getPlatform())) {
			gmbPostValidator.validate(req);
		} else if (Platform.FACEBOOK.name().equalsIgnoreCase(req.getPlatform())) {
			facebookPostValidator.validate(req);
		}
	}

	// NOT @Transactional — GCS upload is external I/O, must not hold a DB connection.
	public CaliperResponse createPost(CreatePostRequest req, List<MultipartFile> imageFiles) throws IOException {
		if (imageFiles != null && !imageFiles.isEmpty()) {
			// Files were attached — upload each to GCS independently (a failure on one
			// doesn't block the others) and use the resulting URL(s) instead of
			// whatever (if anything) was in data.image_url.
			List<String> uploadedUrls = new ArrayList<>();
			for (MultipartFile imageFile : imageFiles) {
				if (imageFile == null || imageFile.isEmpty()) {
					continue;
				}
				File tempFile = null;
				try {
					tempFile = googleBucket.convertMultiPartFile(imageFile);
					String gcsUrl = googleBucket.uploadToBucket(GCS_BUCKET_NAME, tempFile);
					if (gcsUrl != null) {
						uploadedUrls.add(gcsUrl);
					} else {
						log.error("GCS upload returned null url for client {} file {}",
								req.getClientId(), imageFile.getOriginalFilename());
					}
				} catch (Exception e) {
					log.error("Upload failed for client {} file {}: {}",
							req.getClientId(), imageFile.getOriginalFilename(), e.getMessage(), e);
				} finally {
					if (tempFile != null && tempFile.exists()) {
						tempFile.delete();
					}
				}
			}
			if (uploadedUrls.isEmpty()) {
				return new CaliperResponse(req.getClientId(), "Failed", "Failed to upload image(s). Please try again.");
			}
			req.setImageURL(String.join(",", uploadedUrls));
		}
		// else: no files attached — req.getImageURL() is used exactly as sent by the
		// UI (already-hosted URL), unchanged from today's behavior.

		// GmbPostValidator/FacebookPostValidator both require a non-blank imageURL for
		// post types that need one, so validation must run after the block above,
		// once image_url is finally resolved either way.
		validatePlatform(req);
		return self.persistPost(req);
	}

	// Public so the Spring CGLIB proxy applies @Transactional. Internal use only.
	@Transactional
	public CaliperResponse persistPost(CreatePostRequest req) {
		Post post = Post.builder()
				.clientId(req.getClientId())
				.platform(Platform.valueOf(req.getPlatform()))
				.postType(req.getPostType())
				.offerTitle(req.getOfferTitle())
				.summary(req.getSummary())
				.startDate(req.getStartDate())
				.endDate(req.getEndDate())
				.imageUrl(req.getImageURL())
				.mediaFormat(req.getMediaFormat())
				.actionType(req.getActionType())
				.actionUrl(req.getActionURL())
				.couponCode(req.getCouponCode())
				.redeemUrl(req.getRedeemURL())
				.termsConditions(req.getTermsConditions())
				.createdBy(req.getCreatedBy())
				.comment(req.getComment())
				.status(req.getStatus())
				.createdDate(req.getCreatedDate())
				.build();

		Post savedPost = postRepository.save(post);

		Date now = new Date();
		List<PostLocationMap> locationMaps = req.getDealerId().stream()
				.map(dealer -> PostLocationMap.builder()
						.postId(savedPost.getPostId())
						.dealerId(dealer)
						.clientId(req.getClientId())
						.platform(req.getPlatform())
						.views(0L)
						.likes(0L)
						.shares(0L)
						.comments(0L)
						.status(req.getStatus())
						.createdDate(now)
						.consolePostId("-1")
						.build())
				.collect(Collectors.toList());

		postLocationMapRepository.saveAll(locationMaps);

		return new CaliperResponse(req.clientId, "Successful", "Post Created");
	}

	public List<PostLocationMap> findByClientIdAndDealerIdInAndCreatedDateBetween(String clientId,
			Set<String> mappedDealers, Date fromDate, Date toDate) {
		// TODO Auto-generated method stub
		return postLocationMapRepository.findByClientIdAndDealerIdInAndCreatedDateBetween(clientId, mappedDealers, fromDate, toDate);
	}

	//--------------------------------------------------------------------TESTING CODE MAYBE REQUIRED----------------------------------------------------

	//Filtered data from Post table
	//	public List<Post> getFilterData(PostGraphRequest postGraphRequest) {
	//		return postRepository.findAll(PostSpecification.filterPost(postGraphRequest.getClientId(), postGraphRequest.getStartDate(), postGraphRequest.getEndDate(), 
	//				postGraphRequest.getState(), postGraphRequest.getCity(), postGraphRequest.getDealerId(), postGraphRequest.getPlatform(), 
	//				postGraphRequest.getDeploymentStatus()));
	//	}


	//	//API which gives data to show locations details of post
	//		public PostLocationDetailsPageResponse getPostLocationDetails(PostGraphRequest req) {
	//
	//
	//			Page<PostLocationMap> filteredPost = getFilteredPostLocationDetails(req);
	//			PostViewDetailsResponse postViewDetailsResponse = null;
	//			List<PostViewDetailsResponse> postViewDetailsResponseList = new ArrayList<PostViewDetailsResponse>();
	//			//List<PostLocationMap> postLocationMapByPostId = getPostLocationMapByPostId(req.getPostId(), req.getPlatform());
	//			if (!filteredPost.isEmpty()) {
	//				for(PostLocationMap postLocationMap : filteredPost) {
	//					String dealerId = postLocationMap.getDealerId();
	//					DealerLocation dealerLocation = dealerLocationService.getDealerLocationByDealerId(dealerId);
	//					String name = dealerLocation.getDealerName();
	//					postViewDetailsResponse = new PostViewDetailsResponse(dealerId, name);
	//					postViewDetailsResponseList.add(postViewDetailsResponse);
	//				}
	//
	//			}
	//			PostLocationDetailsPageResponse postPageResponse = new PostLocationDetailsPageResponse();
	//			postPageResponse.setPostViewDetailsResponseList(postViewDetailsResponseList);
	//			postPageResponse.setTotalNoOfPages(filteredPost.getTotalPages());
	//			postPageResponse.setTotalNoOfRecords(filteredPost.getTotalElements());
	//			System.out.println("postViewDetailsResponseList size :: "+postViewDetailsResponseList.size());
	//			return postPageResponse;
	//		}
	//
	//		private Page<PostLocationMap> getFilteredPostLocationDetails(PostGraphRequest req) {
	//			Pageable pageable = PageRequest.of(req.getPageNo(), 10, Sort.by("postId").descending());
	//			LocationFilterRequest locationRequest = locationFilterRequestCreate(req);
	//			List<DealerLocation> locations = dealerLocationService.getFilteredDealerLocation(locationRequest);
	//			List<String> dealerIds = locations.stream().map(DealerLocation::getDealerId).collect(Collectors.toList());
	//
	//			Page<PostLocationMap> filterPostLocationMap = postLocationMapRepository.findAll(PostLocationMapSpecification.filterPostLocationMap(req, dealerIds), pageable);
	//			return filterPostLocationMap;	
	//		}

//	public PostDataPageResponse getPostData(PostRequest postGraphRequest) {
//
//	    Page<Post> filteredPost = getFilteredPost(postGraphRequest);
//
//	    List<PostDataResponse> postDataResponseList =
//	            filteredPost.getContent()
//	                    .stream()
//	                    .map(post -> {
//
//	                        List<PostLocationMap> locationMaps = getPostLocationMapByPostId(post.getPostId(),String.valueOf(post.getPlatform()));
//	                        int dealerCount = locationMaps != null ? locationMaps.size() : 0;
//
//	                        return PostDataResponse.builder()
//	                                .postId(post.getPostId())
//	                                .status(post.getStatus())
//	                                .image(post.getImageUrl())
//	                                .title(post.getOfferTitle())
//	                                .label(post.getLabel())
//	                                .description(post.getSummary())
//	                                .label(post.getLabel())
//	                                .likes(0)
//	                                .comments(0)
//	                                .shares(0)
//	                                .date(post.getCreatedDate())
//	                                .dealers(dealerCount)
//	                                .build();
//	                    })
//	                    .toList();
//
//	    PostDataPageResponse postPageResponse = new PostDataPageResponse();
//	    postPageResponse.setPostDataResponseList(postDataResponseList);
//	    postPageResponse.setTotalNoOfPages(filteredPost.getTotalPages());
//	    postPageResponse.setTotalNoOfRecords(filteredPost.getTotalElements());
//
//	    return postPageResponse;
//	}

}
