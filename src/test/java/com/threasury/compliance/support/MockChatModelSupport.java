package com.threasury.compliance.support;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public final class MockChatModelSupport {

    private MockChatModelSupport() {}

    public static void stubExtraction(ChatModel chatModel, String jsonPayload) {
        ChatResponse response = new ChatResponse(List.of(
                new Generation(new AssistantMessage(jsonPayload))
        ));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
    }

    public static String compliantExtractionJson() {
        return """
                {
                  "brandName": "STONE'S THROW",
                  "alcoholPercentage": "12.5%",
                  "productType": "Wine",
                  "fullLabelText": "STONE'S THROW 12.5% Wine  GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects."
                }
                """;
    }

    public static String nonCompliantExtractionJson() {
        return """
                {
                  "brandName": "WRONG BRAND",
                  "alcoholPercentage": "14.0%",
                  "productType": "Beer",
                  "fullLabelText": "Wrong Brand 14.0% Beer government warning: lowercase phrase"
                }
                """;
    }
}
