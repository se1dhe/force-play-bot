package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotArcana;
import com.forceplay.bot.model.TarotSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarotArcanaRepository extends JpaRepository<TarotArcana, Long> {
    List<TarotArcana> findAllBySeasonOrderByIdAsc(TarotSeason season);

    long countBySeason(TarotSeason season);
}
