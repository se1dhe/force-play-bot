package com.forceplay.bot.service;

import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TelegramBotProperties botProperties;
    private final UserRepository userRepository;

    public boolean isAdmin(Long telegramId) {
        return botProperties.getAdminIds().contains(telegramId);
    }

    public java.util.List<Long> getBroadcastTargets() {
        return userRepository.findAll().stream()
                .map(user -> user.getTelegramId())
                .toList();
    }

    public java.util.List<Long> getAdminIds() {
        return botProperties.getAdminIds();
    }
}
