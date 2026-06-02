package com.forceplay.bot.dto;

public record CharacterProfileResult(
        Long externalCharacterId,
        int level,
        String className,
        int pvp,
        int pk,
        String clanName,
        boolean online
) {
}
