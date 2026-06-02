package com.forceplay.bot.service;

import com.forceplay.bot.dto.AutofarmStatusResult;
import com.forceplay.bot.dto.CharacterActionResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.GameCharacter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutofarmService {

    private final LineageApiService lineageApiService;

    public AutofarmStatusResult getStatus(GameCharacter character) {
        return lineageApiService.getAutofarmStatus(
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getExternalCharacterId()
        );
    }

    public CharacterActionResult revive(GameCharacter character) {
        return lineageApiService.reviveAutofarm(
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getExternalCharacterId()
        );
    }
}
