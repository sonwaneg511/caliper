package com.caliper.post.controller;

import java.io.IOException;
import java.util.List;

import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.caliper.post.dto.Request.CreatePostRequest;
import com.caliper.post.dto.Request.PostRequest;
import com.caliper.post.dto.Response.CaliperResponse;

import com.caliper.post.dto.Response.PostDataPageResponse;
import com.caliper.post.dto.Response.PostGraphResponse;
import com.caliper.post.dto.Response.PostLocationDetailsResponse;
import com.caliper.post.service.PostService;


@RestController
@RequestMapping("/post")
public class PostController {

	@Autowired
	public PostService postService;

	@PostMapping("/post-graph")
	public ResponseEntity<PostGraphResponse> postGraphRequest(@RequestBody PostRequest postGraphRequest) {
		PostGraphResponse postGraphResponse = postService.getPostGraphData(postGraphRequest);
		return ResponseEntity.ok(postGraphResponse);
	}

	@PostMapping("/post-data")
	public ResponseEntity<PostDataPageResponse> postData(@RequestBody PostRequest postGraphRequest) {
		PostDataPageResponse postData = postService.getPostData(postGraphRequest);
		return ResponseEntity.ok(postData);
	}

	@PostMapping("/location-details")
	public ResponseEntity<PostLocationDetailsResponse> locationDeatils(@RequestBody PostRequest postGraphRequest) {
		PostLocationDetailsResponse postData = postService.getPostLocationDetails(postGraphRequest);
		return ResponseEntity.ok(postData);
	}


	@PostMapping(value = "/create-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CaliperResponse> createPost(@RequestPart("data") CreatePostRequest createPostRequest,
			@RequestPart(value = "file", required = false) List<MultipartFile> imageFiles) throws IOException {
		Log.info("createPostRequest :: "+createPostRequest);
		CaliperResponse post = postService.createPost(createPostRequest, imageFiles);
		return ResponseEntity.ok(post);
	}

}