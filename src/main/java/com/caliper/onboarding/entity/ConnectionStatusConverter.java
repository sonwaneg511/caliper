package com.caliper.onboarding.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ConnectionStatusConverter implements AttributeConverter<ConnectionStatus, String> {

    @Override
    public String convertToDatabaseColumn(ConnectionStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public ConnectionStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.trim().isEmpty()) return null;
        return ConnectionStatus.valueOf(dbValue.trim());
    }
}
