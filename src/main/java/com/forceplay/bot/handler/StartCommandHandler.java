package com.forceplay.bot.handler;

import com.forceplay.bot.service.InlineMenuService;
import com.forceplay.bot.service.ReferralService;
import com.forceplay.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@Order(5)
@RequiredArgsConstructor
public class StartCommandHandler implements UpdateHandler {

    private final ReferralService referralService;
    private final InlineMenuService inlineMenuService;
    private final UserService userService;

    @Override
    public boolean supports(Update update) {
        return update.hasMessage() && isCommand(update.getMessage(), "/start");
    }

    @Override
    public void handle(Update update) {
        Message message = update.getMessage();
        String[] parts = message.getText().split("\\s+", 2);
        String payload = parts.length > 1 ? parts[1] : null;
        userService.getOrCreateUser(message.getFrom().getId(), message.getFrom().getLanguageCode());
        referralService.registerReferral(message.getFrom().getId(), message.getFrom().getLanguageCode(), payload);
        inlineMenuService.showMainMenu(message.getChatId(), message.getFrom().getId());
    }

    private boolean isCommand(Message message, String command) {
        return message.hasText() && message.getText().startsWith(command);
    }
}
