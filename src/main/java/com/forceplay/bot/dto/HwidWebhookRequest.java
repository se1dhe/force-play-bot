package com.forceplay.bot.dto;

import jakarta.validation.constraints.NotBlank;

public record HwidWebhookRequest(
        @NotBlank String serverName,
        @NotBlank String externalAccountId,
        @NotBlank String newHwid
) {
}
