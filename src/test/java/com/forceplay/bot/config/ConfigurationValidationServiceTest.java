package com.forceplay.bot.config;

import com.forceplay.bot.util.ForcePlayException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationValidationServiceTest {

    @Test
    void shouldPassForValidRootConfiguration() {
        ConfigurationValidationService service = new ConfigurationValidationService(validBotProperties(), validServers(), validMessages());

        assertThatCode(service::validate).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenServersListIsEmpty() {
        LineageServersProperties properties = new LineageServersProperties();
        properties.setEndpoints(validEndpoints());
        ConfigurationValidationService service =
                new ConfigurationValidationService(validBotProperties(), properties, validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("servers.yml must define at least one server");
    }

    @Test
    void shouldFailWhenServerNamesAreDuplicated() {
        LineageServersProperties properties = new LineageServersProperties();
        properties.setEndpoints(validEndpoints());
        properties.setServers(java.util.List.of(server("x25_old"), server("X25_OLD")));
        ConfigurationValidationService service = new ConfigurationValidationService(validBotProperties(), properties, validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("servers.yml contains duplicate server names: X25_OLD");
    }

    @Test
    void shouldFailWhenRequiredMessagesAreMissing() {
        MessagesProperties messages = new MessagesProperties();
        messages.setTranslations(Map.of(
                "ru", Map.of("link.requested", "ok"),
                "en", Map.of("link.requested", "ok"),
                "ua", Map.of("link.requested", "ok")
        ));
        ConfigurationValidationService service = new ConfigurationValidationService(validBotProperties(), validServers(), messages);

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("messages.yml is missing required keys for ru: link.success, access.denied, broadcast.done");
    }

    @Test
    void shouldFailWhenBotTokenIsPlaceholder() {
        TelegramBotProperties botProperties = validBotProperties();
        botProperties.setToken("replace-me");
        ConfigurationValidationService service = new ConfigurationValidationService(botProperties, validServers(), validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("BOT_TOKEN must be set to a real Telegram bot token");
    }

    @Test
    void shouldFailWhenChannelUsernameIsInvalid() {
        TelegramBotProperties botProperties = validBotProperties();
        botProperties.setChannelUsername("forceplay");
        ConfigurationValidationService service = new ConfigurationValidationService(botProperties, validServers(), validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("BOT_CHANNEL_USERNAME must start with '@'");
    }

    @Test
    void shouldFailWhenIntegrationModeIsUnsupported() {
        TelegramBotProperties botProperties = validBotProperties();
        botProperties.setIntegrationMode("sandbox");
        ConfigurationValidationService service = new ConfigurationValidationService(botProperties, validServers(), validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("FORCEPLAY_INTEGRATION_MODE must be one of: mock, real");
    }

    @Test
    void shouldFailWhenAdminIdsContainNonPositiveValues() {
        TelegramBotProperties botProperties = validBotProperties();
        botProperties.setAdminIds(java.util.List.of(0L, 77L));
        ConfigurationValidationService service = new ConfigurationValidationService(botProperties, validServers(), validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("ADMIN_IDS must contain only positive Telegram IDs");
    }

    @Test
    void shouldFailWhenDemoDataEnabledWithoutTelegramId() {
        TelegramBotProperties botProperties = validBotProperties();
        botProperties.setDemoDataEnabled(true);
        botProperties.setDemoUserTelegramId(null);
        ConfigurationValidationService service = new ConfigurationValidationService(botProperties, validServers(), validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("DEMO_USER_TELEGRAM_ID must be set to a positive Telegram ID when DEMO_DATA_ENABLED=true");
    }

    @Test
    void shouldFailWhenEndpointIsBlank() {
        LineageServersProperties properties = validServers();
        properties.getEndpoints().setFunctionTown(" ");
        ConfigurationValidationService service = new ConfigurationValidationService(validBotProperties(), properties, validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("servers.yml must define endpoint: function-town");
    }

    @Test
    void shouldFailWhenEndpointDoesNotContainServerPlaceholder() {
        LineageServersProperties properties = validServers();
        properties.getEndpoints().setBonusClaim("/api/bonus/claim");
        ConfigurationValidationService service = new ConfigurationValidationService(validBotProperties(), properties, validMessages());

        assertThatThrownBy(service::validate)
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("Endpoint 'bonus-claim' in servers.yml must contain {server}");
    }

    private TelegramBotProperties validBotProperties() {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.setUsername("forceplay_bot");
        properties.setToken("123456:real-token");
        properties.setChannelUsername("@forceplay");
        properties.setAdminIds(java.util.List.of(100000001L));
        properties.setIntegrationMode("mock");
        properties.setHwidTtlSeconds(120);
        properties.setRateLimitPerMinute(30);
        properties.setReferralItemId(57L);
        properties.setReferralItemCount(1);
        properties.setBonusItemId(57L);
        properties.setBonusItemCount(10);
        properties.setPromoFilePath("promo.txt");
        properties.setPromoDailyCron("0 0 12 * * *");
        properties.setPromoLowStockThreshold(100);
        properties.setAutofarmQuizFilePath("autofarm-quiz.json");
        return properties;
    }

    private LineageServersProperties validServers() {
        LineageServersProperties properties = new LineageServersProperties();
        properties.setEndpoints(validEndpoints());
        properties.setServers(java.util.List.of(server("x25_old")));
        return properties;
    }

    private LineageServersProperties.Endpoints validEndpoints() {
        LineageServersProperties.Endpoints endpoints = new LineageServersProperties.Endpoints();
        endpoints.setAccountLinkRequest("/api/{server}/account/link/request");
        endpoints.setAccountLinkConfirm("/api/{server}/account/link/confirm");
        endpoints.setHwidConfirm("/api/{server}/hwid/confirm");
        endpoints.setHwidUnlink("/api/{server}/hwid/unlink");
        endpoints.setTradeKeyChange("/api/{server}/account/tradekey");
        endpoints.setBonusClaim("/api/{server}/bonus/claim");
        endpoints.setCharacterProfile("/api/{server}/characters/profile");
        endpoints.setAutofarmStatus("/api/{server}/autofarm/status");
        endpoints.setAutofarmRevive("/api/{server}/autofarm/revive");
        endpoints.setFunctionTown("/api/{server}/functions/town");
        return endpoints;
    }

    private MessagesProperties validMessages() {
        MessagesProperties properties = new MessagesProperties();
        Map<String, String> required = Map.of(
                "link.requested", "requested",
                "link.success", "success",
                "access.denied", "denied",
                "broadcast.done", "done"
        );
        properties.setTranslations(Map.of(
                "ru", required,
                "en", required,
                "ua", required
        ));
        return properties;
    }

    private LineageServersProperties.Server server(String name) {
        LineageServersProperties.Server server = new LineageServersProperties.Server();
        server.setName(name);
        server.setBaseUrl("https://example.com/" + name);
        server.setToken("token-" + name);
        return server;
    }
}
