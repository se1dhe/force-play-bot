package com.forceplay.bot.service;

import com.forceplay.bot.dto.TradeKeyResult;
import com.forceplay.bot.integration.LineageApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerActionService {

    private final LineageApiService lineageApiService;
    private final BonusService bonusService;
    private final TelegramBotPropertiesAccessor telegramBotPropertiesAccessor;

    public TradeKeyResult changeTradeKey(String serverName, String externalAccountId) {
        return lineageApiService.changeTradeKey(serverName, externalAccountId);
    }

    public String claimBonus(Long telegramId, String serverName, String externalAccountId) {
        return bonusService.claimIfSubscribed(
                telegramId,
                serverName,
                externalAccountId,
                telegramBotPropertiesAccessor.channelUsername()
        );
    }
}
