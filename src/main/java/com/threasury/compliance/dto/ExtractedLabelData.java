package com.threasury.compliance.dto;

/**
 * Structured output mapping for Spring AI vision extraction responses.
 */
public record ExtractedLabelData(
        String brandName,
        String alcoholPercentage,
        String productType,
        String fullLabelText
) {}
