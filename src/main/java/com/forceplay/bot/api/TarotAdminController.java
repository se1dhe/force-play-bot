package com.forceplay.bot.api;

import com.forceplay.bot.dto.tarot.TarotAdminDto;
import com.forceplay.bot.dto.tarot.TarotDto;
import com.forceplay.bot.model.TarotArcana;
import com.forceplay.bot.model.TarotDeck;
import com.forceplay.bot.model.TarotReward;
import com.forceplay.bot.model.TarotSeason;
import com.forceplay.bot.service.TarotAdminService;
import com.forceplay.bot.service.TarotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tarot")
@RequiredArgsConstructor
public class TarotAdminController {

    private final TarotService tarotService;
    private final TarotAdminService tarotAdminService;

    @GetMapping("/stats")
    public TarotDto.AdminStatsResponse stats(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotService.adminStats(initData);
    }

    @GetMapping("/seasons")
    public List<Map<String, Object>> seasons(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotAdminService.seasons(initData);
    }

    @PostMapping("/seasons")
    public TarotSeason createSeason(@RequestHeader("X-Telegram-Init-Data") String initData,
                                    @RequestBody TarotAdminDto.SeasonRequest request) {
        return tarotAdminService.saveSeason(initData, null, request);
    }

    @PutMapping("/seasons/{id}")
    public TarotSeason updateSeason(@RequestHeader("X-Telegram-Init-Data") String initData,
                                    @PathVariable Long id,
                                    @RequestBody TarotAdminDto.SeasonRequest request) {
        return tarotAdminService.saveSeason(initData, id, request);
    }

    @GetMapping("/decks")
    public List<Map<String, Object>> decks(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotAdminService.decks(initData);
    }

    @PostMapping("/decks")
    public TarotDeck createDeck(@RequestHeader("X-Telegram-Init-Data") String initData,
                                @RequestBody TarotAdminDto.DeckRequest request) {
        return tarotAdminService.saveDeck(initData, null, request);
    }

    @PutMapping("/decks/{id}")
    public TarotDeck updateDeck(@RequestHeader("X-Telegram-Init-Data") String initData,
                                @PathVariable Long id,
                                @RequestBody TarotAdminDto.DeckRequest request) {
        return tarotAdminService.saveDeck(initData, id, request);
    }

    @GetMapping("/arcana")
    public List<Map<String, Object>> arcana(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotAdminService.arcana(initData);
    }

    @PostMapping("/arcana")
    public TarotArcana createArcana(@RequestHeader("X-Telegram-Init-Data") String initData,
                                    @RequestBody TarotAdminDto.ArcanaRequest request) {
        return tarotAdminService.saveArcana(initData, null, request);
    }

    @PutMapping("/arcana/{id}")
    public TarotArcana updateArcana(@RequestHeader("X-Telegram-Init-Data") String initData,
                                    @PathVariable Long id,
                                    @RequestBody TarotAdminDto.ArcanaRequest request) {
        return tarotAdminService.saveArcana(initData, id, request);
    }

    @GetMapping("/rewards")
    public List<Map<String, Object>> rewards(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotAdminService.rewards(initData);
    }

    @PostMapping("/rewards")
    public TarotReward createReward(@RequestHeader("X-Telegram-Init-Data") String initData,
                                    @RequestBody TarotAdminDto.RewardRequest request) {
        return tarotAdminService.saveReward(initData, null, request);
    }

    @PutMapping("/rewards/{id}")
    public TarotReward updateReward(@RequestHeader("X-Telegram-Init-Data") String initData,
                                    @PathVariable Long id,
                                    @RequestBody TarotAdminDto.RewardRequest request) {
        return tarotAdminService.saveReward(initData, id, request);
    }

    @GetMapping("/settings")
    public List<Map<String, Object>> settings(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotAdminService.settings(initData);
    }

    @PutMapping("/settings/{key}")
    public void updateSetting(@RequestHeader("X-Telegram-Init-Data") String initData,
                              @PathVariable String key,
                              @RequestBody TarotAdminDto.SettingRequest request) {
        tarotAdminService.saveSetting(initData, key, request);
    }

    @GetMapping("/packages")
    public List<Map<String, Object>> packages(@RequestHeader("X-Telegram-Init-Data") String initData) {
        return tarotAdminService.packages(initData);
    }

    @PutMapping("/packages/{code}")
    public void updatePackage(@RequestHeader("X-Telegram-Init-Data") String initData,
                              @PathVariable String code,
                              @RequestBody TarotAdminDto.PackageRequest request) {
        tarotAdminService.savePackage(initData, code, request);
    }
}
