package com.forceplay.bot.api;

import com.forceplay.bot.dto.tarot.TarotDto;
import com.forceplay.bot.model.TarotRarity;
import com.forceplay.bot.service.TarotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tarot")
@RequiredArgsConstructor
public class TarotController {

    private final TarotService tarotService;

    @GetMapping("/state")
    public TarotDto.StateResponse state(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotService.state(initData);
    }

    @PostMapping("/draws/start")
    public TarotDto.DrawDto startDraw(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotService.startDraw(initData);
    }

    @PostMapping("/draws/{drawId}/reveal")
    public TarotDto.RevealResponse reveal(@RequestHeader("X-Telegram-Init-Data") String initData,
                                          @PathVariable Long drawId,
                                          @Valid @RequestBody TarotDto.RevealRequest request) {
        return tarotService.reveal(initData, drawId, request.cardIndex());
    }

    @PostMapping("/draws/{drawId}/claim")
    public TarotDto.ClaimResponse claim(@RequestHeader("X-Telegram-Init-Data") String initData,
                                        @PathVariable Long drawId) {
        return tarotService.claim(initData, drawId);
    }

    @GetMapping("/history")
    public List<TarotDto.HistoryItemDto> history(@RequestHeader("X-Telegram-Init-Data") String initData,
                                                 @RequestParam(required = false) TarotRarity rarity) {
        return tarotService.history(initData, rarity);
    }

    @GetMapping("/feed")
    public List<TarotDto.FeedItemDto> feed() {
        return tarotService.feed();
    }

    @PostMapping("/payments/invoice")
    public TarotDto.InvoiceResponse invoice(@RequestHeader("X-Telegram-Init-Data") String initData,
                                            @Valid @RequestBody TarotDto.InvoiceRequest request) {
        return tarotService.invoice(initData, request.packageCode());
    }
}
