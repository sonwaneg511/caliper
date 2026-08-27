package com.caliper.location.gmb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caliper.location.gmb.dto.GMBInfoRequest;
import com.caliper.location.gmb.entity.GMBInfoQueue;
import com.caliper.location.gmb.repository.GMBInfoQueueRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GMBInfoQueueService {

	@Autowired
	private GMBInfoQueueRepository gmbInfoQueueRepository;

	@Autowired
	private ObjectMapper objectMapper; 
	
	public List<GMBInfoQueue> getAllGMBInfoQueue(){
		return gmbInfoQueueRepository.findAll();
	}

	public List<GMBInfoQueue> getAllGMBInfoQueueByClientId(String clientId){
		return gmbInfoQueueRepository.getAllGMBInfoQueueByClientId(clientId);
	}
	
	public void updateStatusById(String status, Long id) {
		gmbInfoQueueRepository.updateGMBInfoQueueStatusById(status, id);
	}
	
	
	public GMBInfoQueue saveGMBInfoRequest(String dealerId, GMBInfoRequest gmbInfoRequest) {
        try {
        	
            String json = objectMapper.writeValueAsString(gmbInfoRequest);

            GMBInfoQueue queueEntry = new GMBInfoQueue();
            queueEntry.setDealerId(dealerId);
            queueEntry.setRequestJson(json);
            queueEntry.setStatus(GMBInfoQueue.STATUS_SUBMIT);

            return gmbInfoQueueRepository.save(queueEntry);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize GMBInfoRequest", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save GMBInfoQueue", e);
        }
    }

	public List<GMBInfoQueue> findByStatus(String statusSubmit) {
		// TODO Auto-generated method stub
		return gmbInfoQueueRepository.findByStatus(statusSubmit);
	}
}
