package com.forceplay.bot.handler;

import com.forceplay.bot.service.InlineMenuService;
import com.forceplay.bot.service.ReferralService;
import com.forceplay.bot.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StartCommandHandlerTest {

    @Mock
    private ReferralService referralService;
    @Mock
    private InlineMenuService inlineMenuService;
    @Mock
    private UserService userService;

    @Test
    void shouldSupportStartCommand() {
        StartCommandHandler handler = new StartCommandHandler(referralService, inlineMenuService, userService);

        assertThat(handler.supports(updateWithMessage("/start"))).isTrue();
        assertThat(handler.supports(updateWithMessage("/start ref_123"))).isTrue();
        assertThat(handler.supports(updateWithMessage("/help"))).isFalse();
    }

    @Test
    void shouldCreateUserRegisterReferralAndShowMenu() {
        StartCommandHandler handler = new StartCommandHandler(referralService, inlineMenuService, userService);
        com.forceplay.bot.model.User user = com.forceplay.bot.model.User.builder().telegramId(777L).language("ru").languageSelected(true).build();
        when(userService.getOrCreateUser(777L, "ru")).thenReturn(user);
        User from = new User(777L, "se1dhe", false);
        from.setLanguageCode("ru");
        Message message = new Message();
        Chat chat = new Chat(900L, "private");
        message.setFrom(from);
        message.setText("/start ref_abc");
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        handler.handle(update);

        verify(userService).getOrCreateUser(777L, "ru");
        verify(referralService).registerReferral(777L, "ru", "ref_abc");
        verify(inlineMenuService).showMainMenu(900L, 777L);
    }

    @Test
    void shouldPassNullPayloadForPlainStart() {
        StartCommandHandler handler = new StartCommandHandler(referralService, inlineMenuService, userService);
        com.forceplay.bot.model.User user = com.forceplay.bot.model.User.builder().telegramId(555L).language("en").languageSelected(true).build();
        when(userService.getOrCreateUser(555L, "en")).thenReturn(user);
        User from = new User(555L, "plain", false);
        from.setLanguageCode("en");
        Message message = new Message();
        Chat chat = new Chat(901L, "private");
        message.setFrom(from);
        message.setText("/start");
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        handler.handle(update);

        verify(referralService).registerReferral(555L, "en", null);
    }

    @Test
    void shouldShowLanguageSelectionForNewUserWithoutExplicitLanguage() {
        StartCommandHandler handler = new StartCommandHandler(referralService, inlineMenuService, userService);
        com.forceplay.bot.model.User user = com.forceplay.bot.model.User.builder().telegramId(888L).language("ru").languageSelected(false).build();
        when(userService.getOrCreateUser(888L, "ru")).thenReturn(user);
        User from = new User(888L, "newbie", false);
        from.setLanguageCode("ru");
        Message message = new Message();
        Chat chat = new Chat(902L, "private");
        message.setFrom(from);
        message.setText("/start");
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        handler.handle(update);

        verify(inlineMenuService).showLanguageMenu(902L, 888L);
    }

    @Test
    void shouldIgnoreNonStartCommandsInSupportsOnly() {
        StartCommandHandler handler = new StartCommandHandler(referralService, inlineMenuService, userService);

        if (handler.supports(updateWithMessage("/help"))) {
            handler.handle(updateWithMessage("/help"));
        }

        verifyNoInteractions(referralService, inlineMenuService, userService);
    }

    private Update updateWithMessage(String text) {
        Message message = new Message();
        message.setText(text);
        Update update = new Update();
        update.setMessage(message);
        return update;
    }
}
