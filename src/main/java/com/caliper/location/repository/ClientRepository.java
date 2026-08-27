package com.caliper.location.repository;

import com.caliper.location.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



import java.util.Optional;


@Repository
public interface ClientRepository extends JpaRepository<Client,Long> {

    Client findByEmail(String email);
    
    @Query("SELECT MAX(c.id) FROM Client c")
    Long findLatestId();
    
    @Query("SELECT c FROM Client c ORDER BY c.id DESC LIMIT 1")
    Client findTopByOrderByIdDesc();
    
    Client findByClientName(String clientName);
    
    Optional<Client> findByClientId(String clientId);

}
