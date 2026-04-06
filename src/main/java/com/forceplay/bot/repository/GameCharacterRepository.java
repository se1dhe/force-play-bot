package com.forceplay.bot.repository;

import com.forceplay.bot.model.GameCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameCharacterRepository extends JpaRepository<GameCharacter, Long> {
    void deleteAllByAccountId(Long accountId);
    List<GameCharacter> findAllByAccountId(Long accountId);
}
