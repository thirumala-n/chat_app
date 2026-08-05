package com.chat.app.service;

import com.chat.app.ai.AiPromptTemplates;
import com.chat.app.ai.AiProviderRegistry;
import com.chat.app.dto.request.AiChatRequest;
import com.chat.app.dto.response.AiConversationResponse;
import com.chat.app.dto.response.AiMessageResponse;
import com.chat.app.entity.AiConversation;
import com.chat.app.entity.AiMessage;
import com.chat.app.entity.User;
import com.chat.app.enums.AiProviderType;
import com.chat.app.exception.BadRequestException;
import com.chat.app.exception.ResourceNotFoundException;
import com.chat.app.mapper.AppMapper;
import com.chat.app.repository.AiConversationRepository;
import com.chat.app.repository.AiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final AiProviderRegistry providerRegistry;
    private final AiPromptTemplates promptTemplates;
    private final AppMapper appMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AiConversationResponse> getConversations(String userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(appMapper::toAiConversationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AiConversationResponse getConversation(String userId, String conversationId) {
        AiConversation conversation = conversationRepository.findByIdAndUserIdWithMessages(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found"));
        AiConversationResponse response = appMapper.toAiConversationResponse(conversation);
        response.setMessages(conversation.getMessages().stream()
                .map(appMapper::toAiMessageResponse)
                .toList());
        return response;
    }

    @Transactional
    public AiMessageResponse chat(String userId, AiChatRequest request) {
        User user = userService.findUser(userId);
        AiProviderType provider = resolveProvider(request.getProvider());
        AiConversation conversation = getOrCreateConversation(user, request, provider);

        String userContent = promptTemplates.buildFeaturePrompt(request.getFeatureType(), request.getMessage());
        saveMessage(conversation, "user", request.getMessage());

        List<Message> messages = buildMessageHistory(conversation, request.getFeatureType(), userContent);
        ChatModel chatModel = providerRegistry.getChatModel(provider);
        ChatResponse chatResponse = chatModel.call(new Prompt(messages));
        String assistantContent = chatResponse.getResult().getOutput().getText();

        AiMessage assistantMessage = saveMessage(conversation, "assistant", assistantContent);

        if ("New Chat".equals(conversation.getTitle()) && assistantContent.length() > 10) {
            conversation.setTitle(generateTitle(request.getMessage()));
            conversationRepository.save(conversation);
        }

        return appMapper.toAiMessageResponse(assistantMessage);
    }

    public Flux<String> streamChat(String userId, AiChatRequest request) {
        User user = userService.findUser(userId);
        AiProviderType provider = resolveProvider(request.getProvider());
        AiConversation conversation = getOrCreateConversation(user, request, provider);

        String userContent = promptTemplates.buildFeaturePrompt(request.getFeatureType(), request.getMessage());
        saveMessage(conversation, "user", request.getMessage());

        List<Message> messages = buildMessageHistory(conversation, request.getFeatureType(), userContent);
        ChatModel chatModel = providerRegistry.getChatModel(provider);

        StringBuilder fullResponse = new StringBuilder();
        return chatModel.stream(new Prompt(messages))
                .map(response -> {
                    String text = response.getResult().getOutput().getText();
                    if (text != null) {
                        fullResponse.append(text);
                    }
                    return text != null ? text : "";
                })
                .doOnComplete(() -> saveMessage(conversation, "assistant", fullResponse.toString()));
    }

    @Transactional
    public void deleteConversation(String userId, String conversationId) {
        AiConversation conversation = conversationRepository.findByIdAndUserIdWithMessages(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found"));
        conversationRepository.delete(conversation);
    }

    private AiConversation getOrCreateConversation(User user, AiChatRequest request, AiProviderType provider) {
        if (request.getConversationId() != null) {
            return conversationRepository.findByIdAndUserIdWithMessages(request.getConversationId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found"));
        }

        AiConversation conversation = AiConversation.builder()
                .user(user)
                .title("New Chat")
                .provider(provider)
                .featureType(request.getFeatureType())
                .build();
        return conversationRepository.save(conversation);
    }

    private AiMessage saveMessage(AiConversation conversation, String role, String content) {
        AiMessage message = AiMessage.builder()
                .conversation(conversation)
                .role(role)
                .content(content)
                .build();
        message = messageRepository.save(message);
        conversation.getMessages().add(message);
        return message;
    }

    private List<Message> buildMessageHistory(AiConversation conversation, String featureType, String currentMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(promptTemplates.getSystemPrompt(featureType)));

        conversation.getMessages().stream()
                .limit(Math.max(0, conversation.getMessages().size() - 1))
                .forEach(msg -> {
                    if ("user".equals(msg.getRole())) {
                        messages.add(new UserMessage(msg.getContent()));
                    } else if ("assistant".equals(msg.getRole())) {
                        messages.add(new AssistantMessage(msg.getContent()));
                    }
                });

        messages.add(new UserMessage(currentMessage));
        return messages;
    }

    private AiProviderType resolveProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return providerRegistry.getDefaultProvider();
        }
        try {
            return AiProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid AI provider: " + provider);
        }
    }

    private String generateTitle(String firstMessage) {
        String title = firstMessage.length() > 50 ? firstMessage.substring(0, 47) + "..." : firstMessage;
        return title.replaceAll("\\s+", " ").trim();
    }
}
