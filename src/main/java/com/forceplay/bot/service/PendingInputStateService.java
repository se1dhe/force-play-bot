package com.forceplay.bot.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class PendingInputStateService {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final RedisStateService redisStateService;

    public PendingInputStateService(RedisStateService redisStateService) {
        this.redisStateService = redisStateService;
    }

    public void save(Long telegramId, PendingInputAction action, String payload) {
        redisStateService.put(redisKey(telegramId), action.name() + "|" + (payload == null ? "" : payload), DEFAULT_TTL);
    }

    public Optional<PendingInputState> get(Long telegramId) {
        String value = redisStateService.get(redisKey(telegramId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String[] parts = value.split("\\|", 2);
        PendingInputAction action = PendingInputAction.valueOf(parts[0]);
        String payload = parts.length > 1 ? parts[1] : "";
        return Optional.of(new PendingInputState(action, payload));
    }

    public boolean exists(Long telegramId) {
        return get(telegramId).isPresent();
    }

    public void clear(Long telegramId) {
        redisStateService.delete(redisKey(telegramId));
    }

    private String redisKey(Long telegramId) {
        return "forceplay:pending:" + telegramId;
    }

    public record PendingInputState(PendingInputAction action, String payload) {
    }
}
