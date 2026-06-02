package com.forceplay.bot.service;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.GameCharacterRepository;
import com.forceplay.bot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private TelegramBotProperties botProperties;
    @Mock
    private LineageServersProperties serversProperties;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private GameCharacterRepository gameCharacterRepository;
    @InjectMocks
    private DemoDataSeeder demoDataSeeder;

    @Test
    void shouldSeedDemoUserAccountsAndCharacters() throws Exception {
        LineageServersProperties.Server x25 = server("x25_old");
        LineageServersProperties.Server x10 = server("x10_new");
        when(botProperties.getDemoUserTelegramId()).thenReturn(777000L);
        when(botProperties.getDemoUserLanguage()).thenReturn("ru");
        when(serversProperties.getServers()).thenReturn(List.of(x25, x10));
        when(userRepository.findByTelegramId(777000L)).thenReturn(java.util.Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(accountRepository.findByExternalAccountIdAndServerName(any(), any())).thenReturn(java.util.Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(account.getServerName().equals("x25_old") ? 10L : 20L);
            return account;
        });
        when(gameCharacterRepository.findAllByAccountId(10L)).thenReturn(List.of(), List.of(), List.of());
        when(gameCharacterRepository.findAllByAccountId(20L)).thenReturn(List.of(), List.of(), List.of());

        demoDataSeeder.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository).save(any(User.class));
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(gameCharacterRepository, times(6)).save(any());
    }

    private LineageServersProperties.Server server(String name) {
        LineageServersProperties.Server server = new LineageServersProperties.Server();
        server.setName(name);
        server.setBaseUrl("https://example.com/" + name);
        server.setToken("token-" + name);
        return server;
    }
}
