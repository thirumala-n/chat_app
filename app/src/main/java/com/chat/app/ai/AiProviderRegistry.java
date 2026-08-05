package com.chat.app.ai;

import com.chat.app.enums.AiProviderType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AiProviderRegistry {

    private final Map<AiProviderType, ChatModel> providers;
    private final AiProviderType defaultProvider;

    public AiProviderRegistry(
            @Autowired(required = false) @Qualifier("openAiChatModel") ChatModel openAiChatModel,
            @Value("${spring.ai.provider:openai}") String defaultProviderName) {

        this.providers = new java.util.EnumMap<>(AiProviderType.class);
        if (openAiChatModel != null) {
            providers.put(AiProviderType.OPENAI, openAiChatModel);
        }

        this.defaultProvider = resolveProvider(defaultProviderName);
    }

    public ChatModel getChatModel(AiProviderType provider) {
        ChatModel model = providers.get(provider != null ? provider : defaultProvider);
        if (model == null) {
            throw new IllegalStateException("AI provider not configured: " + provider);
        }
        return model;
    }

    public ChatModel getDefaultChatModel() {
        return getChatModel(defaultProvider);
    }

    public AiProviderType getDefaultProvider() {
        return defaultProvider;
    }

    private AiProviderType resolveProvider(String name) {
        try {
            return AiProviderType.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return AiProviderType.OPENAI;
        }
    }
}
