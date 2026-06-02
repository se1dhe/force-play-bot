package com.forceplay.bot.service;

import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.model.PromoCode;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.PromoCodeRepository;
import com.forceplay.bot.repository.UserRepository;
import com.forceplay.bot.util.ForcePlayException;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyPromoDistributionService {

    private static final String LOW_STOCK_ALERT_KEY = "forceplay:promo:low-stock-alert";

    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;
    private final TelegramGateway telegramGateway;
    private final AdminService adminService;
    private final RedisStateService redisStateService;
    private final TelegramBotProperties botProperties;
    private final MessageResolver messageResolver;

    @Transactional
    public PromoDistributionResult distributeDailyPromos() {
        String distributionKey = "forceplay:promo:distribution:" + LocalDate.now();
        if (!redisStateService.registerIdempotency(distributionKey, Duration.ofDays(2))) {
            int remaining = remainingPromoCodes();
            notifyAdminsAboutLowStockIfNeeded(remaining);
            return new PromoDistributionResult(0, remaining, true);
        }

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            int remaining = remainingPromoCodes();
            notifyAdminsAboutLowStockIfNeeded(remaining);
            return new PromoDistributionResult(0, remaining, false);
        }

        synchronized (this) {
            List<String> availableCodes = new ArrayList<>(loadPromoCodes());
            int assigned = 0;
            List<PromoCode> issuedPromos = new ArrayList<>();
            OffsetDateTime now = OffsetDateTime.now();

            for (User user : users) {
                String code = pollNextAvailableCode(availableCodes);
                if (code == null) {
                    break;
                }
                issuedPromos.add(PromoCode.builder()
                        .code(code)
                        .serverName("daily")
                        .usedBy(user)
                        .usedAt(now)
                        .createdAt(now)
                        .build());
                telegramGateway.sendText(
                        user.getTelegramId(),
                        text(user.getLanguage(), "promo.daily.assigned", "Ваш ежедневный промокод: %s").formatted(code)
                );
                assigned++;
            }

            if (!issuedPromos.isEmpty()) {
                promoCodeRepository.saveAll(issuedPromos);
            }
            writePromoCodes(availableCodes);

            if (assigned < users.size()) {
                log.warn("Promo pool exhausted: assigned {} of {} users", assigned, users.size());
            }

            notifyAdminsAboutLowStockIfNeeded(availableCodes.size());
            return new PromoDistributionResult(assigned, availableCodes.size(), false);
        }
    }

    public PromoStats getStats() {
        int remaining = remainingPromoCodes();
        notifyAdminsAboutLowStockIfNeeded(remaining);
        return new PromoStats(remaining, botProperties.getPromoLowStockThreshold(), botProperties.getPromoDailyCron());
    }

    @Transactional
    public String issueDailyPromoToUser(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ForcePlayException("User not found: " + telegramId));
        synchronized (this) {
            List<String> availableCodes = new ArrayList<>(loadPromoCodes());
            String code = pollNextAvailableCode(availableCodes);
            if (code == null) {
                throw new ForcePlayException("No promo codes left in file");
            }
            OffsetDateTime now = OffsetDateTime.now();
            promoCodeRepository.save(PromoCode.builder()
                    .code(code)
                    .serverName("daily")
                    .usedBy(user)
                    .usedAt(now)
                    .createdAt(now)
                    .build());
            writePromoCodes(availableCodes);
            notifyAdminsAboutLowStockIfNeeded(availableCodes.size());
            return code;
        }
    }

    public Optional<String> latestIssuedCode(Long telegramId) {
        return promoCodeRepository.findFirstByUsedByTelegramIdOrderByUsedAtDesc(telegramId)
                .map(PromoCode::getCode);
    }

    public int remainingPromoCodes() {
        synchronized (this) {
            return loadPromoCodes().size();
        }
    }

    private void notifyAdminsAboutLowStockIfNeeded(int remaining) {
        if (remaining >= botProperties.getPromoLowStockThreshold()) {
            redisStateService.delete(LOW_STOCK_ALERT_KEY);
            return;
        }
        if (redisStateService.get(LOW_STOCK_ALERT_KEY) != null) {
            return;
        }
        adminService.getAdminIds().forEach(adminId ->
                telegramGateway.sendText(
                        adminId,
                        text("ru", "promo.admin.low_stock", "В файле promo.txt осталось меньше %d промокодов. Текущий остаток: %d")
                                .formatted(botProperties.getPromoLowStockThreshold(), remaining)
                )
        );
        redisStateService.put(LOW_STOCK_ALERT_KEY, String.valueOf(remaining), Duration.ofDays(3650));
    }

    private List<String> loadPromoCodes() {
        Path promoFile = resolvePromoFilePath();
        try {
            if (!Files.exists(promoFile)) {
                throw new ForcePlayException("promo.txt not found: " + promoFile);
            }
            return Files.readAllLines(promoFile).stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .toList();
        } catch (IOException exception) {
            throw new ForcePlayException("Failed to read promo file: " + promoFile);
        }
    }

    private void writePromoCodes(List<String> remainingCodes) {
        Path promoFile = resolvePromoFilePath();
        Path tempFile = promoFile.resolveSibling(promoFile.getFileName() + ".tmp");
        try {
            Files.write(tempFile, remainingCodes);
            Files.move(tempFile, promoFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new ForcePlayException("Failed to write promo file: " + promoFile);
        }
    }

    private Path resolvePromoFilePath() {
        return Path.of(botProperties.getPromoFilePath()).toAbsolutePath().normalize();
    }

    private String pollNextAvailableCode(List<String> availableCodes) {
        while (!availableCodes.isEmpty()) {
            String code = availableCodes.remove(0);
            if (!promoCodeRepository.existsByCode(code)) {
                return code;
            }
        }
        return null;
    }

    private String text(String language, String key, String defaultValue) {
        return messageResolver.get(language, key, defaultValue);
    }

    public record PromoDistributionResult(int assignedCount, int remainingCount, boolean skipped) {
    }

    public record PromoStats(int remainingCount, int lowStockThreshold, String dailyCron) {
    }
}
