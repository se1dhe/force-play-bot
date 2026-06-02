package com.forceplay.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramGatewayImpl implements TelegramGateway {

    private static final Set<String> SUBSCRIBED_STATUSES = Set.of("creator", "administrator", "member", "restricted");

    private final TelegramClient telegramClient;

    @Override
    public void sendText(Long chatId, String text) {
        sendText(chatId, text, null);
    }

    @Override
    public void sendText(Long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        send(chatId, text, replyMarkup, null);
    }

    @Override
    public void sendHtml(Long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        send(chatId, text, replyMarkup, "HTML");
    }

    private void send(Long chatId, String text, InlineKeyboardMarkup replyMarkup, String parseMode) {
        try {
            SendMessage.SendMessageBuilder builder = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(replyMarkup);
            if (parseMode != null) {
                builder.parseMode(parseMode);
            }
            telegramClient.execute(builder.build());
        } catch (TelegramApiException exception) {
            throw new IllegalStateException("Failed to send telegram message", exception);
        }
    }

    @Override
    public void editText(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup) {
        edit(chatId, messageId, text, replyMarkup, null);
    }

    @Override
    public void editHtml(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup) {
        edit(chatId, messageId, text, replyMarkup, "HTML");
    }

    private void edit(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup, String parseMode) {
        try {
            EditMessageText.EditMessageTextBuilder builder = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(text)
                    .replyMarkup(replyMarkup);
            if (parseMode != null) {
                builder.parseMode(parseMode);
            }
            telegramClient.execute(builder.build());
        } catch (TelegramApiException exception) {
            if (isMessageNotModified(exception)) {
                log.debug("Skipping telegram edit for unmodified message {} in chat {}", messageId, chatId);
                return;
            }
            throw new IllegalStateException("Failed to edit telegram message", exception);
        }
    }

    @Override
    public void answerCallback(String callbackQueryId) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException exception) {
            log.warn("Failed to answer callback {}", callbackQueryId, exception);
        }
    }

    @Override
    public void answerCallback(String callbackQueryId, String text) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text(text)
                    .build());
        } catch (TelegramApiException exception) {
            log.warn("Failed to answer callback {}", callbackQueryId, exception);
        }
    }

    @Override
    public void copyMessage(Long fromChatId, Integer messageId, Long toChatId) {
        try {
            telegramClient.execute(CopyMessage.builder()
                    .fromChatId(fromChatId.toString())
                    .chatId(toChatId.toString())
                    .messageId(messageId)
                    .build());
        } catch (TelegramApiException exception) {
            log.warn("Failed to copy message {} from {} to {}", messageId, fromChatId, toChatId, exception);
        }
    }

    @Override
    public boolean isSubscribed(Long telegramId, String channelUsername) {
        try {
            String status = telegramClient.execute(GetChatMember.builder()
                            .chatId(channelUsername)
                            .userId(telegramId)
                            .build())
                    .getStatus();
            return SUBSCRIBED_STATUSES.contains(status);
        } catch (TelegramApiException exception) {
            log.warn("Cannot verify subscription for {}", telegramId, exception);
            return false;
        }
    }

    private boolean isMessageNotModified(TelegramApiException exception) {
        String message = exception.getMessage();
        return message != null && message.contains("message is not modified");
    }
}
