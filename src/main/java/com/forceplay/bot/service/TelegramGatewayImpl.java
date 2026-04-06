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

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramGatewayImpl implements TelegramGateway {

    private final TelegramClient telegramClient;

    @Override
    public void sendText(Long chatId, String text) {
        sendText(chatId, text, null);
    }

    @Override
    public void sendText(Long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(replyMarkup)
                    .build());
        } catch (TelegramApiException exception) {
            throw new IllegalStateException("Failed to send telegram message", exception);
        }
    }

    @Override
    public void editText(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup) {
        try {
            telegramClient.execute(EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(text)
                    .replyMarkup(replyMarkup)
                    .build());
        } catch (TelegramApiException exception) {
            throw new IllegalStateException("Failed to edit telegram message", exception);
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
            return telegramClient.execute(GetChatMember.builder()
                            .chatId(channelUsername)
                            .userId(telegramId)
                            .build())
                    .getStatus() != null;
        } catch (TelegramApiException exception) {
            log.warn("Cannot verify subscription for {}", telegramId, exception);
            return false;
        }
    }
}
