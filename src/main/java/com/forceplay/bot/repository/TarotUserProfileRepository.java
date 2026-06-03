package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotUserProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TarotUserProfileRepository extends JpaRepository<TarotUserProfile, Long> {
    Optional<TarotUserProfile> findByUserTelegramId(Long telegramId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from TarotUserProfile profile where profile.user.telegramId = :telegramId")
    Optional<TarotUserProfile> lockByUserTelegramId(@Param("telegramId") Long telegramId);
}
