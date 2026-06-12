package com.threasury.compliance.service;

import com.threasury.compliance.config.ComplianceProperties;
import com.threasury.compliance.dto.ComplianceTestResult;
import com.threasury.compliance.dto.ExpectedFormData;
import com.threasury.compliance.exception.ProcessingTimeoutException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ComplianceProcessingService {

    private final AIVisionExtractionService visionService;
    private final ComplianceRuleEngine ruleEngine;
    private final Executor complianceVirtualExecutor;
    private final long timeoutMs;

    public ComplianceProcessingService(
            AIVisionExtractionService visionService,
            ComplianceRuleEngine ruleEngine,
            Executor complianceVirtualExecutor,
            ComplianceProperties properties
    ) {
        this.visionService = visionService;
        this.ruleEngine = ruleEngine;
        this.complianceVirtualExecutor = complianceVirtualExecutor;
        this.timeoutMs = properties.processing().timeoutMs();
    }

    public ComplianceTestResult process(MultipartFile image, ExpectedFormData formData) {
        long start = System.nanoTime();
        String fileName = resolveFileName(image);

        try {
            var extracted = CompletableFuture
                    .supplyAsync(() -> visionService.extract(image), complianceVirtualExecutor)
                    .orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .join();

            var ruleResults = ruleEngine.evaluate(formData, extracted);
            long elapsedMs = elapsedMillis(start);

            return ComplianceTestResult.success(fileName, elapsedMs, extracted, ruleResults);
        } catch (Exception ex) {
            long elapsedMs = elapsedMillis(start);
            if (isTimeout(ex)) {
                throw new ProcessingTimeoutException(
                        "Processing exceeded SLA of " + timeoutMs + "ms for file: " + fileName, ex);
            }
            return ComplianceTestResult.failure(fileName, elapsedMs, rootCauseMessage(ex));
        }
    }

    private boolean isTimeout(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String rootCauseMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : "Unknown processing error";
    }

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private String resolveFileName(MultipartFile image) {
        return image.getOriginalFilename() != null ? image.getOriginalFilename() : "unknown";
    }
}
