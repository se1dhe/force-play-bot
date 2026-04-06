package com.forceplay.bot.util;

import com.forceplay.bot.config.MessagesProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageResolver {

    private final MessagesProperties messagesProperties;

    public String get(String key, String defaultValue) {
        return messagesProperties.getTexts().getOrDefault(key, defaultValue);
    }
}
