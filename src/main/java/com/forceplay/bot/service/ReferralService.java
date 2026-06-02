package com.forceplay.bot.service;

import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.model.Referral;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.ReferralRepository;
import com.forceplay.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TelegramBotProperties botProperties;

    @Transactional
    public void registerReferral(Long telegramId, String language, String referralPayload) {
        if (!botProperties.isReferralEnabled() || referralPayload == null || !referralPayload.startsWith("ref_")) {
            return;
        }

        Long inviterTelegramId = Long.parseLong(referralPayload.substring(4));
        if (inviterTelegramId.equals(telegramId)) {
            return;
        }

        User referred = userService.getOrCreateUser(telegramId, language);
        userRepository.findByTelegramId(inviterTelegramId).ifPresent(inviter -> {
            if (!referralRepository.existsByUserTelegramIdAndReferredUserTelegramId(inviterTelegramId, telegramId)) {
                referralRepository.save(Referral.builder()
                        .user(inviter)
                        .referredUser(referred)
                        .createdAt(OffsetDateTime.now())
                .build());
            }
        });
    }

    public long countInvitedUsers(Long telegramId) {
        return referralRepository.countByUserTelegramId(telegramId);
    }
}
