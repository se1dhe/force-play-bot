package com.forceplay.bot.dto;

import jakarta.validation.constraints.NotNull;

public record IssueDailyPromoRequest(@NotNull Long telegramId) {
}
