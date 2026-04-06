package com.forceplay.bot.dto;

import java.util.List;

public record LinkConfirmResult(
        String externalAccountId,
        String serverName,
        String hwid,
        List<String> characters
) {
}
