package com.forceplay.bot.service;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.GameCharacterRepository;
import com.forceplay.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "forceplay.bot.demo-data-enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private final TelegramBotProperties botProperties;
    private final LineageServersProperties serversProperties;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final GameCharacterRepository gameCharacterRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long telegramId = botProperties.getDemoUserTelegramId();
        User user = userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .telegramId(telegramId)
                        .language(botProperties.getDemoUserLanguage())
                        .createdAt(OffsetDateTime.now())
                        .build()));

        serversProperties.getServers().forEach(server -> {
            Account account = seedAccount(user, server.getName());
            seedCharacters(account);
        });

        log.info("Demo data ready for telegramId={} on {} servers", telegramId, serversProperties.getServers().size());
    }

    private Account seedAccount(User user, String serverName) {
        String externalAccountId = "demo_%s_acc".formatted(serverName.toLowerCase(Locale.ROOT));
        return accountRepository.findByExternalAccountIdAndServerName(externalAccountId, serverName)
                .orElseGet(() -> accountRepository.save(Account.builder()
                        .externalAccountId(externalAccountId)
                        .serverName(serverName)
                        .hwid("DEMO-%s-HWID".formatted(serverName.toUpperCase(Locale.ROOT)))
                        .user(user)
                        .createdAt(OffsetDateTime.now())
                        .build()));
    }

    private void seedCharacters(Account account) {
        ensureCharacter(account, 1, "Gladiator");
        ensureCharacter(account, 2, "Archmage");
        ensureCharacter(account, 3, "Spoiler");
    }

    private void ensureCharacter(Account account, int slot, String roleName) {
        Long externalCharacterId = demoExternalCharacterId(account.getServerName(), slot);
        boolean exists = gameCharacterRepository.findAllByAccountId(account.getId()).stream()
                .anyMatch(character -> externalCharacterId.equals(character.getExternalCharacterId()));
        if (exists) {
            return;
        }

        String serverSuffix = account.getServerName().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        gameCharacterRepository.save(GameCharacter.builder()
                .account(account)
                .externalCharacterId(externalCharacterId)
                .name("Demo%s%s".formatted(roleName, serverSuffix))
                .build());
    }

    private long demoExternalCharacterId(String serverName, int slot) {
        long normalized = Math.abs(serverName.toLowerCase(Locale.ROOT).hashCode());
        return normalized * 10L + slot;
    }
}
