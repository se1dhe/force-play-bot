package com.forceplay.bot.service;

import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.UserRepository;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MessageResolver messageResolver;

    @Override
    @Transactional
    public User getOrCreateUser(Long telegramId, String languageCode) {
        return getOrCreateUser(telegramId, languageCode, null, null, null);
    }

    @Override
    @Transactional
    public User getOrCreateUser(Long telegramId, String languageCode, String username, String firstName, String lastName) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .telegramId(telegramId)
                        .language(messageResolver.normalizeLanguage(languageCode))
                        .createdAt(OffsetDateTime.now())
                        .build()));

        boolean updated = false;
        if (username != null && !username.equals(user.getUsername())) {
            user.setUsername(username);
            updated = true;
        }
        if (firstName != null && !firstName.equals(user.getFirstName())) {
            user.setFirstName(firstName);
            updated = true;
        }
        if (lastName != null && !lastName.equals(user.getLastName())) {
            user.setLastName(lastName);
            updated = true;
        }

        if (updated) {
            return userRepository.save(user);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveLanguage(Long telegramId, String fallbackLanguageCode) {
        return userRepository.findByTelegramId(telegramId)
                .map(User::getLanguage)
                .map(messageResolver::normalizeLanguage)
                .orElse(messageResolver.normalizeLanguage(fallbackLanguageCode));
    }

    @Override
    @Transactional
    public User updateLanguage(Long telegramId, String languageCode) {
        User user = getOrCreateUser(telegramId, languageCode);
        user.setLanguage(messageResolver.normalizeLanguage(languageCode));
        user.setLanguageSelected(true);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User toggleBossSpawnAnnouncements(Long telegramId, String languageCode) {
        User user = getOrCreateUser(telegramId, languageCode);
        user.setAnnounceBossSpawn(!user.isAnnounceBossSpawn());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User toggleEventStartAnnouncements(Long telegramId, String languageCode) {
        User user = getOrCreateUser(telegramId, languageCode);
        user.setAnnounceEventStart(!user.isAnnounceEventStart());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User saveNotificationSettings(Long telegramId,
                                         String languageCode,
                                         boolean announceBossSpawn,
                                         boolean announceEventStart,
                                         boolean announceAutofarmDeath,
                                         boolean announceServerRestart,
                                         boolean announceNewHwidLogin) {
        User user = getOrCreateUser(telegramId, languageCode);
        user.setAnnounceBossSpawn(announceBossSpawn);
        user.setAnnounceEventStart(announceEventStart);
        user.setAnnounceAutofarmDeath(announceAutofarmDeath);
        user.setAnnounceServerRestart(announceServerRestart);
        user.setAnnounceNewHwidLogin(announceNewHwidLogin);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getBossAnnouncementSubscribers() {
        return userRepository.findAllByAnnounceBossSpawnTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getEventAnnouncementSubscribers() {
        return userRepository.findAllByAnnounceEventStartTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getServerRestartSubscribers() {
        return userRepository.findAllByAnnounceServerRestartTrue();
    }
}
