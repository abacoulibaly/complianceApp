package com.threasury.compliance.controller;

import com.threasury.compliance.dto.BatchComplianceResponse;
import com.threasury.compliance.dto.ComplianceTestResult;
import com.threasury.compliance.dto.ExpectedFormData;
import com.threasury.compliance.service.BatchComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/compliance")
@Tag(name = "Compliance Verification", description = "Upload label images and run automated compliance checks")
public class ComplianceController {

    private final BatchComplianceService batchComplianceService;
    private final ExpectedFormDataParser formDataParser;

    public ComplianceController(
            BatchComplianceService batchComplianceService,
            ExpectedFormDataParser formDataParser
    ) {
        this.batchComplianceService = batchComplianceService;
        this.formDataParser = formDataParser;
    }

    @Operation(
            summary = "Verify a single label image",
            description = """
                    Upload one JPG/PNG label image with expected form metadata.
                    Provide either a `formData` JSON field or individual fields: brandName, alcoholPercentage, productType.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Compliance test completed",
                            content = @Content(schema = @Schema(implementation = ComplianceTestResult.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "408", description = "Processing timeout exceeded SLA",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplianceTestResult> verifySingle(
            @Parameter(
                    description = "Label image file (JPG/PNG)",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam(value = "image", required = false) MultipartFile image,
            @Parameter(
                    description = "Optional JSON metadata. Leave empty when using individual form fields.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExpectedFormData.class, defaultValue = "")
                    )
            )
            @RequestPart(value = "formData", required = false) String formData,
            @Parameter(schema = @Schema(defaultValue = ""))
            @RequestParam(value = "brandName", required = false, defaultValue = "") String brandName,
            @Parameter(schema = @Schema(defaultValue = ""))
            @RequestParam(value = "alcoholPercentage", required = false, defaultValue = "") String alcoholPercentage,
            @Parameter(schema = @Schema(defaultValue = ""))
            @RequestParam(value = "productType", required = false, defaultValue = "") String productType
    ) {
        requireImage(image);
        var expected = formDataParser.resolve(formData, brandName, alcoholPercentage, productType);
        return ResponseEntity.ok(batchComplianceService.processSingle(image, expected));
    }

    @Operation(
            summary = "Verify multiple label images in batch",
            description = """
                    Upload multiple JPG/PNG label images with expected form metadata.
                    Provide either a `formData` JSON field or individual fields: brandName, alcoholPercentage, productType.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Batch compliance test completed",
                            content = @Content(schema = @Schema(implementation = BatchComplianceResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @PostMapping(value = "/verify/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BatchComplianceResponse> verifyBatch(
            @Parameter(
                    description = "Label image files (JPG/PNG)",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @Parameter(
                    description = "Optional JSON metadata. Leave empty when using individual form fields.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExpectedFormData.class, defaultValue = "")
                    )
            )
            @RequestPart(value = "formData", required = false) String formData,
            @Parameter(schema = @Schema(defaultValue = ""))
            @RequestParam(value = "brandName", required = false, defaultValue = "") String brandName,
            @Parameter(schema = @Schema(defaultValue = ""))
            @RequestParam(value = "alcoholPercentage", required = false, defaultValue = "") String alcoholPercentage,
            @Parameter(schema = @Schema(defaultValue = ""))
            @RequestParam(value = "productType", required = false, defaultValue = "") String productType
    ) {
        requireImages(images);
        var expected = formDataParser.resolve(formData, brandName, alcoholPercentage, productType);
        return ResponseEntity.ok(batchComplianceService.processBatch(images, expected));
    }

    private void requireImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Required part 'image' is not present. Upload a JPG or PNG label file.");
        }
    }

    private void requireImages(MultipartFile[] images) {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("Required part 'images' is not present. Upload at least one JPG or PNG label file.");
        }
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                return;
            }
        }
        throw new IllegalArgumentException("Required part 'images' is not present. Upload at least one JPG or PNG label file.");
    }
}
