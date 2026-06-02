package com.forceplay.bot.dto;

import com.forceplay.bot.model.HwidSlot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HwidWebhookRequest(
        @NotBlank String serverName,
        @NotNull Long externalCharacterId,
        @NotNull HwidSlot slot,
        @NotBlank String newHwid
) {
}
