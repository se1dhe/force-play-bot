package com.forceplay.bot.service;

import com.forceplay.bot.integration.LineageApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final LineageApiService lineageApiService;
    private final TelegramGateway telegramGateway;

    public String claimIfSubscribed(Long telegramId, String serverName, String externalAccountId, String channelUsername) {
        if (!telegramGateway.isSubscribed(telegramId, channelUsername)) {
            return "Subscription required";
        }
        return lineageApiService.claimBonus(serverName, externalAccountId, telegramId).message();
    }
}
