package com.caliper.dashboard.dto.post;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("summary")
public class PostsSummary {

	private Long totalPosts;
	private Long unDeployedPosts;
}
