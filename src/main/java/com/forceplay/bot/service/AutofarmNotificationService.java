package com.forceplay.bot.service;

import com.forceplay.bot.dto.AutofarmDeathWebhookRequest;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.repository.GameCharacterRepository;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AutofarmNotificationService {

    private final GameCharacterRepository gameCharacterRepository;
    private final TelegramGateway telegramGateway;
    private final RedisStateService redisStateService;
    private final MessageResolver messageResolver;

    public void notifyDeath(AutofarmDeathWebhookRequest request) {
        String dedupeKey = request.dedupeKey() == null || request.dedupeKey().isBlank()
                ? "%s:%s".formatted(request.serverName(), request.externalCharacterId())
                : request.dedupeKey().trim();
        if (!redisStateService.registerIdempotency("forceplay:autofarm-death:" + dedupeKey, Duration.ofHours(12))) {
            return;
        }

        gameCharacterRepository.findByExternalCharacterIdAndAccountServerName(request.externalCharacterId(), request.serverName())
                .filter(character -> character.getAccount().getUser().isAnnounceAutofarmDeath())
                .ifPresent(character -> telegramGateway.sendText(
                        character.getAccount().getUser().getTelegramId(),
                        renderDeathText(character, request)
                ));
    }

    private String renderDeathText(GameCharacter character, AutofarmDeathWebhookRequest request) {
        String language = character.getAccount().getUser().getLanguage();
        return messageResolver.get(language, "notifications.autofarm.death.template", "Автофарм — убили\n\nСервер: %s\nПерсонаж: %s\nУбийца: %s")
                .formatted(
                        request.serverName(),
                        character.getName(),
                        request.killerName() == null || request.killerName().isBlank() ? "-" : request.killerName().trim()
                );
    }
}
