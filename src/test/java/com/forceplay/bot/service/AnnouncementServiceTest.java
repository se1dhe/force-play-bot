package com.forceplay.bot.service;

import com.forceplay.bot.dto.AnnouncementType;
import com.forceplay.bot.dto.AnnouncementWebhookRequest;
import com.forceplay.bot.model.User;
import com.forceplay.bot.util.MessageResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private TelegramGateway telegramGateway;
    @Mock
    private RedisStateService redisStateService;
    @Mock
    private MessageResolver messageResolver;

    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
        lenient().when(messageResolver.get(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(messageResolver.get(anyString(), anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(2));
        announcementService = new AnnouncementService(userService, telegramGateway, redisStateService, messageResolver);
    }

    @Test
    void shouldSendBossAnnouncementToSubscribedUsers() {
        when(redisStateService.registerIdempotency(eq("forceplay:announce:boss-1"), any(Duration.class))).thenReturn(true);
        when(userService.getBossAnnouncementSubscribers()).thenReturn(List.of(user(101L), user(102L)));

        announcementService.announce(new AnnouncementWebhookRequest(
                AnnouncementType.BOSS_SPAWN,
                "x25_old",
                "Antharas",
                "2026-04-07T21:00:00Z",
                "boss-1"
        ));

        verify(telegramGateway).sendText(101L, "Скоро спавн РБ\nСервер: x25_old\nРБ: Antharas\nСпавн: 2026-04-07T21:00:00Z");
        verify(telegramGateway).sendText(102L, "Скоро спавн РБ\nСервер: x25_old\nРБ: Antharas\nСпавн: 2026-04-07T21:00:00Z");
    }

    @Test
    void shouldSendEventAnnouncementToSubscribedUsers() {
        when(redisStateService.registerIdempotency(eq("forceplay:announce:event-1"), any(Duration.class))).thenReturn(true);
        when(userService.getEventAnnouncementSubscribers()).thenReturn(List.of(user(201L)));

        announcementService.announce(new AnnouncementWebhookRequest(
                AnnouncementType.EVENT_REGISTRATION,
                "x25_old",
                "TvT",
                "2026-04-07T20:00:00Z",
                "event-1"
        ));

        verify(telegramGateway).sendText(201L, "Скоро начало ивента\nСервер: x25_old\nИвент: TvT\nСтарт: 2026-04-07T20:00:00Z");
    }

    @Test
    void shouldSkipDuplicateAnnouncement() {
        when(redisStateService.registerIdempotency(anyString(), any(Duration.class))).thenReturn(false);

        announcementService.announce(new AnnouncementWebhookRequest(
                AnnouncementType.BOSS_SPAWN,
                "x25_old",
                "Antharas",
                "2026-04-07T21:00:00Z",
                "boss-1"
        ));

        verify(userService, never()).getBossAnnouncementSubscribers();
        verify(telegramGateway, never()).sendText(any(), anyString());
    }

    private static User user(Long telegramId) {
        return User.builder()
                .telegramId(telegramId)
                .language("ru")
                .build();
    }
}
