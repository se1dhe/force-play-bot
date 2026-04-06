package com.forceplay.bot.scheduler;

import com.forceplay.bot.service.HwidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HwidExpirationScheduler {

    private final HwidService hwidService;

    @Scheduled(fixedDelayString = "${forceplay.bot.hwid-ttl-seconds:120}000")
    public void expireRequests() {
        int expired = hwidService.expirePendingRequests();
        if (expired > 0) {
            log.info("Expired {} HWID requests", expired);
        }
    }
}
