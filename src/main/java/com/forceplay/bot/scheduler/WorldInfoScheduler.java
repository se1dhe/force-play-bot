package com.forceplay.bot.scheduler;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.service.InfoService;
import com.forceplay.bot.service.RedisStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorldInfoScheduler {

    private final LineageServersProperties serversProperties;
    private final InfoService infoService;
    private final RedisStateService redisStateService;

    @Scheduled(cron = "0 */15 * * * *")
    public void pollWorldState() {
        serversProperties.getServers().forEach(server -> {
            infoService.getBosses(server.getName()).forEach(boss ->
                    redisStateService.enqueueNotification("BOSS:" + boss.serverName() + ":" + boss.name()));
            infoService.getEvents(server.getName()).forEach(event ->
                    redisStateService.enqueueNotification("EVENT:" + event.serverName() + ":" + event.title()));
        });
    }
}
