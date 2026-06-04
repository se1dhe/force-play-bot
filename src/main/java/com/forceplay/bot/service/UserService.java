package com.forceplay.bot.service;

import com.forceplay.bot.model.User;

import java.util.List;

public interface UserService {
    User getOrCreateUser(Long telegramId, String languageCode);

    User getOrCreateUser(Long telegramId, String languageCode, String username, String firstName, String lastName);

    java.util.Optional<User> findByTelegramId(Long telegramId);

    String resolveLanguage(Long telegramId, String fallbackLanguageCode);

    User updateLanguage(Long telegramId, String languageCode);

    User toggleBossSpawnAnnouncements(Long telegramId, String languageCode);

    User toggleEventStartAnnouncements(Long telegramId, String languageCode);

    User saveNotificationSettings(Long telegramId,
                                  String languageCode,
                                  boolean announceBossSpawn,
                                  boolean announceEventStart,
                                  boolean announceAutofarmDeath,
                                  boolean announceServerRestart,
                                  boolean announceNewHwidLogin);

    List<User> getBossAnnouncementSubscribers();

    List<User> getEventAnnouncementSubscribers();

    List<User> getServerRestartSubscribers();
}
