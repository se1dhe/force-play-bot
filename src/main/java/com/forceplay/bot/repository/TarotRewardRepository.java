package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotRarity;
import com.forceplay.bot.model.TarotReward;
import com.forceplay.bot.model.TarotSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarotRewardRepository extends JpaRepository<TarotReward, Long> {
    List<TarotReward> findAllBySeasonAndEnabledTrueAndRarityIn(TarotSeason season, List<TarotRarity> rarities);

    List<TarotReward> findAllBySeasonAndEnabledTrueAndRarity(TarotSeason season, TarotRarity rarity);
}
