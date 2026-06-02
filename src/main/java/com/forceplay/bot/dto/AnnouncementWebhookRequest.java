package com.forceplay.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnnouncementWebhookRequest(
        @NotNull AnnouncementType type,
        @NotBlank String serverName,
        @NotBlank String title,
        String scheduledAt,
        String dedupeKey
) {
}
