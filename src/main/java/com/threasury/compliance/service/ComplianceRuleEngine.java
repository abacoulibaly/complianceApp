package com.threasury.compliance.service;

import com.threasury.compliance.dto.ExpectedFormData;
import com.threasury.compliance.dto.ExtractedLabelData;
import com.threasury.compliance.dto.RuleCheckResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ComplianceRuleEngine {

    /**
     * Exact phrase required on compliant labels. Leading and trailing spaces are significant.
     */
    public static final String REQUIRED_GOVERNMENT_WARNING_PHRASE = " GOVERNMENT WARNING: ";

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]");

    public List<RuleCheckResult> evaluate(ExpectedFormData expected, ExtractedLabelData extracted) {
        List<RuleCheckResult> results = new ArrayList<>();

        results.add(evaluateFuzzyBrandName(expected.brandName(), extracted.brandName()));
        results.add(evaluateAlcoholPercentage(expected.alcoholPercentage(), extracted.alcoholPercentage()));
        results.add(evaluateFuzzyProductType(expected.productType(), extracted.productType()));
        results.add(evaluatePerfectHealthWarning(extracted.fullLabelText()));

        return results;
    }

    private RuleCheckResult evaluateFuzzyBrandName(String expected, String actual) {
        boolean passed = fuzzyMatch(expected, actual);
        if (passed) {
            return RuleCheckResult.passed("Brand Name", "FUZZY", expected, actual);
        }
        return RuleCheckResult.failed(
                "Brand Name",
                "FUZZY",
                expected,
                actual,
                "Brand name does not match (case-insensitive, whitespace-tolerant comparison failed).");
    }

    private RuleCheckResult evaluateFuzzyProductType(String expected, String actual) {
        boolean passed = fuzzyMatch(expected, actual);
        if (passed) {
            return RuleCheckResult.passed("Product Type", "FUZZY", expected, actual);
        }
        return RuleCheckResult.failed(
                "Product Type",
                "FUZZY",
                expected,
                actual,
                "Product type does not match (case-insensitive, whitespace-tolerant comparison failed).");
    }

    private RuleCheckResult evaluateAlcoholPercentage(String expected, String actual) {
        String normalizedExpected = normalizePercentage(expected);
        String normalizedActual = normalizePercentage(actual);
        boolean passed = normalizedExpected.equals(normalizedActual);

        if (passed) {
            return RuleCheckResult.passed("Alcohol Percentage", "NORMALIZED", expected, actual);
        }
        return RuleCheckResult.failed(
                "Alcohol Percentage",
                "NORMALIZED",
                expected,
                actual,
                "Alcohol percentage values do not match after normalization.");
    }

    private RuleCheckResult evaluatePerfectHealthWarning(String fullLabelText) {
        if (fullLabelText == null || fullLabelText.isBlank()) {
            return RuleCheckResult.failed(
                    "Government Health Warning",
                    "PERFECT",
                    REQUIRED_GOVERNMENT_WARNING_PHRASE,
                    null,
                    "Label text is missing; required government warning phrase not found.");
        }

        if (fullLabelText.contains(REQUIRED_GOVERNMENT_WARNING_PHRASE)) {
            return RuleCheckResult.passed(
                    "Government Health Warning",
                    "PERFECT",
                    REQUIRED_GOVERNMENT_WARNING_PHRASE,
                    "Phrase present in label text.");
        }

        return RuleCheckResult.failed(
                "Government Health Warning",
                "PERFECT",
                REQUIRED_GOVERNMENT_WARNING_PHRASE,
                extractWarningSnippet(fullLabelText),
                "Label must contain the exact phrase \" GOVERNMENT WARNING: \" in all capital letters with surrounding spaces.");
    }

    /**
     * Case-insensitive, whitespace-tolerant equality for brand and product type fields.
     */
    boolean fuzzyMatch(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return normalizeForFuzzy(expected).equals(normalizeForFuzzy(actual));
    }

    private String normalizeForFuzzy(String value) {
        return NON_ALPHANUMERIC.matcher(value.toLowerCase(Locale.ROOT)).replaceAll("");
    }

    private String normalizePercentage(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9.]", "").trim();
    }

    private String extractWarningSnippet(String labelText) {
        int idx = labelText.toUpperCase(Locale.ROOT).indexOf("GOVERNMENT WARNING");
        if (idx < 0) {
            return "Phrase not found";
        }
        int start = Math.max(0, idx - 5);
        int end = Math.min(labelText.length(), idx + 40);
        return labelText.substring(start, end);
    }
}
