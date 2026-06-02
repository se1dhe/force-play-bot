package com.forceplay.bot.service;

import com.forceplay.bot.dto.TradeKeyResult;
import com.forceplay.bot.dto.HwidUnlinkResult;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.integration.LineageApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerActionService {

    private final LineageApiService lineageApiService;
    private final BonusService bonusService;
    private final TelegramBotPropertiesAccessor telegramBotPropertiesAccessor;

    public HwidUnlinkResult unlinkHwid(GameCharacter character) {
        return lineageApiService.unlinkHwid(
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getExternalCharacterId()
        );
    }

    public TradeKeyResult changeTradeKey(GameCharacter character, String password) {
        return lineageApiService.changeTradeKey(
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getExternalCharacterId(),
                password
        );
    }

    public String claimBonus(Long telegramId, String language, GameCharacter character) {
        return bonusService.claimIfSubscribed(
                telegramId,
                language,
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getExternalCharacterId(),
                telegramBotPropertiesAccessor.channelUsername(),
                telegramBotPropertiesAccessor.bonusItemId(),
                telegramBotPropertiesAccessor.bonusItemCount()
        );
    }
}
