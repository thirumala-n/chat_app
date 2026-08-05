package com.chat.app.ai;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AiPromptTemplates {

    private static final Map<String, String> FEATURE_PROMPTS = Map.of(
            "SUMMARIZE", "You are a summarization assistant. Provide concise, well-structured summaries.",
            "CODE_EXPLAIN", "You are an expert software engineer. Explain code clearly with examples.",
            "RESUME_REVIEW", "You are a career coach. Review resumes and provide actionable feedback.",
            "INTERVIEW_PREP", "You are an interview coach. Generate relevant interview questions and tips.",
            "SUGGEST", "You are a helpful assistant suggesting short, natural chat replies."
    );

    public String getSystemPrompt(String featureType) {
        if (featureType == null || featureType.isBlank()) {
            return "You are a helpful AI assistant in a real-time chat platform. " +
                    "Respond in markdown when appropriate. Be concise and professional.";
        }
        return FEATURE_PROMPTS.getOrDefault(featureType.toUpperCase(),
                "You are a helpful AI assistant. Respond in markdown when appropriate.");
    }

    public String buildFeaturePrompt(String featureType, String userMessage) {
        return switch (featureType != null ? featureType.toUpperCase() : "") {
            case "SUMMARIZE" -> "Summarize the following:\n\n" + userMessage;
            case "CODE_EXPLAIN" -> "Explain the following code:\n\n```\n" + userMessage + "\n```";
            case "RESUME_REVIEW" -> "Review this resume and provide feedback:\n\n" + userMessage;
            case "INTERVIEW_PREP" -> "Generate interview questions for:\n\n" + userMessage;
            case "SUGGEST" -> "Suggest 3 short reply options for this message:\n\n" + userMessage;
            default -> userMessage;
        };
    }
}
