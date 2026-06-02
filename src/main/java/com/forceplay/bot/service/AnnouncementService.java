package com.forceplay.bot.service;

import com.forceplay.bot.dto.AnnouncementType;
import com.forceplay.bot.dto.AnnouncementWebhookRequest;
import com.forceplay.bot.model.User;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final UserService userService;
    private final TelegramGateway telegramGateway;
    private final RedisStateService redisStateService;
    private final MessageResolver messageResolver;

    public void announce(AnnouncementWebhookRequest request) {
        String dedupeKey = request.dedupeKey() == null || request.dedupeKey().isBlank()
                ? defaultDedupeKey(request)
                : request.dedupeKey().trim();

        if (!redisStateService.registerIdempotency("forceplay:announce:" + dedupeKey, Duration.ofHours(12))) {
            return;
        }

        List<User> recipients = request.type() == AnnouncementType.BOSS_SPAWN
                ? userService.getBossAnnouncementSubscribers()
                : request.type() == AnnouncementType.EVENT_REGISTRATION
                ? userService.getEventAnnouncementSubscribers()
                : userService.getServerRestartSubscribers();

        recipients.forEach(user -> telegramGateway.sendText(user.getTelegramId(), renderAnnouncementText(user.getLanguage(), request)));
    }

    private String renderAnnouncementText(String language, AnnouncementWebhookRequest request) {
        return switch (request.type()) {
            case BOSS_SPAWN -> messageResolver.get(language, "announce.boss.spawn", "Скоро спавн РБ\n%s")
                    .formatted(renderBossBody(language, request));
            case EVENT_REGISTRATION -> messageResolver.get(language, "announce.event.start", "Скоро начало ивента\n%s")
                    .formatted(renderEventBody(language, request));
            case SERVER_RESTART -> messageResolver.get(language, "announce.server.restart", "Рестарт сервера\n%s")
                    .formatted(renderRestartBody(language, request));
        };
    }

    private String renderBossBody(String language, AnnouncementWebhookRequest request) {
        return messageResolver.get(language, "notifications.boss.template", "Сервер: %s\nРБ: %s\nСпавн: %s")
                .formatted(
                        request.serverName(),
                        request.title(),
                        safeScheduledAt(request.scheduledAt())
                );
    }

    private String renderEventBody(String language, AnnouncementWebhookRequest request) {
        return messageResolver.get(language, "notifications.event.template", "Сервер: %s\nИвент: %s\nСтарт: %s")
                .formatted(
                        request.serverName(),
                        request.title(),
                        safeScheduledAt(request.scheduledAt())
                );
    }

    private String renderRestartBody(String language, AnnouncementWebhookRequest request) {
        return messageResolver.get(language, "notifications.restart.template", "Сервер: %s\nСобытие: %s\nВремя: %s")
                .formatted(
                        request.serverName(),
                        request.title(),
                        safeScheduledAt(request.scheduledAt())
                );
    }

    private String safeScheduledAt(String scheduledAt) {
        return scheduledAt == null || scheduledAt.isBlank() ? "-" : scheduledAt.trim();
    }

    private String defaultDedupeKey(AnnouncementWebhookRequest request) {
        return "%s:%s:%s:%s".formatted(
                request.type().name(),
                request.serverName(),
                request.title(),
                safeScheduledAt(request.scheduledAt())
        );
    }
}
