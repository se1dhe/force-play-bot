package com.forceplay.bot.service;

import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.User;
import com.forceplay.bot.repository.UserRepository;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final LineageApiService lineageApiService;
    private final TelegramGateway telegramGateway;
    private final UserRepository userRepository;
    private final MessageResolver messageResolver;

    @Transactional
    public String claimIfSubscribed(Long telegramId,
                                    String language,
                                    String serverName,
                                    String externalAccountId,
                                    Long externalCharacterId,
                                    String channelUsername,
                                    long itemId,
                                    int itemCount) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + telegramId));
        if (user.getBonusClaimedAt() != null) {
            return text(language, "bonus.already.claimed", "Бонус за подписку уже получен.");
        }
        if (!telegramGateway.isSubscribed(telegramId, channelUsername)) {
            return text(language, "bonus.subscription.required", "Нужна подписка на канал.");
        }
        var result = lineageApiService.claimBonus(serverName, externalAccountId, externalCharacterId, telegramId, itemId, itemCount);
        if (result.success()) {
            user.setBonusClaimedAt(OffsetDateTime.now());
        }
        return result.message();
    }

    private String text(String language, String key, String defaultValue) {
        return messageResolver.get(language, key, defaultValue);
    }
}
