package com.forceplay.bot.scheduler;

import com.forceplay.bot.service.DailyPromoDistributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyPromoScheduler {

    private final DailyPromoDistributionService dailyPromoDistributionService;

    @Scheduled(cron = "${forceplay.bot.promo-daily-cron:0 0 12 * * *}", zone = "Europe/Kiev")
    public void distributeDailyPromos() {
        DailyPromoDistributionService.PromoDistributionResult result = dailyPromoDistributionService.distributeDailyPromos();
        if (!result.skipped()) {
            log.info("Daily promo distribution finished: assigned={}, remaining={}", result.assignedCount(), result.remainingCount());
        }
    }
}
