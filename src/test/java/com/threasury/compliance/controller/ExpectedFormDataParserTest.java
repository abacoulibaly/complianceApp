package com.threasury.compliance.controller;

import com.threasury.compliance.dto.ExpectedFormData;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ExpectedFormDataParserTest {

    private ExpectedFormDataParser parser;

    @BeforeEach
    void setUp() {
        Validator validator = Mockito.mock(Validator.class);
        when(validator.validate(any())).thenReturn(java.util.Set.of());
        parser = new ExpectedFormDataParser(new com.fasterxml.jackson.databind.ObjectMapper(), validator);
    }

    @Test
    void resolve_parsesJsonFormData() {
        ExpectedFormData result = parser.resolve(
                """
                        {"brandName":"Stone's Throw","alcoholPercentage":"12.5","productType":"Wine"}
                        """,
                null,
                null,
                null
        );

        assertEquals("Stone's Throw", result.brandName());
        assertEquals("12.5", result.alcoholPercentage());
        assertEquals("Wine", result.productType());
    }

    @Test
    void resolve_buildsFromIndividualFields() {
        ExpectedFormData result = parser.resolve(
                null,
                "Stone's Throw",
                "12.5",
                "Wine"
        );

        assertEquals("Stone's Throw", result.brandName());
    }

    @Test
    void resolve_throwsWhenNoFormDataProvided() {
        assertThrows(IllegalArgumentException.class, () -> parser.resolve(null, null, null, null));
    }
}
