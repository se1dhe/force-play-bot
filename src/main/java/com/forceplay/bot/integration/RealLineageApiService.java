package com.forceplay.bot.integration;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.dto.BossInfo;
import com.forceplay.bot.dto.EventInfo;
import com.forceplay.bot.dto.HwidConfirmCommand;
import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.dto.PromoRedeemResult;
import com.forceplay.bot.dto.TradeKeyResult;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "forceplay.bot.integration-mode", havingValue = "real")
public class RealLineageApiService implements LineageApiService {

    private final RestClient restClient;
    private final LineageServersProperties serversProperties;

    @Override
    public LinkRequestResult requestAccountLink(String serverName, String characterName, Long telegramId) {
        record Response(String requestId, String serverName, String characterName) {}
        Response response = post(serverName, "/api/%s/account/link/request".formatted(serverName),
                Map.of("characterName", characterName, "telegramId", telegramId), Response.class);
        return new LinkRequestResult(response.requestId(), response.serverName(), response.characterName());
    }

    @Override
    public LinkConfirmResult confirmAccountLink(String serverName, String requestId, Long telegramId) {
        record Response(String externalAccountId, String serverName, String hwid, List<String> characters) {}
        Response response = post(serverName, "/api/%s/account/link/confirm".formatted(serverName),
                Map.of("requestId", requestId, "telegramId", telegramId), Response.class);
        return new LinkConfirmResult(response.externalAccountId(), response.serverName(), response.hwid(), response.characters());
    }

    @Override
    public void confirmHwid(HwidConfirmCommand command) {
        post(command.serverName(), "/api/%s/hwid/confirm".formatted(command.serverName()), command, Void.class);
    }

    @Override
    public TradeKeyResult changeTradeKey(String serverName, String externalAccountId) {
        record Response(String tradeKey, String message) {}
        Response response = post(serverName, "/api/%s/account/tradekey".formatted(serverName),
                Map.of("externalAccountId", externalAccountId), Response.class);
        return new TradeKeyResult(response.tradeKey(), response.message());
    }

    @Override
    public PromoRedeemResult redeemPromo(String serverName, String externalAccountId, String code) {
        record Response(boolean redeemed, String message) {}
        Response response = post(serverName, "/api/%s/promo/redeem".formatted(serverName),
                Map.of("externalAccountId", externalAccountId, "code", code), Response.class);
        return new PromoRedeemResult(response.redeemed(), response.message());
    }

    @Override
    public PromoRedeemResult claimBonus(String serverName, String externalAccountId, Long telegramId) {
        record Response(boolean redeemed, String message) {}
        Response response = post(serverName, "/api/%s/bonus/claim".formatted(serverName),
                Map.of("externalAccountId", externalAccountId, "telegramId", telegramId), Response.class);
        return new PromoRedeemResult(response.redeemed(), response.message());
    }

    @Override
    public List<BossInfo> getBosses(String serverName) {
        BossInfo[] response = get(serverName, "/api/%s/bosses".formatted(serverName), BossInfo[].class);
        return response == null ? List.of() : List.of(response);
    }

    @Override
    public List<EventInfo> getEvents(String serverName) {
        EventInfo[] response = get(serverName, "/api/%s/events".formatted(serverName), EventInfo[].class);
        return response == null ? List.of() : List.of(response);
    }

    private <T> T post(String serverName, String path, Object body, Class<T> type) {
        LineageServersProperties.Server server = resolveServer(serverName);
        return restClient.post()
                .uri(server.getBaseUrl() + path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + server.getToken())
                .body(body)
                .retrieve()
                .body(type);
    }

    private <T> T get(String serverName, String path, Class<T> type) {
        LineageServersProperties.Server server = resolveServer(serverName);
        return restClient.get()
                .uri(server.getBaseUrl() + path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + server.getToken())
                .retrieve()
                .body(type);
    }

    private LineageServersProperties.Server resolveServer(String serverName) {
        return serversProperties.findByName(serverName)
                .orElseThrow(() -> new ForcePlayException("Unknown server: " + serverName));
    }
}
