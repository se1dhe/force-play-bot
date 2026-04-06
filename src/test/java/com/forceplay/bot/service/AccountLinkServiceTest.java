package com.forceplay.bot.service;

import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.GameCharacterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountLinkServiceTest {

    @Mock
    private LineageApiService lineageApiService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private GameCharacterRepository gameCharacterRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private AccountLinkService accountLinkService;

    @Test
    void shouldPersistAccountAndCharactersOnConfirm() {
        User user = User.builder().id(1L).telegramId(99L).language("ru").build();
        when(userService.getOrCreateUser(99L, "ru")).thenReturn(user);
        when(lineageApiService.confirmAccountLink("x25_old", "req-1", 99L))
                .thenReturn(new LinkConfirmResult("acc-1", "x25_old", "hwid-1", List.of("Main", "Spoil")));
        when(accountRepository.findByExternalAccountIdAndServerName("acc-1", "x25_old")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(10L);
            return account;
        });

        LinkConfirmResult result = accountLinkService.confirmLink(99L, "ru", "x25_old", "req-1");

        assertThat(result.externalAccountId()).isEqualTo("acc-1");
        verify(gameCharacterRepository).deleteAllByAccountId(10L);
        verify(gameCharacterRepository, times(2)).save(any());
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getHwid()).isEqualTo("hwid-1");
        assertThat(accountCaptor.getValue().getUser()).isEqualTo(user);
    }
}
