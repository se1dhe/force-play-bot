package com.forceplay.bot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramGatewayImplTest {

    @Mock
    private TelegramClient telegramClient;

    @InjectMocks
    private TelegramGatewayImpl telegramGateway;

    @Test
    void shouldReturnTrueForMemberStatus() throws TelegramApiException {
        when(telegramClient.execute(any(GetChatMember.class))).thenReturn(member("member"));

        boolean subscribed = telegramGateway.isSubscribed(77L, "@forceplay");

        assertThat(subscribed).isTrue();
    }

    @Test
    void shouldReturnFalseForLeftStatus() throws TelegramApiException {
        when(telegramClient.execute(any(GetChatMember.class))).thenReturn(member("left"));

        boolean subscribed = telegramGateway.isSubscribed(77L, "@forceplay");

        assertThat(subscribed).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTelegramApiFails() throws TelegramApiException {
        when(telegramClient.execute(any(GetChatMember.class))).thenThrow(new TelegramApiException("boom"));

        boolean subscribed = telegramGateway.isSubscribed(77L, "@forceplay");

        assertThat(subscribed).isFalse();
    }

    @Test
    void shouldIgnoreEditWhenTelegramSaysMessageNotModified() throws TelegramApiException {
        when(telegramClient.execute(any(EditMessageText.class)))
                .thenThrow(new TelegramApiException("Bad Request: message is not modified"));

        assertThatCode(() -> telegramGateway.editText(77L, 10, "same", new InlineKeyboardMarkup(java.util.List.of(new InlineKeyboardRow()))))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSendHtmlMessagesWithHtmlParseMode() throws TelegramApiException {
        when(telegramClient.execute(any(SendMessage.class))).thenReturn(null);

        telegramGateway.sendHtml(77L, "<code>CODE123</code>", new InlineKeyboardMarkup(java.util.List.of(new InlineKeyboardRow())));

        org.mockito.ArgumentCaptor<SendMessage> captor = org.mockito.ArgumentCaptor.forClass(SendMessage.class);
        org.mockito.Mockito.verify(telegramClient).execute(captor.capture());
        assertThat(captor.getValue().getParseMode()).isEqualTo("HTML");
    }

    private ChatMember member(String status) {
        User user = new User(77L, "se1dhe", false);
        return switch (status) {
            case "left" -> new ChatMemberLeft(user);
            case "member" -> new ChatMemberMember(user);
            default -> new ChatMember() {
                @Override
                public String getStatus() {
                    return status;
                }

                @Override
                public User getUser() {
                    return user;
                }
            };
        };
    }
}
