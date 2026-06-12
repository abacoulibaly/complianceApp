package com.threasury.compliance.dto;

import java.time.Instant;
import java.util.List;

public record ComplianceTestResult(
        String fileName,
        boolean compliant,
        long processingTimeMs,
        Instant processedAt,
        ExtractedLabelData extractedData,
        List<RuleCheckResult> ruleResults,
        String errorMessage
) {
    public static ComplianceTestResult success(
            String fileName,
            long processingTimeMs,
            ExtractedLabelData extractedData,
            List<RuleCheckResult> ruleResults
    ) {
        boolean compliant = ruleResults.stream().allMatch(RuleCheckResult::passed);
        return new ComplianceTestResult(
                fileName,
                compliant,
                processingTimeMs,
                Instant.now(),
                extractedData,
                ruleResults,
                null
        );
    }

    public static ComplianceTestResult failure(String fileName, long processingTimeMs, String errorMessage) {
        return new ComplianceTestResult(
                fileName,
                false,
                processingTimeMs,
                Instant.now(),
                null,
                List.of(),
                errorMessage
        );
    }
}
