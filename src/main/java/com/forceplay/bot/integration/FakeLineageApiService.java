package com.forceplay.bot.integration;

import com.forceplay.bot.dto.BossInfo;
import com.forceplay.bot.dto.EventInfo;
import com.forceplay.bot.dto.HwidConfirmCommand;
import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.dto.PromoRedeemResult;
import com.forceplay.bot.dto.TradeKeyResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "forceplay.bot.integration-mode", havingValue = "mock", matchIfMissing = true)
public class FakeLineageApiService implements LineageApiService {

    @Override
    public LinkRequestResult requestAccountLink(String serverName, String characterName, Long telegramId) {
        return new LinkRequestResult(UUID.randomUUID().toString(), serverName, characterName);
    }

    @Override
    public LinkConfirmResult confirmAccountLink(String serverName, String requestId, Long telegramId) {
        return new LinkConfirmResult(
                "acc-" + telegramId,
                serverName,
                "mock-hwid",
                List.of("MainChar", "Buffer", "Spoiler")
        );
    }

    @Override
    public void confirmHwid(HwidConfirmCommand command) {
    }

    @Override
    public TradeKeyResult changeTradeKey(String serverName, String externalAccountId) {
        return new TradeKeyResult("TRADE-" + externalAccountId, "Trade key updated");
    }

    @Override
    public PromoRedeemResult redeemPromo(String serverName, String externalAccountId, String code) {
        return new PromoRedeemResult(true, "Promo redeemed: " + code);
    }

    @Override
    public PromoRedeemResult claimBonus(String serverName, String externalAccountId, Long telegramId) {
        return new PromoRedeemResult(true, "Bonus claimed");
    }

    @Override
    public List<BossInfo> getBosses(String serverName) {
        return List.of(new BossInfo("Queen Ant", OffsetDateTime.now().plusHours(2).toString(), serverName));
    }

    @Override
    public List<EventInfo> getEvents(String serverName) {
        return List.of(new EventInfo("TvT Event", OffsetDateTime.now().plusHours(1).toString(), serverName));
    }
}
