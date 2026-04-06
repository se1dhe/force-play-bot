package com.forceplay.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisStateService {

    private final StringRedisTemplate redisTemplate;

    public boolean tryAcquireRateLimit(String key, Duration ttl, long threshold) {
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1) {
            redisTemplate.expire(key, ttl);
        }
        return current != null && current <= threshold;
    }

    public boolean registerIdempotency(String key, Duration ttl) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(result);
    }

    public void put(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void enqueueNotification(String payload) {
        redisTemplate.opsForList().rightPush("forceplay:notifications", payload);
    }
}
