package com.forceplay.bot.config;

import com.forceplay.bot.util.ForcePlayException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ConfigurationValidationService {

    private static final List<String> REQUIRED_MESSAGE_KEYS = List.of(
            "link.requested",
            "link.success",
            "access.denied",
            "broadcast.done"
    );
    private static final List<String> SUPPORTED_LANGUAGES = List.of("ru", "en", "ua");

    private final TelegramBotProperties botProperties;
    private final LineageServersProperties serversProperties;
    private final MessagesProperties messagesProperties;

    @PostConstruct
    public void validate() {
        validateBotProperties();
        validateServers();
        validateMessages();
    }

    private void validateBotProperties() {
        if (normalize(botProperties.getUsername()) == null) {
            throw new ForcePlayException("BOT_USERNAME must not be blank");
        }
        if (normalize(botProperties.getToken()) == null || "replace-me".equals(botProperties.getToken().trim())) {
            throw new ForcePlayException("BOT_TOKEN must be set to a real Telegram bot token");
        }
        String channelUsername = normalize(botProperties.getChannelUsername());
        if (channelUsername == null || !channelUsername.startsWith("@") || channelUsername.length() < 2) {
            throw new ForcePlayException("BOT_CHANNEL_USERNAME must start with '@'");
        }
        if (!List.of("mock", "real").contains(normalize(botProperties.getIntegrationMode()))) {
            throw new ForcePlayException("FORCEPLAY_INTEGRATION_MODE must be one of: mock, real");
        }
        if (botProperties.getHwidTtlSeconds() <= 0) {
            throw new ForcePlayException("HWID_TTL_SECONDS must be greater than 0");
        }
        if (botProperties.getRateLimitPerMinute() <= 0) {
            throw new ForcePlayException("RATE_LIMIT_PER_MINUTE must be greater than 0");
        }
        if (botProperties.getReferralItemId() <= 0) {
            throw new ForcePlayException("REFERRAL_ITEM_ID must be greater than 0");
        }
        if (botProperties.getReferralItemCount() <= 0) {
            throw new ForcePlayException("REFERRAL_ITEM_COUNT must be greater than 0");
        }
        if (botProperties.getBonusItemId() <= 0) {
            throw new ForcePlayException("BONUS_ITEM_ID must be greater than 0");
        }
        if (botProperties.getBonusItemCount() <= 0) {
            throw new ForcePlayException("BONUS_ITEM_COUNT must be greater than 0");
        }
        if (normalize(botProperties.getPromoFilePath()) == null) {
            throw new ForcePlayException("PROMO_FILE_PATH must not be blank");
        }
        if (normalize(botProperties.getPromoDailyCron()) == null) {
            throw new ForcePlayException("PROMO_DAILY_CRON must not be blank");
        }
        if (botProperties.getPromoLowStockThreshold() <= 0) {
            throw new ForcePlayException("PROMO_LOW_STOCK_THRESHOLD must be greater than 0");
        }
        if (normalize(botProperties.getAutofarmQuizFilePath()) == null) {
            throw new ForcePlayException("AUTOFARM_QUIZ_FILE_PATH must not be blank");
        }
        boolean invalidAdminIdPresent = botProperties.getAdminIds().stream()
                .anyMatch(adminId -> adminId == null || adminId <= 0);
        if (invalidAdminIdPresent) {
            throw new ForcePlayException("ADMIN_IDS must contain only positive Telegram IDs");
        }
        if (botProperties.isDemoDataEnabled() && (botProperties.getDemoUserTelegramId() == null || botProperties.getDemoUserTelegramId() <= 0)) {
            throw new ForcePlayException("DEMO_USER_TELEGRAM_ID must be set to a positive Telegram ID when DEMO_DATA_ENABLED=true");
        }
    }

    private void validateServers() {
        if (serversProperties.getServers().isEmpty()) {
            throw new ForcePlayException("servers.yml must define at least one server");
        }
        validateEndpoints();

        Set<String> seenNames = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        serversProperties.getServers().forEach(server -> {
            String name = normalize(server.getName());
            if (name == null) {
                throw new ForcePlayException("servers.yml contains a server with empty name");
            }
            if (normalize(server.getBaseUrl()) == null) {
                throw new ForcePlayException("Server '%s' in servers.yml must define base-url".formatted(server.getName()));
            }
            if (normalize(server.getToken()) == null) {
                throw new ForcePlayException("Server '%s' in servers.yml must define token".formatted(server.getName()));
            }
            if (!seenNames.add(name.toLowerCase())) {
                duplicates.add(server.getName());
            }
        });

        if (!duplicates.isEmpty()) {
            throw new ForcePlayException("servers.yml contains duplicate server names: " + String.join(", ", duplicates));
        }
    }

    private void validateEndpoints() {
        LineageServersProperties.Endpoints endpoints = serversProperties.getEndpoints();
        if (endpoints == null) {
            throw new ForcePlayException("servers.yml must define lineage endpoints");
        }
        validateEndpoint("account-link-request", endpoints.getAccountLinkRequest());
        validateEndpoint("account-link-confirm", endpoints.getAccountLinkConfirm());
        validateEndpoint("hwid-confirm", endpoints.getHwidConfirm());
        validateEndpoint("hwid-unlink", endpoints.getHwidUnlink());
        validateEndpoint("trade-key-change", endpoints.getTradeKeyChange());
        validateEndpoint("bonus-claim", endpoints.getBonusClaim());
        validateEndpoint("character-profile", endpoints.getCharacterProfile());
        validateEndpoint("autofarm-status", endpoints.getAutofarmStatus());
        validateEndpoint("autofarm-revive", endpoints.getAutofarmRevive());
        validateEndpoint("function-town", endpoints.getFunctionTown());
    }

    private void validateEndpoint(String name, String path) {
        String normalized = normalize(path);
        if (normalized == null) {
            throw new ForcePlayException("servers.yml must define endpoint: " + name);
        }
        if (!normalized.startsWith("/")) {
            throw new ForcePlayException("Endpoint '%s' in servers.yml must start with '/'".formatted(name));
        }
        if (!normalized.contains("{server}")) {
            throw new ForcePlayException("Endpoint '%s' in servers.yml must contain {server}".formatted(name));
        }
    }

    private void validateMessages() {
        for (String language : SUPPORTED_LANGUAGES) {
            if (messagesProperties.getTranslations().get(language) == null) {
                throw new ForcePlayException("messages.yml is missing language section: " + language);
            }
            List<String> missingKeys = REQUIRED_MESSAGE_KEYS.stream()
                    .filter(key -> normalize(messagesProperties.getTranslations().get(language).get(key)) == null)
                    .toList();
            if (!missingKeys.isEmpty()) {
                throw new ForcePlayException("messages.yml is missing required keys for " + language + ": " + String.join(", ", missingKeys));
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
