package com.forceplay.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    boolean supports(Update update);

    void handle(Update update);
}
