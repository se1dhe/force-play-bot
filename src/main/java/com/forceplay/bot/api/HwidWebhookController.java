package com.forceplay.bot.api;

import com.forceplay.bot.dto.HwidWebhookRequest;
import com.forceplay.bot.model.HwidRequest;
import com.forceplay.bot.service.HwidService;
import com.forceplay.bot.service.InlineMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/hwid")
@RequiredArgsConstructor
public class HwidWebhookController {

    private final HwidService hwidService;
    private final InlineMenuService inlineMenuService;

    @PostMapping
    public ResponseEntity<Void> createRequest(@Valid @RequestBody HwidWebhookRequest request) {
        HwidRequest saved = hwidService.createRequest(request.serverName(), request.externalAccountId(), request.newHwid());
        inlineMenuService.sendHwidNotification(saved);
        return ResponseEntity.accepted().build();
    }
}
