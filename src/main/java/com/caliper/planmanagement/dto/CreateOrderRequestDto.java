package com.caliper.planmanagement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequestDto {

    private String clientId;
    private String userId;
    private List<String> serviceKeys;
    private String durationType;
    private long locationCount;
}
