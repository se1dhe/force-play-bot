package com.forceplay.bot.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.dto.AutofarmStatusResult;
import com.forceplay.bot.dto.BonusClaimResult;
import com.forceplay.bot.dto.CharacterActionResult;
import com.forceplay.bot.dto.CharacterProfileResult;
import com.forceplay.bot.dto.HwidConfirmCommand;
import com.forceplay.bot.dto.HwidUnlinkResult;
import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.dto.TradeKeyResult;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
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
        JsonNode response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getAccountLinkRequest()),
                Map.of("characterName", characterName, "telegramId", telegramId), JsonNode.class);
        return new LinkRequestResult(
                text(response, "requestId", "request_id"),
                text(response, "serverName", "server_name"),
                text(response, "characterName", "character_name"),
                bool(response, true, "ok"),
                text(response, "status"),
                text(response, "message")
        );
    }

    @Override
    public LinkConfirmResult confirmAccountLink(String serverName, String requestId, Long telegramId) {
        JsonNode response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getAccountLinkConfirm()),
                Map.of("requestId", requestId, "telegramId", telegramId), JsonNode.class);
        if (!bool(response, true, "ok")) {
            throw new ForcePlayException(defaultMessage(
                    text(response, "message"),
                    "Запрос еще не подтвержден в игре. Подтвердите привязку в игровом окне и попробуйте снова."
            ));
        }
        return new LinkConfirmResult(
                requiredText(response, "externalAccountId", "external_account_id"),
                defaultMessage(text(response, "serverName", "server_name"), serverName),
                text(response, "hwid"),
                linkedCharacters(response.get("linkedCharacters") == null ? response.get("linked_characters") : response.get("linkedCharacters"))
        );
    }

    @Override
    public void confirmHwid(HwidConfirmCommand command) {
        post(command.serverName(), endpoint(command.serverName(), serversProperties.getEndpoints().getHwidConfirm()), command, Void.class);
    }

    @Override
    public HwidUnlinkResult unlinkHwid(String serverName, String externalAccountId, Long externalCharacterId) {
        record Response(boolean unlinked, String message) {}
        Response response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getHwidUnlink()),
                Map.of("externalAccountId", externalAccountId, "externalCharacterId", externalCharacterId), Response.class);
        return new HwidUnlinkResult(externalCharacterId, response.unlinked(), response.message());
    }

    @Override
    public TradeKeyResult changeTradeKey(String serverName, String externalAccountId, Long externalCharacterId, String password) {
        record Response(boolean success, String message) {}
        Response response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getTradeKeyChange()),
                Map.of("externalAccountId", externalAccountId, "externalCharacterId", externalCharacterId, "password", password), Response.class);
        return new TradeKeyResult(externalCharacterId, response.success(), response.message());
    }

    @Override
    public BonusClaimResult claimBonus(String serverName, String externalAccountId, Long externalCharacterId, Long telegramId, long itemId, int itemCount) {
        record Response(boolean success, String message) {}
        Response response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getBonusClaim()),
                Map.of(
                        "externalAccountId", externalAccountId,
                        "externalCharacterId", externalCharacterId,
                        "telegramId", telegramId,
                        "itemId", itemId,
                        "itemCount", itemCount
                ), Response.class);
        return new BonusClaimResult(response.success(), response.message());
    }

    @Override
    public CharacterProfileResult getCharacterProfile(String serverName, String externalAccountId, Long externalCharacterId) {
        record Response(int level, String className, int pvp, int pk, String clanName, boolean online) {}
        Response response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getCharacterProfile()),
                Map.of("externalAccountId", externalAccountId, "externalCharacterId", externalCharacterId), Response.class);
        return new CharacterProfileResult(externalCharacterId, response.level(), response.className(), response.pvp(), response.pk(), response.clanName(), response.online());
    }

    @Override
    public AutofarmStatusResult getAutofarmStatus(String serverName, String externalAccountId, Long externalCharacterId) {
        record Response(boolean activeAutofarm, boolean autofarming, boolean alive, String killerName, String message) {}
        Response response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getAutofarmStatus()),
                Map.of("externalAccountId", externalAccountId, "externalCharacterId", externalCharacterId), Response.class);
        return new AutofarmStatusResult(
                externalCharacterId,
                response.activeAutofarm(),
                response.autofarming(),
                response.alive(),
                response.killerName(),
                response.message()
        );
    }

    @Override
    public CharacterActionResult reviveAutofarm(String serverName, String externalAccountId, Long externalCharacterId) {
        record Response(boolean success, String message) {}
        Response response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getAutofarmRevive()),
                Map.of("externalAccountId", externalAccountId, "externalCharacterId", externalCharacterId), Response.class);
        return new CharacterActionResult(externalCharacterId, response.success(), response.message());
    }

    @Override
    public CharacterActionResult teleportToTown(String serverName, String externalAccountId, Long externalCharacterId) {
        record Response(boolean success, String message) {}
        Response response = post(serverName, endpoint(serverName, serversProperties.getEndpoints().getFunctionTown()),
                Map.of("externalAccountId", externalAccountId, "externalCharacterId", externalCharacterId), Response.class);
        return new CharacterActionResult(externalCharacterId, response.success(), response.message());
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

    private LineageServersProperties.Server resolveServer(String serverName) {
        return serversProperties.findByName(serverName)
                .orElseThrow(() -> new ForcePlayException("Unknown server: " + serverName));
    }

    private String endpoint(String serverName, String template) {
        if (template == null || template.isBlank()) {
            throw new ForcePlayException("Endpoint template must not be blank for server: " + serverName);
        }
        return template.replace("{server}", serverName);
    }

    private List<LinkConfirmResult.LinkedCharacter> linkedCharacters(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<LinkConfirmResult.LinkedCharacter> result = new ArrayList<>();
        for (JsonNode character : node) {
            result.add(new LinkConfirmResult.LinkedCharacter(
                    longValue(character, "externalCharacterId", "external_character_id"),
                    requiredText(character, "name")
            ));
        }
        return result;
    }

    private String requiredText(JsonNode node, String... names) {
        String value = text(node, names);
        if (value == null || value.isBlank()) {
            throw new ForcePlayException("Game server response is missing field: " + String.join("/", names));
        }
        return value;
    }

    private String text(JsonNode node, String... names) {
        if (node == null) {
            return null;
        }
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                String text = value.asText();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private Long longValue(JsonNode node, String... names) {
        if (node == null) {
            return null;
        }
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value.asLong();
            }
        }
        return null;
    }

    private boolean bool(JsonNode node, boolean defaultValue, String... names) {
        if (node == null) {
            return defaultValue;
        }
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value.asBoolean();
            }
        }
        return defaultValue;
    }

    private String defaultMessage(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
