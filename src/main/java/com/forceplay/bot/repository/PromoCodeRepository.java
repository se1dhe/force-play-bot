package com.forceplay.bot.repository;

import com.forceplay.bot.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    java.util.Optional<PromoCode> findFirstByUsedByTelegramIdOrderByUsedAtDesc(Long telegramId);

    boolean existsByCode(String code);
}
