package com.caliper.utils.gemini.dto.request;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GeminiRequest {
	 private List<Contents> contents;
	 
	 @AllArgsConstructor
	 @NoArgsConstructor
	 @Data
	 public static class Contents{
		 private List<Part> parts;
	 }
	 
	 @AllArgsConstructor
	 @NoArgsConstructor
	 @Data
	 public static class Part{
		 private String text;
	 }
}
