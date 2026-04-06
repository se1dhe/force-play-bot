package com.forceplay.bot.service;

import com.forceplay.bot.dto.BossInfo;
import com.forceplay.bot.dto.EventInfo;
import com.forceplay.bot.integration.LineageApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InfoService {

    private final LineageApiService lineageApiService;

    public List<BossInfo> getBosses(String serverName) {
        return lineageApiService.getBosses(serverName);
    }

    public List<EventInfo> getEvents(String serverName) {
        return lineageApiService.getEvents(serverName);
    }
}
