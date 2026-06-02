package com.forceplay.bot.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class AutofarmQuizStateService {

    private static final Duration QUIZ_TTL = Duration.ofMinutes(5);

    private final RedisStateService redisStateService;

    public AutofarmQuizStateService(RedisStateService redisStateService) {
        this.redisStateService = redisStateService;
    }

    public void save(Long telegramId, Long characterId, int correctAnswerIndex) {
        redisStateService.put(redisKey(telegramId), characterId + "|" + correctAnswerIndex, QUIZ_TTL);
    }

    public Optional<QuizState> get(Long telegramId) {
        String value = redisStateService.get(redisKey(telegramId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String[] parts = value.split("\\|");
        return Optional.of(new QuizState(Long.parseLong(parts[0]), Integer.parseInt(parts[1])));
    }

    public void clear(Long telegramId) {
        redisStateService.delete(redisKey(telegramId));
    }

    private String redisKey(Long telegramId) {
        return "forceplay:autofarm:quiz:" + telegramId;
    }

    public record QuizState(Long characterId, int correctAnswerIndex) {
    }
}
