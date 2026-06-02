package com.forceplay.bot.util;

import com.forceplay.bot.config.MessagesProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageResolver {

    private final MessagesProperties messagesProperties;

    public String get(String key, String defaultValue) {
        return get("ru", key, defaultValue);
    }

    public String get(String language, String key, String defaultValue) {
        String normalizedLanguage = normalizeLanguage(language);
        Map<String, String> selectedTranslations = messagesProperties.getTranslations().get(normalizedLanguage);
        if (selectedTranslations != null && selectedTranslations.containsKey(key)) {
            return selectedTranslations.get(key);
        }
        Map<String, String> defaultTranslations = messagesProperties.getTranslations().get("ru");
        if (defaultTranslations != null && defaultTranslations.containsKey(key)) {
            return defaultTranslations.get(key);
        }
        return defaultValue;
    }

    public String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "ru";
        }
        String normalized = language.trim().toLowerCase();
        if (normalized.startsWith("uk")) {
            return "ua";
        }
        if (normalized.startsWith("ua")) {
            return "ua";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }
        if (normalized.startsWith("ru")) {
            return "ru";
        }
        return "ru";
    }
}
