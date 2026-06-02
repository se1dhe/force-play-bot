package com.forceplay.bot.service;

import com.forceplay.bot.dto.CharacterProfileResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.GameCharacter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterProfileService {

    private final LineageApiService lineageApiService;

    public CharacterProfileResult getProfile(GameCharacter character) {
        return lineageApiService.getCharacterProfile(
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getExternalCharacterId()
        );
    }
}
