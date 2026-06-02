package com.forceplay.bot.service;

import com.forceplay.bot.dto.CharacterActionResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.GameCharacter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterFunctionService {

    private final LineageApiService lineageApiService;

    public CharacterActionResult teleportToTown(GameCharacter character) {
        return lineageApiService.teleportToTown(
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getExternalCharacterId()
        );
    }
}
