package com.forceplay.bot.dto;

public record TradeKeyResult(
        Long externalCharacterId,
        boolean success,
        String message
) {
}
