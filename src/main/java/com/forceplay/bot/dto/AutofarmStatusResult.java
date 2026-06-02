package com.forceplay.bot.dto;

public record AutofarmStatusResult(
        Long externalCharacterId,
        boolean activeAutofarm,
        boolean autofarming,
        boolean alive,
        String killerName,
        String message
) {
}
