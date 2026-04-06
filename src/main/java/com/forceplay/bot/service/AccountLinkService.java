package com.forceplay.bot.service;

import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.GameCharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AccountLinkService {

    private final LineageApiService lineageApiService;
    private final AccountRepository accountRepository;
    private final GameCharacterRepository gameCharacterRepository;
    private final UserService userService;

    public LinkRequestResult requestLink(Long telegramId, String language, String serverName, String characterName) {
        userService.getOrCreateUser(telegramId, language);
        return lineageApiService.requestAccountLink(serverName, characterName, telegramId);
    }

    @Transactional
    public LinkConfirmResult confirmLink(Long telegramId, String language, String serverName, String requestId) {
        User user = userService.getOrCreateUser(telegramId, language);
        LinkConfirmResult result = lineageApiService.confirmAccountLink(serverName, requestId, telegramId);

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
        result.characters().forEach(name -> gameCharacterRepository.save(GameCharacter.builder()
                .account(saved)
                .name(name)
                .build()));

        return result;
    }
}
