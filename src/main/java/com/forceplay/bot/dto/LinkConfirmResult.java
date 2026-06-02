package com.forceplay.bot.dto;

import java.util.List;

public record LinkConfirmResult(
        String externalAccountId,
        String serverName,
        String hwid,
        List<LinkedCharacter> linkedCharacters
) {
    public List<String> characters() {
        return (linkedCharacters == null ? List.<LinkedCharacter>of() : linkedCharacters).stream()
                .map(LinkedCharacter::name)
                .toList();
    }

    public record LinkedCharacter(
            Long externalCharacterId,
            String name
    ) {
    }
}
