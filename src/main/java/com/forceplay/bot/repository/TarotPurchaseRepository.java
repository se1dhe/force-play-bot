package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotPurchase;
import com.forceplay.bot.model.TarotPurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarotPurchaseRepository extends JpaRepository<TarotPurchase, Long> {
    Optional<TarotPurchase> findByPayload(String payload);

    long countByStatus(TarotPurchaseStatus status);

    long countByStatusAndStarsAmountGreaterThan(TarotPurchaseStatus status, int starsAmount);
}
