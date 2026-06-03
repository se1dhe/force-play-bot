package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotDraw;
import com.forceplay.bot.model.TarotDrawStatus;
import com.forceplay.bot.model.TarotRarity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TarotDrawRepository extends JpaRepository<TarotDraw, Long> {
    Optional<TarotDraw> findFirstByUserTelegramIdAndStatusOrderByCreatedAtDesc(Long telegramId, TarotDrawStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select draw from TarotDraw draw where draw.id = :id and draw.user.telegramId = :telegramId")
    Optional<TarotDraw> lockByIdAndUserTelegramId(@Param("id") Long id, @Param("telegramId") Long telegramId);

    List<TarotDraw> findAllByUserTelegramIdAndStatusInOrderByCreatedAtDesc(Long telegramId, List<TarotDrawStatus> statuses, Pageable pageable);

    @Query("""
            select draw from TarotDraw draw
            where draw.user.telegramId = :telegramId
              and draw.status in :statuses
              and (:rarity is null or draw.selectedReward.rarity = :rarity)
            order by draw.createdAt desc
            """)
    List<TarotDraw> findHistory(@Param("telegramId") Long telegramId,
                                @Param("statuses") List<TarotDrawStatus> statuses,
                                @Param("rarity") TarotRarity rarity,
                                Pageable pageable);

    long countByStatusIn(List<TarotDrawStatus> statuses);

    long countBySelectedRewardRarityAndStatusIn(TarotRarity rarity, List<TarotDrawStatus> statuses);
}
