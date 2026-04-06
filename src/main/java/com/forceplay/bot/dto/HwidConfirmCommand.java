package com.forceplay.bot.dto;

public record HwidConfirmCommand(
        String serverName,
        String externalAccountId,
        String hwid,
        boolean approved
) {
}
