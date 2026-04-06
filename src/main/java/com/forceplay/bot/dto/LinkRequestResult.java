package com.forceplay.bot.dto;

import java.util.List;

public record LinkRequestResult(
        String requestId,
        String serverName,
        String characterName
) {
}
