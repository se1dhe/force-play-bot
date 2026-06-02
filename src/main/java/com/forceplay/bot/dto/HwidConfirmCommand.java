package com.forceplay.bot.dto;

import com.forceplay.bot.model.HwidSlot;

public record HwidConfirmCommand(
        String serverName,
        String externalAccountId,
        Long externalCharacterId,
        HwidSlot slot,
        String hwid,
        boolean approved
) {
}
