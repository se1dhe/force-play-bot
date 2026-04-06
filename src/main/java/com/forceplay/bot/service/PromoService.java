package com.forceplay.bot.service;

import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.PromoCode;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.PromoCodeRepository;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromoService {

    private final PromoCodeRepository promoCodeRepository;
    private final UserService userService;
    private final LineageApiService lineageApiService;

    @Transactional
    public PromoCode generate(String serverName) {
        return promoCodeRepository.save(PromoCode.builder()
                .serverName(serverName)
                .code(UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT))
                .createdAt(OffsetDateTime.now())
                .build());
    }

    @Transactional
    public String redeem(Long telegramId, String language, String serverName, String externalAccountId, String code) {
        User user = userService.getOrCreateUser(telegramId, language);
        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ForcePlayException("Promo not found"));
        if (promoCode.getUsedBy() != null) {
            throw new ForcePlayException("Promo already used");
        }

        lineageApiService.redeemPromo(serverName, externalAccountId, code);
        promoCode.setUsedBy(user);
        promoCode.setUsedAt(OffsetDateTime.now());
        promoCodeRepository.save(promoCode);
        return promoCode.getCode();
    }
}
