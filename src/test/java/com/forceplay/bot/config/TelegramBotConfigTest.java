package com.forceplay.bot.config;

import com.forceplay.bot.handler.UpdateHandler;
import com.forceplay.bot.service.RedisStateService;
import com.forceplay.bot.service.UpdateUserContextService;
import com.forceplay.bot.util.ForcePlayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class TelegramBotConfigTest {

    @Mock
    private TelegramBotProperties properties;
    @Mock
    private UpdateHandler updateHandler;
    @Mock
    private RedisStateService redisStateService;
    @Mock
    private UpdateUserContextService updateUserContextService;

    @Test
    void shouldSendBusinessErrorMessageToUser() {
        when(properties.getRateLimitPerMinute()).thenReturn(10);
        when(updateHandler.supports(any(Update.class))).thenReturn(true);
        when(redisStateService.tryAcquireRateLimit(anyString(), any(), anyLong()))
                .thenReturn(true);
        doThrow(new ForcePlayException("Введите текущий пароль.")).when(updateHandler).handle(any(Update.class));

        TelegramBotConfig config = org.mockito.Mockito.spy(new TelegramBotConfig(
                properties,
                List.of(updateHandler),
                redisStateService,
                updateUserContextService
        ));
        doNothing().when(config).sendSafe(anyLong(), anyString());

        config.processUpdate(update(100L, 77L, "payload"));

        verify(config).sendSafe(100L, "Введите текущий пароль.");
    }

    @Test
    void shouldSendGenericErrorForUnexpectedException() {
        when(properties.getRateLimitPerMinute()).thenReturn(10);
        when(updateHandler.supports(any(Update.class))).thenReturn(true);
        when(redisStateService.tryAcquireRateLimit(anyString(), any(), anyLong()))
                .thenReturn(true);
        doThrow(new IllegalStateException("boom")).when(updateHandler).handle(any(Update.class));

        TelegramBotConfig config = org.mockito.Mockito.spy(new TelegramBotConfig(
                properties,
                List.of(updateHandler),
                redisStateService,
                updateUserContextService
        ));
        doNothing().when(config).sendSafe(anyLong(), anyString());

        config.processUpdate(update(100L, 77L, "payload"));

        verify(config).sendSafe(100L, "Внутренняя ошибка. Попробуйте ещё раз.");
    }

    private Update update(Long chatId, Long telegramId, String text) {
        org.telegram.telegrambots.meta.api.objects.User from =
                new org.telegram.telegrambots.meta.api.objects.User(telegramId, "user" + telegramId, false);
        Message message = new Message();
        message.setChat(new Chat(chatId, "private"));
        message.setFrom(from);
        message.setText(text);

        Update update = new Update();
        update.setMessage(message);
        return update;
    }
}
