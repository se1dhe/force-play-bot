package com.forceplay.bot.service;

import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.PromoCodeRepository;
import com.forceplay.bot.repository.UserRepository;
import com.forceplay.bot.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyPromoDistributionServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TelegramGateway telegramGateway;
    @Mock
    private AdminService adminService;
    @Mock
    private RedisStateService redisStateService;
    @Mock
    private MessageResolver messageResolver;

    @TempDir
    Path tempDir;

    @Test
    void shouldIssueDailyPromoToSingleUser() throws Exception {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.setPromoFilePath(tempDir.resolve("promo.txt").toString());
        properties.setPromoLowStockThreshold(100);
        Files.write(tempDir.resolve("promo.txt"), List.of("CODE1111", "CODE2222"));

        DailyPromoDistributionService service = new DailyPromoDistributionService(
                promoCodeRepository,
                userRepository,
                telegramGateway,
                adminService,
                redisStateService,
                properties,
                messageResolver
        );

        User user = User.builder()
                .telegramId(1259547081L)
                .language("ru")
                .createdAt(OffsetDateTime.now())
                .build();
        when(userRepository.findByTelegramId(1259547081L)).thenReturn(Optional.of(user));
        when(redisStateService.get("forceplay:promo:low-stock-alert")).thenReturn(null);
        when(adminService.getAdminIds()).thenReturn(List.of());
        when(promoCodeRepository.existsByCode("CODE1111")).thenReturn(false);

        String code = service.issueDailyPromoToUser(1259547081L);

        assertThat(code).isEqualTo("CODE1111");
        assertThat(Files.readAllLines(tempDir.resolve("promo.txt"))).containsExactly("CODE2222");

        ArgumentCaptor<com.forceplay.bot.model.PromoCode> captor = ArgumentCaptor.forClass(com.forceplay.bot.model.PromoCode.class);
        verify(promoCodeRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("CODE1111");
        assertThat(captor.getValue().getUsedBy()).isEqualTo(user);
        verify(telegramGateway, never()).sendText(any(), any());
    }

    @Test
    void shouldSkipDuplicatePromoCodesFromFile() throws Exception {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.setPromoFilePath(tempDir.resolve("promo.txt").toString());
        properties.setPromoLowStockThreshold(100);
        Files.write(tempDir.resolve("promo.txt"), List.of("DUPLICATE", "FRESH999"));

        DailyPromoDistributionService service = new DailyPromoDistributionService(
                promoCodeRepository,
                userRepository,
                telegramGateway,
                adminService,
                redisStateService,
                properties,
                messageResolver
        );

        User user = User.builder()
                .telegramId(1259547081L)
                .language("ru")
                .createdAt(OffsetDateTime.now())
                .build();
        when(userRepository.findByTelegramId(1259547081L)).thenReturn(Optional.of(user));
        when(redisStateService.get("forceplay:promo:low-stock-alert")).thenReturn(null);
        when(adminService.getAdminIds()).thenReturn(List.of());
        when(promoCodeRepository.existsByCode("DUPLICATE")).thenReturn(true);
        when(promoCodeRepository.existsByCode("FRESH999")).thenReturn(false);

        String code = service.issueDailyPromoToUser(1259547081L);

        assertThat(code).isEqualTo("FRESH999");
        assertThat(Files.readAllLines(tempDir.resolve("promo.txt"))).isEmpty();
    }
}
