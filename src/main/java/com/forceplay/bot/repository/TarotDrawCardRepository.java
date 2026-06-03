package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotDrawCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarotDrawCardRepository extends JpaRepository<TarotDrawCard, Long> {
    List<TarotDrawCard> findAllByDrawIdOrderByCardIndexAsc(Long drawId);

    Optional<TarotDrawCard> findByDrawIdAndCardIndex(Long drawId, int cardIndex);
}
