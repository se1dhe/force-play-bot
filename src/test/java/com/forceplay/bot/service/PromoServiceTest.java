package com.forceplay.bot.service;

import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.PromoCode;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.PromoCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromoServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private UserService userService;
    @Mock
    private LineageApiService lineageApiService;

    @InjectMocks
    private PromoService promoService;

    @Test
    void shouldRedeemPromoAndMarkUsed() {
        User user = User.builder().id(1L).telegramId(77L).language("ru").build();
        PromoCode promoCode = PromoCode.builder()
                .id(1L)
                .code("PROMO123")
                .serverName("x25_old")
                .createdAt(OffsetDateTime.now())
                .build();
        when(userService.getOrCreateUser(77L, "ru")).thenReturn(user);
        when(promoCodeRepository.findByCode("PROMO123")).thenReturn(Optional.of(promoCode));

        String result = promoService.redeem(77L, "ru", "x25_old", "acc-1", "PROMO123");

        assertThat(result).isEqualTo("PROMO123");
        assertThat(promoCode.getUsedBy()).isEqualTo(user);
        verify(promoCodeRepository).save(promoCode);
        verify(lineageApiService).redeemPromo("x25_old", "acc-1", "PROMO123");
    }
}
