package com.forceplay.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
public class UpdateUserContextService {

    private final UserService userService;

    public UpdateUserContextService(UserService userService) {
        this.userService = userService;
    }

    public void registerFromUpdate(Update update) {
        resolveUser(update).ifPresent(user ->
                userService.getOrCreateUser(user.getId(), user.getLanguageCode()));
    }

    private java.util.Optional<User> resolveUser(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return java.util.Optional.of(update.getMessage().getFrom());
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return java.util.Optional.of(update.getCallbackQuery().getFrom());
        }
        return java.util.Optional.empty();
    }
}
