package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarotSeasonRepository extends JpaRepository<TarotSeason, Long> {
    Optional<TarotSeason> findFirstByActiveTrueOrderByStartDateDesc();
}
