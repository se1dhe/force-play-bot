package com.forceplay.bot.api;

import com.forceplay.bot.dto.IssueDailyPromoRequest;
import com.forceplay.bot.dto.IssueDailyPromoResponse;
import com.forceplay.bot.service.DailyPromoDistributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/promos")
@RequiredArgsConstructor
public class PromoWebhookController {

    private final DailyPromoDistributionService dailyPromoDistributionService;

    @PostMapping("/issue-daily")
    public ResponseEntity<IssueDailyPromoResponse> issueDaily(@Valid @RequestBody IssueDailyPromoRequest request) {
        String code = dailyPromoDistributionService.issueDailyPromoToUser(request.telegramId());
        return ResponseEntity.ok(new IssueDailyPromoResponse(request.telegramId(), code));
    }
}
