package com.forceplay.bot.dto;

public record HwidUnlinkResult(
        Long externalCharacterId,
        boolean unlinked,
        String message
) {
}
