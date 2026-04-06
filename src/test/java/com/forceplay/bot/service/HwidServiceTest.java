package com.forceplay.bot.service;

import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.dto.HwidConfirmCommand;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.HwidRequest;
import com.forceplay.bot.model.HwidRequestStatus;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.HwidRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HwidServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private HwidRequestRepository hwidRequestRepository;
    @Mock
    private LineageApiService lineageApiService;
    @Mock
    private RedisStateService redisStateService;

    @InjectMocks
    private HwidService hwidService;

    @Test
    void shouldApproveHwidAndCallIntegration() {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.setHwidTtlSeconds(120);
        hwidService = new HwidService(accountRepository, hwidRequestRepository, lineageApiService, properties, redisStateService);

        User user = User.builder().id(1L).telegramId(100L).language("ru").build();
        Account account = Account.builder().id(5L).externalAccountId("acc-1").serverName("x25_old").user(user).build();
        HwidRequest request = HwidRequest.builder()
                .id(11L)
                .account(account)
                .newHwid("new-hwid")
                .status(HwidRequestStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusMinutes(2))
                .build();
        when(hwidRequestRepository.findById(11L)).thenReturn(Optional.of(request));

        hwidService.resolve(11L, true);

        assertThat(account.getHwid()).isEqualTo("new-hwid");
        ArgumentCaptor<HwidConfirmCommand> captor = ArgumentCaptor.forClass(HwidConfirmCommand.class);
        verify(lineageApiService).confirmHwid(captor.capture());
        assertThat(captor.getValue().approved()).isTrue();
    }

    @Test
    void shouldExpirePendingRequestsByDefaultDeny() {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.setHwidTtlSeconds(120);
        hwidService = new HwidService(accountRepository, hwidRequestRepository, lineageApiService, properties, redisStateService);

        Account account = Account.builder().id(5L).externalAccountId("acc-1").serverName("x25_old").build();
        HwidRequest request = HwidRequest.builder()
                .id(11L)
                .account(account)
                .newHwid("new-hwid")
                .status(HwidRequestStatus.PENDING)
                .createdAt(OffsetDateTime.now().minusMinutes(5))
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        when(hwidRequestRepository.findAllByStatusAndExpiresAtBefore(any(), any())).thenReturn(List.of(request));

        int expired = hwidService.expirePendingRequests();

        assertThat(expired).isEqualTo(1);
        assertThat(request.getStatus()).isEqualTo(HwidRequestStatus.DENIED);
        verify(lineageApiService).confirmHwid(any(HwidConfirmCommand.class));
    }
}
