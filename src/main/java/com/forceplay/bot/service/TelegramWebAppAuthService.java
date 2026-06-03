package com.forceplay.bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.dto.tarot.TelegramWebAppUser;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class TelegramWebAppAuthService {

    private static final long MAX_AUTH_AGE_SECONDS = 86_400;

    private final TelegramBotProperties properties;
    private final ObjectMapper objectMapper;

    public TelegramWebAppUser authenticate(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new ForcePlayException("Откройте приложение через Telegram.");
        }

        MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString("https://forceplay.local/?" + initData)
                .build()
                .getQueryParams();
        String hash = params.getFirst("hash");
        if (hash == null || hash.isBlank()) {
            throw new ForcePlayException("Некорректная подпись Telegram Mini App.");
        }

        validateAge(params.getFirst("auth_date"));
        validateHash(initData, hash);

        String userJson = params.getFirst("user");
        if (userJson == null || userJson.isBlank()) {
            throw new ForcePlayException("Telegram не передал данные пользователя.");
        }
        return parseUser(userJson);
    }

    private void validateAge(String authDate) {
        if (authDate == null || authDate.isBlank()) {
            throw new ForcePlayException("Telegram auth_date отсутствует.");
        }
        long authenticatedAt;
        try {
            authenticatedAt = Long.parseLong(authDate);
        } catch (NumberFormatException exception) {
            throw new ForcePlayException("Telegram auth_date некорректен.");
        }
        long age = Instant.now().getEpochSecond() - authenticatedAt;
        if (age < 0 || age > MAX_AUTH_AGE_SECONDS) {
            throw new ForcePlayException("Сессия Telegram Mini App устарела. Откройте приложение заново.");
        }
    }

    private void validateHash(String initData, String receivedHash) {
        try {
            Map<String, String> checkData = new TreeMap<>();
            for (String pair : initData.split("&")) {
                int separator = pair.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8);
                if ("hash".equals(key)) {
                    continue;
                }
                String value = URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8);
                checkData.put(key, value);
            }

            String dataCheckString = checkData.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((left, right) -> left + "\n" + right)
                    .orElse("");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] secret = mac.doFinal(properties.getToken().getBytes(StandardCharsets.UTF_8));
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            String calculated = HexFormat.of().formatHex(mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(calculated.getBytes(StandardCharsets.UTF_8), receivedHash.getBytes(StandardCharsets.UTF_8))) {
                throw new ForcePlayException("Подпись Telegram Mini App не прошла проверку.");
            }
        } catch (ForcePlayException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ForcePlayException("Не удалось проверить Telegram Mini App.");
        }
    }

    private TelegramWebAppUser parseUser(String userJson) {
        try {
            JsonNode node = objectMapper.readTree(userJson);
            Long id = node.path("id").isNumber() ? node.path("id").asLong() : null;
            if (id == null) {
                throw new ForcePlayException("Telegram user.id отсутствует.");
            }
            return new TelegramWebAppUser(
                    id,
                    text(node, "first_name"),
                    text(node, "last_name"),
                    text(node, "username"),
                    text(node, "language_code")
            );
        } catch (ForcePlayException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ForcePlayException("Не удалось прочитать пользователя Telegram.");
        }
    }

    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }
}
