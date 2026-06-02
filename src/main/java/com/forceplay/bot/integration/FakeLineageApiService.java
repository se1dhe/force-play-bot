package com.forceplay.bot.integration;

import com.forceplay.bot.dto.CharacterActionResult;
import com.forceplay.bot.dto.AutofarmStatusResult;
import com.forceplay.bot.dto.CharacterProfileResult;
import com.forceplay.bot.dto.HwidConfirmCommand;
import com.forceplay.bot.dto.HwidUnlinkResult;
import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.dto.BonusClaimResult;
import com.forceplay.bot.dto.TradeKeyResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "forceplay.bot.integration-mode", havingValue = "mock", matchIfMissing = true)
public class FakeLineageApiService implements LineageApiService {

    @Override
    public LinkRequestResult requestAccountLink(String serverName, String characterName, Long telegramId) {
        return new LinkRequestResult(UUID.randomUUID().toString(), serverName, characterName, true, "prompt_sent", null);
    }

    @Override
    public LinkConfirmResult confirmAccountLink(String serverName, String requestId, Long telegramId) {
        return new LinkConfirmResult(
                "acc-" + telegramId,
                serverName,
                "mock-hwid",
                List.of(
                        new LinkConfirmResult.LinkedCharacter(telegramId * 100 + 1, "MainChar"),
                        new LinkConfirmResult.LinkedCharacter(telegramId * 100 + 2, "Buffer"),
                        new LinkConfirmResult.LinkedCharacter(telegramId * 100 + 3, "Spoiler")
                )
        );
    }

    @Override
    public void confirmHwid(HwidConfirmCommand command) {
    }

    @Override
    public HwidUnlinkResult unlinkHwid(String serverName, String externalAccountId, Long externalCharacterId) {
        return new HwidUnlinkResult(externalCharacterId, true, "HWID отвязан");
    }

    @Override
    public TradeKeyResult changeTradeKey(String serverName, String externalAccountId, Long externalCharacterId, String password) {
        return new TradeKeyResult(externalCharacterId, true, "Trade key обновлен");
    }

    @Override
    public BonusClaimResult claimBonus(String serverName, String externalAccountId, Long externalCharacterId, Long telegramId, long itemId, int itemCount) {
        return new BonusClaimResult(true, "Bonus claimed for character " + externalCharacterId + ": " + itemId + " x" + itemCount);
    }

    @Override
    public CharacterProfileResult getCharacterProfile(String serverName, String externalAccountId, Long externalCharacterId) {
        int profileIndex = Math.floorMod(externalCharacterId.intValue(), 3);
        return switch (profileIndex) {
            case 0 -> new CharacterProfileResult(externalCharacterId, 65, "Sorcerer", 58, 0, "Moonlight", false);
            case 1 -> new CharacterProfileResult(externalCharacterId, 78, "Paladin", 142, 3, "Immortal", true);
            default -> new CharacterProfileResult(externalCharacterId, 76, "Spoiler", 89, 1, null, true);
        };
    }

    @Override
    public AutofarmStatusResult getAutofarmStatus(String serverName, String externalAccountId, Long externalCharacterId) {
        if (externalCharacterId % 3 == 0) {
            return new AutofarmStatusResult(externalCharacterId, true, false, false, "RaidFighter", "Персонаж мертв, автофарм остановлен");
        }
        if (externalCharacterId % 2 == 0) {
            return new AutofarmStatusResult(externalCharacterId, true, false, true, null, "Автофарм остановлен");
        }
        return new AutofarmStatusResult(externalCharacterId, true, true, true, null, "Автофарм работает");
    }

    @Override
    public CharacterActionResult reviveAutofarm(String serverName, String externalAccountId, Long externalCharacterId) {
        return new CharacterActionResult(externalCharacterId, true, "Персонаж воскрешен на месте, автофарм снова запущен");
    }

    @Override
    public CharacterActionResult teleportToTown(String serverName, String externalAccountId, Long externalCharacterId) {
        return new CharacterActionResult(externalCharacterId, true, "Ваш персонаж был телепортирован в город");
    }
}
