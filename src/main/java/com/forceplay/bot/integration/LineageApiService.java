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

import java.util.List;

public interface LineageApiService {
    LinkRequestResult requestAccountLink(String serverName, String characterName, Long telegramId);

    LinkConfirmResult confirmAccountLink(String serverName, String requestId, Long telegramId);

    void confirmHwid(HwidConfirmCommand command);

    HwidUnlinkResult unlinkHwid(String serverName, String externalAccountId, Long externalCharacterId);

    TradeKeyResult changeTradeKey(String serverName, String externalAccountId, Long externalCharacterId, String password);

    BonusClaimResult claimBonus(String serverName, String externalAccountId, Long externalCharacterId, Long telegramId, long itemId, int itemCount);

    CharacterProfileResult getCharacterProfile(String serverName, String externalAccountId, Long externalCharacterId);

    AutofarmStatusResult getAutofarmStatus(String serverName, String externalAccountId, Long externalCharacterId);

    CharacterActionResult reviveAutofarm(String serverName, String externalAccountId, Long externalCharacterId);

    CharacterActionResult teleportToTown(String serverName, String externalAccountId, Long externalCharacterId);
}
