package com.forceplay.bot.handler;

import com.forceplay.bot.service.InlineMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
public class FallbackHandler implements UpdateHandler {

    private final InlineMenuService inlineMenuService;

    @Override
    public boolean supports(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    @Override
    public void handle(Update update) {
        inlineMenuService.showMainMenu(update.getMessage().getChatId(), update.getMessage().getFrom().getId());
    }
}
