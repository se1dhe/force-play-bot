package com.forceplay.bot.repository;

import com.forceplay.bot.model.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    boolean existsByUserTelegramIdAndReferredUserTelegramId(Long userTelegramId, Long referredTelegramId);
}
