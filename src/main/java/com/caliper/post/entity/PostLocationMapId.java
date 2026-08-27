package com.caliper.post.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostLocationMapId implements Serializable {

	
	private Long postId;
    private String dealerId;
    
    public PostLocationMapId(long postId) {
		this.postId = postId;
	}
}