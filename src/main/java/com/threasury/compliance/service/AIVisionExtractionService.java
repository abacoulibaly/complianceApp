package com.threasury.compliance.service;

import com.threasury.compliance.dto.ExtractedLabelData;
import com.threasury.compliance.exception.ComplianceProcessingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AIVisionExtractionService {

    private static final String EXTRACTION_PROMPT = """
            You are analyzing an alcohol bottle label image for government compliance verification.
            Extract the following fields exactly as they appear on the label:
            - brandName: the product brand name
            - alcoholPercentage: the alcohol by volume (ABV) value as printed
            - productType: the beverage category (e.g. wine, beer, spirits)
            - fullLabelText: the complete verbatim text visible on the label, preserving original casing and spacing
            Return only structured data matching the requested schema.
            """;

    private final ChatClient chatClient;

    public AIVisionExtractionService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ExtractedLabelData extract(MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            MimeType mimeType = resolveMimeType(image);

            return chatClient.prompt()
                    .user(spec -> spec
                            .text(EXTRACTION_PROMPT)
                            .media(new Media(mimeType, new ByteArrayResource(imageBytes))))
                    .call()
                    .entity(ExtractedLabelData.class);
        } catch (IOException ex) {
            throw new ComplianceProcessingException("Failed to read image file: " + image.getOriginalFilename(), ex);
        } catch (Exception ex) {
            throw new ComplianceProcessingException("AI vision extraction failed for: " + image.getOriginalFilename(), ex);
        }
    }

    private MimeType resolveMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return MimeTypeUtils.parseMimeType(contentType);
        }
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".png")) {
            return MimeTypeUtils.IMAGE_PNG;
        }
        return MimeTypeUtils.IMAGE_JPEG;
    }
}
