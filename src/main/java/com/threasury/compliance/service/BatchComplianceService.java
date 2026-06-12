package com.threasury.compliance.service;

import com.threasury.compliance.dto.BatchComplianceResponse;
import com.threasury.compliance.dto.ComplianceTestResult;
import com.threasury.compliance.dto.ExpectedFormData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Processes batch uploads concurrently via a virtual-thread queue without blocking servlet threads.
 */
@Service
public class BatchComplianceService {

    private final ComplianceProcessingService processingService;
    private final Executor batchQueueExecutor;

    public BatchComplianceService(
            ComplianceProcessingService processingService,
            Executor batchQueueExecutor
    ) {
        this.processingService = processingService;
        this.batchQueueExecutor = batchQueueExecutor;
    }

    public BatchComplianceResponse processBatch(MultipartFile[] images, ExpectedFormData formData) {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("At least one image file is required for batch processing.");
        }

        long start = System.nanoTime();
        List<CompletableFuture<ComplianceTestResult>> futures = new ArrayList<>();

        for (MultipartFile image : images) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> processingService.process(image, formData),
                    batchQueueExecutor
            ));
        }

        List<ComplianceTestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalMs = (System.nanoTime() - start) / 1_000_000;
        return BatchComplianceResponse.from(results, totalMs);
    }

    public ComplianceTestResult processSingle(MultipartFile image, ExpectedFormData formData) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }
        return processingService.process(image, formData);
    }
}
