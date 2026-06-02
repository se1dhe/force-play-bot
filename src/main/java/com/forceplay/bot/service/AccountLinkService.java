package com.forceplay.bot.service;

import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.GameCharacterRepository;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountLinkService {

    private final LineageApiService lineageApiService;
    private final AccountRepository accountRepository;
    private final GameCharacterRepository gameCharacterRepository;
    private final UserService userService;

    public LinkRequestResult requestLink(Long telegramId, String language, String serverName, String characterName) {
        userService.getOrCreateUser(telegramId, language);
        LinkRequestResult result = lineageApiService.requestAccountLink(serverName, characterName, telegramId);
        if (!result.ok()) {
            throw new ForcePlayException(defaultMessage(result.message(), "Не удалось создать запрос на привязку."));
        }
        if (result.requestId() == null || result.requestId().isBlank()) {
            throw new ForcePlayException("Сервер игры не вернул requestId для привязки.");
        }
        return result;
    }

    @Transactional
    public LinkConfirmResult confirmLink(Long telegramId, String language, String serverName, String requestId) {
        User user = userService.getOrCreateUser(telegramId, language);
        LinkConfirmResult result = lineageApiService.confirmAccountLink(serverName, requestId, telegramId);
        persistLink(user, result);
        return result;
    }

    private void persistLink(User user, LinkConfirmResult result) {
        if (result.externalAccountId() == null || result.externalAccountId().isBlank()) {
            throw new ForcePlayException("Сервер игры не вернул аккаунт для привязки.");
        }
        if (result.serverName() == null || result.serverName().isBlank()) {
            throw new ForcePlayException("Сервер игры не вернул имя сервера для привязки.");
        }
        Account account = accountRepository.findByExternalAccountIdAndServerName(result.externalAccountId(), result.serverName())
                .orElse(Account.builder()
                        .externalAccountId(result.externalAccountId())
                        .serverName(result.serverName())
                        .createdAt(OffsetDateTime.now())
                        .user(user)
                        .build());
        account.setUser(user);
        account.setHwid(result.hwid());
        Account saved = accountRepository.save(account);

        gameCharacterRepository.deleteAllByAccountId(saved.getId());
        gameCharacterRepository.flush();
        List<LinkConfirmResult.LinkedCharacter> linkedCharacters = result.linkedCharacters() == null ? List.of() : result.linkedCharacters();
        linkedCharacters.forEach(character -> gameCharacterRepository.save(GameCharacter.builder()
                .account(saved)
                .externalCharacterId(character.externalCharacterId())
                .name(character.name())
                .build()));
    }

    private String defaultMessage(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
