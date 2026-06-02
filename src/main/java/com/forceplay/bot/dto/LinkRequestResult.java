package com.forceplay.bot.dto;

public record LinkRequestResult(
        String requestId,
        String serverName,
        String characterName,
        boolean ok,
        String status,
        String message
) {
}
