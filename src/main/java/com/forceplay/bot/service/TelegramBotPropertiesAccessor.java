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

    public String username() {
        return properties.getUsername();
    }

    public long bonusItemId() {
        return properties.getBonusItemId();
    }

    public int bonusItemCount() {
        return properties.getBonusItemCount();
    }

    public String promoDailyCron() {
        return properties.getPromoDailyCron();
    }

    public int promoLowStockThreshold() {
        return properties.getPromoLowStockThreshold();
    }

    public String discordUrl() {
        return properties.getDiscordUrl();
    }

    public String telegramChatUrl() {
        return properties.getTelegramChatUrl();
    }

    public String telegramChannelUrl() {
        return properties.getTelegramChannelUrl();
    }

    public String shopUrl() {
        return properties.getShopUrl();
    }

    public String updaterUrl() {
        return properties.getUpdaterUrl();
    }

    public String supportUrl() {
        return properties.getSupportUrl();
    }

    public String tarotWebappUrl() {
        return properties.getTarotWebappUrl();
    }
}
