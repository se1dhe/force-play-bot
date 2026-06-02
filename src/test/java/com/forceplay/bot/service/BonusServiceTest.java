package com.forceplay.bot.service;

import com.forceplay.bot.dto.BonusClaimResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.UserRepository;
import com.forceplay.bot.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BonusServiceTest {

    @Mock
    private LineageApiService lineageApiService;
    @Mock
    private TelegramGateway telegramGateway;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private BonusService bonusService;

    @Test
    void shouldReturnSubscriptionRequiredWhenUserIsNotSubscribed() {
        User user = User.builder().telegramId(77L).language("ru").createdAt(OffsetDateTime.now()).build();
        when(userRepository.findByTelegramId(77L)).thenReturn(Optional.of(user));
        when(telegramGateway.isSubscribed(77L, "@forceplay")).thenReturn(false);
        when(messageResolver.get("ru", "bonus.subscription.required", "Нужна подписка на канал.")).thenReturn("Нужна подписка на канал.");

        String result = bonusService.claimIfSubscribed(77L, "ru", "x25_old", "acc-1", 1001L, "@forceplay", 57L, 10);

        assertThat(result).isEqualTo("Нужна подписка на канал.");
        verify(lineageApiService, never()).claimBonus("x25_old", "acc-1", 1001L, 77L, 57L, 10);
    }

    @Test
    void shouldClaimBonusWhenUserIsSubscribed() {
        User user = User.builder().telegramId(77L).language("ru").createdAt(OffsetDateTime.now()).build();
        when(userRepository.findByTelegramId(77L)).thenReturn(Optional.of(user));
        when(telegramGateway.isSubscribed(77L, "@forceplay")).thenReturn(true);
        when(lineageApiService.claimBonus("x25_old", "acc-1", 1001L, 77L, 57L, 10))
                .thenReturn(new BonusClaimResult(true, "Bonus claimed"));

        String result = bonusService.claimIfSubscribed(77L, "ru", "x25_old", "acc-1", 1001L, "@forceplay", 57L, 10);

        assertThat(result).isEqualTo("Bonus claimed");
        assertThat(user.getBonusClaimedAt()).isNotNull();
        verify(lineageApiService).claimBonus("x25_old", "acc-1", 1001L, 77L, 57L, 10);
    }

    @Test
    void shouldReturnAlreadyClaimedWhenBonusWasReceivedBefore() {
        User user = User.builder()
                .telegramId(77L)
                .language("ru")
                .bonusClaimedAt(OffsetDateTime.now().minusDays(1))
                .createdAt(OffsetDateTime.now())
                .build();
        when(userRepository.findByTelegramId(77L)).thenReturn(Optional.of(user));
        when(messageResolver.get("ru", "bonus.already.claimed", "Бонус за подписку уже получен."))
                .thenReturn("Бонус за подписку уже получен.");

        String result = bonusService.claimIfSubscribed(77L, "ru", "x25_old", "acc-1", 1001L, "@forceplay", 57L, 10);

        assertThat(result).isEqualTo("Бонус за подписку уже получен.");
        verify(telegramGateway, never()).isSubscribed(77L, "@forceplay");
        verify(lineageApiService, never()).claimBonus("x25_old", "acc-1", 1001L, 77L, 57L, 10);
    }
}
