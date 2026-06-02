package com.forceplay.bot.api;

import com.forceplay.bot.dto.AnnouncementWebhookRequest;
import com.forceplay.bot.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/announcements")
@RequiredArgsConstructor
public class AnnouncementWebhookController {

    private final AnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<Void> publish(@Valid @RequestBody AnnouncementWebhookRequest request) {
        announcementService.announce(request);
        return ResponseEntity.accepted().build();
    }
}
