package com.threasury.compliance.config;

import com.threasury.compliance.exception.ComplianceProcessingException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Placeholder {@link ChatModel} used when no OpenAI API key is configured.
 * Allows the application to start; requests fail with a clear message at call time.
 */
final class UnconfiguredChatModel implements ChatModel {

    static final String MESSAGE =
            "OpenAI API key is not configured. Set the OPENAI_API_KEY environment variable "
                    + "or spring.ai.openai.api-key property, then restart the application.";

    @Override
    public ChatResponse call(Prompt prompt) {
        throw new ComplianceProcessingException(MESSAGE);
    }
}
