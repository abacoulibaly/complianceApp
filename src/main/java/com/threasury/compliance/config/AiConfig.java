package com.threasury.compliance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    ChatModel chatModel(
            Environment environment,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o}") String model,
            @Value("${spring.ai.openai.chat.options.temperature:0.0}") double temperature
    ) {
        String apiKey = resolveApiKey(environment);
        if (!StringUtils.hasText(apiKey)) {
            log.warn("No OpenAI API key configured — application will start but compliance verification requests will fail.");
            return new UnconfiguredChatModel();
        }

        log.info("OpenAI API key detected — vision extraction enabled (model: {}).", model);
        var openAiApi = OpenAiApi.builder().apiKey(apiKey).build();
        var options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    private String resolveApiKey(Environment environment) {
        String fromSpringAi = environment.getProperty("spring.ai.openai.api-key");
        if (StringUtils.hasText(fromSpringAi)) {
            return fromSpringAi.trim();
        }
        String fromOpenAiEnv = environment.getProperty("OPENAI_API_KEY");
        if (StringUtils.hasText(fromOpenAiEnv)) {
            return fromOpenAiEnv.trim();
        }
        return null;
    }
}
