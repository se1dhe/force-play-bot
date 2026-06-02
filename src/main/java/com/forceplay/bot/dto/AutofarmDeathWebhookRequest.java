package com.forceplay.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AutofarmDeathWebhookRequest(
        @NotBlank String serverName,
        @NotNull Long externalCharacterId,
        String killerName,
        String dedupeKey
) {
}
