package com.forceplay.bot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface TelegramGateway {
    void sendText(Long chatId, String text);

    void sendText(Long chatId, String text, InlineKeyboardMarkup replyMarkup);

    void sendHtml(Long chatId, String text, InlineKeyboardMarkup replyMarkup);

    void editText(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup);

    void editHtml(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup);

    void answerCallback(String callbackQueryId);

    void answerCallback(String callbackQueryId, String text);

    void copyMessage(Long fromChatId, Integer messageId, Long toChatId);

    boolean isSubscribed(Long telegramId, String channelUsername);
}
