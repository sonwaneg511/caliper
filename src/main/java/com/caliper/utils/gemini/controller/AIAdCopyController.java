package com.caliper.utils.gemini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.utils.gemini.dto.response.AIAdCopyResponse;
import com.caliper.utils.gemini.service.GeminiAIService;

@RestController
@RequestMapping("/ai-ad-copy")
public class AIAdCopyController {

	@Autowired
	private GeminiAIService geminiAIService;

	@GetMapping("/{clientId}")
	public ResponseEntity<AIAdCopyResponse> getAdCopy(@PathVariable String clientId) {
		return ResponseEntity.ok(geminiAIService.getAdCopyByClientId(clientId));
	}

}
