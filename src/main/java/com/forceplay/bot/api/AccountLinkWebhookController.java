package com.forceplay.bot.api;

import com.forceplay.bot.dto.LinkConfirmedWebhookRequest;
import com.forceplay.bot.service.AccountLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/link")
@RequiredArgsConstructor
public class AccountLinkWebhookController {

    private final AccountLinkService accountLinkService;

    @PostMapping("/confirmed")
    public ResponseEntity<Void> confirm(@Valid @RequestBody LinkConfirmedWebhookRequest request) {
        accountLinkService.confirmLink(request.telegramUserId(), "ru", request.serverName(), request.requestId());
        return ResponseEntity.accepted().build();
    }
}
