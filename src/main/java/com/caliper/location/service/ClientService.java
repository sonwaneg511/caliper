package com.caliper.location.service;

import com.caliper.location.entity.Client;
import com.caliper.location.repository.ClientRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client findByEmail(String email) {
        Client clientByEmail = clientRepository.findByEmail(email);
        return clientByEmail;
    }

    public void insertClient(Client client) {
        clientRepository.save(client);
    }

    public Optional<Client> findClientById(long id) {
    	return clientRepository.findById(id);
    }

    public List<Client> getAllClients(){
    	return clientRepository.findAll();
    }
    
    public Client getByClientName(String clientName){
    	return clientRepository.findByClientName(clientName);
    }
    
    public List<Client> findAllClient(){
    	return clientRepository.findAll();
    }

    public Client findByClientId(String clientId){
    	return clientRepository.findByClientId(clientId)
    			.orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }
}
