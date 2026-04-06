package com.forceplay.bot.service;

import com.forceplay.bot.config.TelegramBotProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramBotPropertiesAccessor {

    private final TelegramBotProperties properties;

    public String channelUsername() {
        return properties.getChannelUsername();
    }
}
