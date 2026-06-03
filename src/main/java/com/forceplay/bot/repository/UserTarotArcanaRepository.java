package com.forceplay.bot.repository;

import com.forceplay.bot.model.UserTarotArcana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTarotArcanaRepository extends JpaRepository<UserTarotArcana, Long> {
    Optional<UserTarotArcana> findByUserTelegramIdAndArcanaId(Long telegramId, Long arcanaId);

    long countByUserTelegramId(Long telegramId);
}
