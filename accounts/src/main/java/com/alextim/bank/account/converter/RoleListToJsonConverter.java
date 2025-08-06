package com.alextim.bank.account.converter;

import com.alextim.bank.account.constant.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Converter
@RequiredArgsConstructor
public class RoleListToJsonConverter implements AttributeConverter<List<Role>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    private CollectionType LIST_TYPE = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Role.class);

    @Override
    public String convertToDatabaseColumn(List<Role> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot convert role list to JSON", e);
        }
    }

    @Override
    public List<Role> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || "[]".equals(dbData)) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(dbData, LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot parse role list from JSON", e);
        }
    }
}