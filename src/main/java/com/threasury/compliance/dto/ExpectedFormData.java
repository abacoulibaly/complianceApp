package com.threasury.compliance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "ExpectedFormData",
        example = "{\"brandName\":\"\",\"alcoholPercentage\":\"\",\"productType\":\"\"}"
)
public record ExpectedFormData(
        @Schema(defaultValue = "")
        @NotBlank String brandName,
        @Schema(defaultValue = "")
        @NotBlank String alcoholPercentage,
        @Schema(defaultValue = "")
        @NotBlank String productType
) {}
