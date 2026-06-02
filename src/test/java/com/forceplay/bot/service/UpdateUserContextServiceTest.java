package com.forceplay.bot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateUserContextServiceTest {

    @Mock
    private UserService userService;

    @Test
    void shouldRegisterUserFromMessageUpdate() {
        UpdateUserContextService service = new UpdateUserContextService(userService);
        User user = new User(100L, "se1dhe", false);
        user.setLanguageCode("ru");
        Message message = new Message();
        message.setFrom(user);
        Update update = new Update();
        update.setMessage(message);

        service.registerFromUpdate(update);

        verify(userService).getOrCreateUser(100L, "ru");
    }

    @Test
    void shouldRegisterUserFromCallbackUpdate() {
        UpdateUserContextService service = new UpdateUserContextService(userService);
        User user = new User(200L, "admin", false);
        user.setLanguageCode("en");
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        Update update = new Update();
        update.setCallbackQuery(callbackQuery);

        service.registerFromUpdate(update);

        verify(userService).getOrCreateUser(200L, "en");
    }

    @Test
    void shouldIgnoreUpdateWithoutTelegramUser() {
        UpdateUserContextService service = new UpdateUserContextService(userService);

        service.registerFromUpdate(new Update());

        verify(userService, never()).getOrCreateUser(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }
}
