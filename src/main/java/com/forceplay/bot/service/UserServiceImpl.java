package com.forceplay.bot.service;

import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User getOrCreateUser(Long telegramId, String languageCode) {
        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .telegramId(telegramId)
                        .language(languageCode == null ? "ru" : languageCode)
                        .createdAt(OffsetDateTime.now())
                        .build()));
    }
}
