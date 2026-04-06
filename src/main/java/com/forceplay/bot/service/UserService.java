package com.forceplay.bot.service;

import com.forceplay.bot.model.User;

public interface UserService {
    User getOrCreateUser(Long telegramId, String languageCode);
}
