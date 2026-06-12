package com.threasury.compliance.dto;

import java.time.Instant;
import java.util.List;

public record BatchComplianceResponse(
        int totalFiles,
        int compliantCount,
        int nonCompliantCount,
        long totalProcessingTimeMs,
        Instant completedAt,
        List<ComplianceTestResult> results
) {
    public static BatchComplianceResponse from(List<ComplianceTestResult> results, long totalProcessingTimeMs) {
        int compliant = (int) results.stream().filter(ComplianceTestResult::compliant).count();
        return new BatchComplianceResponse(
                results.size(),
                compliant,
                results.size() - compliant,
                totalProcessingTimeMs,
                Instant.now(),
                results
        );
    }
}
