package com.forceplay.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LinkConfirmedWebhookRequest(
        @NotNull Long telegramUserId,
        @NotBlank String requestId,
        @NotBlank String serverName
) {
}
