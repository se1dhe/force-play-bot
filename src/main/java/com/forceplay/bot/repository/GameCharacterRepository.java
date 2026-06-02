package com.forceplay.bot.repository;

import com.forceplay.bot.model.GameCharacter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameCharacterRepository extends JpaRepository<GameCharacter, Long> {
    void deleteAllByAccountId(Long accountId);

    @EntityGraph(attributePaths = "account")
    List<GameCharacter> findAllByAccountId(Long accountId);

    @EntityGraph(attributePaths = "account")
    List<GameCharacter> findAllByAccountUserTelegramId(Long telegramId);

    @EntityGraph(attributePaths = "account")
    Optional<GameCharacter> findByIdAndAccountUserTelegramId(Long id, Long telegramId);

    @EntityGraph(attributePaths = "account")
    Optional<GameCharacter> findByExternalCharacterIdAndAccountServerName(Long externalCharacterId, String serverName);
}
