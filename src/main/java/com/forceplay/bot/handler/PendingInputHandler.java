package com.forceplay.bot.handler;

import com.forceplay.bot.service.InlineMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Order(20)
@RequiredArgsConstructor
public class PendingInputHandler implements UpdateHandler {

    private final InlineMenuService inlineMenuService;

    @Override
    public boolean supports(Update update) {
        if (!update.hasMessage() || update.getMessage().getFrom() == null) {
            return false;
        }
        if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
            return false;
        }
        return inlineMenuService.hasPendingInput(update.getMessage().getFrom().getId());
    }

    @Override
    public void handle(Update update) {
        inlineMenuService.handlePendingInput(update.getMessage());
    }
}
