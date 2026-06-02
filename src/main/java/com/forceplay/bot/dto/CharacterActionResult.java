package com.forceplay.bot.dto;

public record CharacterActionResult(
        Long externalCharacterId,
        boolean success,
        String message
) {
}
