package com.threasury.compliance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threasury.compliance.dto.ExpectedFormData;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ExpectedFormDataParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ExpectedFormDataParser(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public ExpectedFormData resolve(
            String formDataJson,
            String brandName,
            String alcoholPercentage,
            String productType
    ) {
        ExpectedFormData formData;
        if (formDataJson != null && !formDataJson.isBlank()) {
            formData = parseJson(formDataJson);
        } else if (hasText(brandName) && hasText(alcoholPercentage) && hasText(productType)) {
            formData = new ExpectedFormData(brandName, alcoholPercentage, productType);
        } else {
            throw new IllegalArgumentException(
                    "Provide a 'formData' JSON part or the fields brandName, alcoholPercentage, and productType.");
        }
        validate(formData);
        return formData;
    }

    private ExpectedFormData parseJson(String formDataJson) {
        try {
            return objectMapper.readValue(formDataJson, ExpectedFormData.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("formData must be valid JSON.", ex);
        }
    }

    private void validate(ExpectedFormData formData) {
        var violations = validator.validate(formData);
        if (!violations.isEmpty()) {
            String detail = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ConstraintViolationException(detail, violations);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
