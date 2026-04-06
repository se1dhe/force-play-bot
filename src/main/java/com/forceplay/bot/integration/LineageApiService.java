package com.forceplay.bot.integration;

import com.forceplay.bot.dto.BossInfo;
import com.forceplay.bot.dto.EventInfo;
import com.forceplay.bot.dto.HwidConfirmCommand;
import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.dto.PromoRedeemResult;
import com.forceplay.bot.dto.TradeKeyResult;

import java.util.List;

public interface LineageApiService {
    LinkRequestResult requestAccountLink(String serverName, String characterName, Long telegramId);

    LinkConfirmResult confirmAccountLink(String serverName, String requestId, Long telegramId);

    void confirmHwid(HwidConfirmCommand command);

    TradeKeyResult changeTradeKey(String serverName, String externalAccountId);

    PromoRedeemResult redeemPromo(String serverName, String externalAccountId, String code);

    PromoRedeemResult claimBonus(String serverName, String externalAccountId, Long telegramId);

    List<BossInfo> getBosses(String serverName);

    List<EventInfo> getEvents(String serverName);
}
