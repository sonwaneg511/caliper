package com.caliper.utils.gemini.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.entity.Client;
import com.caliper.location.service.ClientService;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.caliper.utils.gemini.dto.request.GeminiRequest;
import com.caliper.utils.gemini.dto.request.GeminiRequest.Contents;
import com.caliper.utils.gemini.dto.request.GeminiRequest.Part;
import com.caliper.utils.gemini.dto.response.AIAdCopyResponse;
import com.caliper.utils.gemini.dto.response.GeminiResponse;
import com.caliper.utils.gemini.entity.AIAdCopy;
import com.caliper.utils.gemini.entity.GeminiAPICred;
import com.caliper.utils.gemini.repository.AIAdCopyRepository;
import com.caliper.utils.gemini.repository.GeminiAPICredRepository;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class GeminiAIService {

	@Autowired
	public GeminiAPICredRepository geminiAPICredRepository;
	
	@Autowired
	public ClientService clientService;
	
	@Autowired
	public AIAdCopyRepository aiAdCopyRepository;
	
	public GeminiAPICred getGeminiApiKey() {
		return geminiAPICredRepository.getGeminiAICred();
	}
	
	public GeminiResponse getGeminiResponse(String userInput) throws InterruptedException {
		Gson gson = new Gson();
	    String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + getGeminiApiKey().getApiKey();

	    OkHttpClient client = new OkHttpClient().newBuilder()
	        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
	        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
	        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
	        .build();
	    
	    GeminiRequest request = new GeminiRequest();
	    List<Contents> contents = new ArrayList<>();
	    Contents content = new Contents();
	    List<Part> parts = new ArrayList<>();
	    Part part = new Part();
	    part.setText(userInput);
	    parts.add(part);
	    content.setParts(parts);
	    contents.add(content);
	    request.setContents(contents);
		
	    String json = gson.toJson(request);
	    RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
	    Request request1 = new Request.Builder().url(GEMINI_URL).post(body).build();
	    
	    int maxRetries = 3;
	    int retryCount = 0;
	    int delayMillis = 2000;
	    
	    while(retryCount < maxRetries) {

	        try (Response response = client.newCall(request1).execute()) {
	            String responseBody = response.body() != null ? response.body().string() : "No response from Gemini";

	            if (response.isSuccessful()) {
	                return gson.fromJson(responseBody, GeminiResponse.class);
	            } else {
	                if (response.code() == 503) {
	                    System.out.println("Gemini API is overloaded (503). Retrying in " + delayMillis + " ms...");
	                    Thread.sleep(delayMillis);
	                    retryCount++;
	                    delayMillis *= 2; // exponential backoff
	                    continue;
	                } else {
	                    System.out.println("Request failed: " + response.code() + " - " + responseBody);
	                    return null;
	                }
	            }
	        } catch (IOException | InterruptedException e) {
	            e.printStackTrace();
	            retryCount++;
	            Thread.sleep(delayMillis);
	            delayMillis *= 2;
	        }
	    
	    }
	    System.out.println("Max retries reached. Gemini API is still unavailable.");
	    return null;
	}
	
	public void handleGeminiResponse(String clientId, String clientName, String rawEscapedText) {
		List<Client> allClients = clientService.getAllClients();
		Map<String, Long> caliperClientIdMap = allClients.stream().collect(Collectors.toMap(e->e.getClientName(), e->e.getId()));

		Long matchedClientId = caliperClientIdMap.get(clientName);
		if (matchedClientId == null) {
			System.out.println("No client found with name '" + clientName + "' — saving AI ad copy with idOfClient=0");
		}
		long idOfClient = matchedClientId != null ? matchedClientId : 0L;

		// Convert escaped \n into real newlines
	    String decodedText = rawEscapedText.replaceAll("\\\\n", "\n");

	    // Normalize spacing — remove multiple newlines
	    String normalized = decodedText.replaceAll("\n{2,}", "\n");

	    // Extract sections
	    // NOTE: keyword for plain headlines is "100 headlines" (not just "headlines") so it
	    // doesn't also match the "100 Long Headlines" section header below.
	    List<String> headlines = extractItems(normalized, "100 headlines");
	    List<String> longHeadlines = extractItems(normalized, "long headlines");
	    List<String> descriptions = extractItems(normalized, "descriptions");

	    // New: Trim prefixes from headlines, long headlines and descriptions
	    List<String> cleanedHeadlines = headlines.stream()
	        .map(h -> h.replaceFirst("\\*\\*Headline:\\*\\*", "").trim())
	        .collect(Collectors.toList());

	    List<String> cleanedLongHeadlines = longHeadlines.stream()
	        .map(h -> h.replaceFirst("\\*\\*Long Headline:\\*\\*", "").trim())
	        .collect(Collectors.toList());

	    List<String> cleanedDescriptions = descriptions.stream()
	        .map(d -> d.replaceFirst("\\*\\*Description:\\*\\*", "").trim())
	        .collect(Collectors.toList());

	    // Output
	    System.out.println("== HEADLINES (" + cleanedHeadlines.size() + ") ==");
	    cleanedHeadlines.forEach(System.out::println);

	    System.out.println("\n== LONG HEADLINES (" + cleanedLongHeadlines.size() + ") ==");
	    cleanedLongHeadlines.forEach(System.out::println);

	    System.out.println("\n== DESCRIPTIONS (" + cleanedDescriptions.size() + ") ==");
	    cleanedDescriptions.forEach(System.out::println);

	    for(String headine : cleanedHeadlines) {
	    	AIAdCopy aiAdCopy = AIAdCopy.builder()
	    			.idOfClient(idOfClient)
	    			.clientId(clientId)
	    			.type(AIAdCopy.HEADLINE)
	    			.headDescValue(headine).build();

	    	aiAdCopyRepository.save(aiAdCopy);
	    }

	    for(String longHeadline : cleanedLongHeadlines) {
	    	AIAdCopy aiAdCopy = AIAdCopy.builder()
	    			.idOfClient(idOfClient)
	    			.clientId(clientId)
	    			.type(AIAdCopy.LONG_HEADLINE)
	    			.headDescValue(longHeadline).build();

	    	aiAdCopyRepository.save(aiAdCopy);
	    }

	    for(String description : cleanedDescriptions) {
	    	AIAdCopy aiAdCopy = AIAdCopy.builder()
	    			.idOfClient(idOfClient)
	    			.clientId(clientId)
	    			.type(AIAdCopy.DESCRIPTION)
	    			.headDescValue(description).build();

	    	aiAdCopyRepository.save(aiAdCopy);
	    }
	}

	public AIAdCopyResponse getAdCopyByClientId(String clientId) {

		List<AIAdCopy> adCopies = aiAdCopyRepository.findByClientId(clientId);

		Map<String, List<String>> groupedByType = adCopies.stream()
				.collect(Collectors.groupingBy(
						AIAdCopy::getType,
						Collectors.mapping(AIAdCopy::getHeadDescValue, Collectors.toList())
				));

		return AIAdCopyResponse.builder()
				.clientId(clientId)
				.headlines(groupedByType.getOrDefault(AIAdCopy.HEADLINE, new ArrayList<>()))
				.longHeadlines(groupedByType.getOrDefault(AIAdCopy.LONG_HEADLINE, new ArrayList<>()))
				.descriptions(groupedByType.getOrDefault(AIAdCopy.DESCRIPTION, new ArrayList<>()))
				.build();
	}

	private static List<String> extractItems(String text, String sectionKeyword) {
	    List<String> items = new ArrayList<>();
	    String[] lines = text.split("\\r?\\n"); // Handles both \n and \r\n line endings

	    boolean inSection = false;

	    for (String line : lines) {
	        line = line.trim();

	        // Start collecting if line starts with "##" AND contains the section keyword
	        if (!inSection && line.toLowerCase().startsWith("##") && line.toLowerCase().contains(sectionKeyword.toLowerCase())) {
	            inSection = true;
	            continue;
	        }

	        //Stop collecting if another section header appears
	        if (inSection && line.startsWith("##")) {
	            break;
	        }

	        // Collect lines like "1. something"
	        if (inSection && line.matches("^\\d+\\.\\s+.+")) {
	            String item = line.replaceFirst("^\\d+\\.\\s+", "").trim();
	            items.add(item);
	        }
	    }

	    return items;
	}
}
