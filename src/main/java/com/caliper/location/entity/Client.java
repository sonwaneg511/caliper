package com.caliper.location.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "client", uniqueConstraints = {@UniqueConstraint(columnNames = {"client_name"})})
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="client_id")
    private String clientId;

    @Column(name = "client_name")
    private String clientName;

    private String email;
    
    @Column(name="country_code")
    private String countryCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "client_added_date")
    private Date clientAddedDate;
    
    @Column(name = "client_code")
    private String clientCode;
}
