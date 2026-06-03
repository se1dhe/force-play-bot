package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotDeck;
import com.forceplay.bot.model.TarotSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarotDeckRepository extends JpaRepository<TarotDeck, Long> {
    Optional<TarotDeck> findFirstBySeasonAndActiveTrueOrderByIdAsc(TarotSeason season);
}
