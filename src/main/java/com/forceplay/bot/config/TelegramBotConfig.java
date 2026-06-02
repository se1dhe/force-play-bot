package com.forceplay.bot.config;

import com.forceplay.bot.handler.UpdateHandler;
import com.forceplay.bot.service.RedisStateService;
import com.forceplay.bot.service.UpdateUserContextService;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final TelegramBotProperties properties;
    private final List<UpdateHandler> handlers;
    private final RedisStateService redisStateService;
    private final UpdateUserContextService updateUserContextService;

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "forceplay.bot.polling-enabled", havingValue = "true", matchIfMissing = true)
    public TelegramBotsLongPollingApplication telegramBotsApplication(LongPollingUpdateConsumer updateConsumer) throws TelegramApiException {
        TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication();
        application.registerBot(properties.getToken(), updateConsumer);
        return application;
    }

    @Bean
    public LongPollingUpdateConsumer longPollingUpdateConsumer() {
        return new LongPollingSingleThreadUpdateConsumer() {
            @Override
            public void consume(Update update) {
                processUpdate(update);
            }
        };
    }

    void processUpdate(Update update) {
        try {
            updateUserContextService.registerFromUpdate(update);

            Long telegramId = extractTelegramId(update);
            if (telegramId != null) {
                boolean allowed = redisStateService.tryAcquireRateLimit(
                        "forceplay:ratelimit:" + telegramId,
                        Duration.ofMinutes(1),
                        properties.getRateLimitPerMinute()
                );
                if (!allowed) {
                    sendSafe(resolveChatId(update), "Too many requests.");
                    return;
                }
            }

            handlers.stream()
                    .filter(handler -> handler.supports(update))
                    .findFirst()
                    .ifPresent(handler -> handler.handle(update));
        } catch (ForcePlayException exception) {
            log.warn("Telegram update rejected: {}", exception.getMessage());
            sendSafe(resolveChatId(update), exception.getMessage());
        } catch (Exception exception) {
            log.error("Failed to process Telegram update: {}", update, exception);
            sendSafe(resolveChatId(update), "Внутренняя ошибка. Попробуйте ещё раз.");
        }
    }

    private Long resolveChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getMessage() != null) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }

    private Long extractTelegramId(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return update.getMessage().getFrom().getId();
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return update.getCallbackQuery().getFrom().getId();
        }
        return null;
    }

    void sendSafe(Long chatId, String text) {
        if (chatId == null) {
            return;
        }
        try {
            new org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient(properties.getToken())
                    .execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (TelegramApiException exception) {
            log.error("Telegram send failed", exception);
        }
    }
}
