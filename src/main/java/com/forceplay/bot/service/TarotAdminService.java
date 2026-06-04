package com.forceplay.bot.service;

import com.forceplay.bot.dto.tarot.TarotAdminDto;
import com.forceplay.bot.dto.tarot.TelegramWebAppUser;
import com.forceplay.bot.model.TarotArcana;
import com.forceplay.bot.model.TarotDeck;
import com.forceplay.bot.model.TarotReward;
import com.forceplay.bot.model.TarotSeason;
import com.forceplay.bot.repository.TarotArcanaRepository;
import com.forceplay.bot.repository.TarotDeckRepository;
import com.forceplay.bot.repository.TarotRewardRepository;
import com.forceplay.bot.repository.TarotSeasonRepository;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TarotAdminService {

    private final TelegramWebAppAuthService authService;
    private final AdminService adminService;
    private final TarotSeasonRepository seasonRepository;
    private final TarotDeckRepository deckRepository;
    private final TarotArcanaRepository arcanaRepository;
    private final TarotRewardRepository rewardRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> seasons(String initData) {
        requireAdmin(initData);
        return jdbcTemplate.queryForList("select id, name, description, start_date, end_date, active from tarot_seasons order by id");
    }

    @Transactional
    public TarotSeason saveSeason(String initData, Long id, TarotAdminDto.SeasonRequest request) {
        requireAdmin(initData);
        TarotSeason season = id == null ? new TarotSeason() : seasonRepository.findById(id)
                .orElseThrow(() -> new ForcePlayException("Сезон не найден."));
        season.setName(required(request.name(), "Название сезона обязательно."));
        season.setDescription(request.description());
        season.setStartDate(request.startDate() == null ? OffsetDateTime.now() : request.startDate());
        season.setEndDate(request.endDate());
        season.setActive(request.active());
        return seasonRepository.save(season);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> decks(String initData) {
        requireAdmin(initData);
        return jdbcTemplate.queryForList("select id, name, description, active, season_id from tarot_decks order by id");
    }

    @Transactional
    public TarotDeck saveDeck(String initData, Long id, TarotAdminDto.DeckRequest request) {
        requireAdmin(initData);
        TarotDeck deck = id == null ? new TarotDeck() : deckRepository.findById(id)
                .orElseThrow(() -> new ForcePlayException("Колода не найдена."));
        deck.setName(required(request.name(), "Название колоды обязательно."));
        deck.setDescription(request.description());
        deck.setActive(request.active());
        deck.setSeason(season(request.seasonId()));
        return deckRepository.save(deck);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> arcana(String initData) {
        requireAdmin(initData);
        return jdbcTemplate.queryForList("select id, name, image, description, rarity, season_id from tarot_arcana order by id");
    }

    @Transactional
    public TarotArcana saveArcana(String initData, Long id, TarotAdminDto.ArcanaRequest request) {
        requireAdmin(initData);
        TarotArcana arcana = id == null ? new TarotArcana() : arcanaRepository.findById(id)
                .orElseThrow(() -> new ForcePlayException("Реликвия не найдена."));
        arcana.setName(required(request.name(), "Название аркана обязательно."));
        arcana.setImage(request.image());
        arcana.setDescription(request.description());
        arcana.setRarity(request.rarity());
        arcana.setSeason(season(request.seasonId()));
        return arcanaRepository.save(arcana);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> rewards(String initData) {
        requireAdmin(initData);
        return jdbcTemplate.queryForList("select id, name, description, icon, rarity, weight, enabled, season_id from tarot_rewards order by id");
    }

    @Transactional
    public TarotReward saveReward(String initData, Long id, TarotAdminDto.RewardRequest request) {
        requireAdmin(initData);
        TarotReward reward = id == null ? new TarotReward() : rewardRepository.findById(id)
                .orElseThrow(() -> new ForcePlayException("Награда не найдена."));
        reward.setName(required(request.name(), "Название награды обязательно."));
        reward.setDescription(request.description());
        reward.setIcon(request.icon());
        reward.setRarity(request.rarity());
        reward.setWeight(Math.max(0, request.weight()));
        reward.setEnabled(request.enabled());
        reward.setSeason(season(request.seasonId()));
        return rewardRepository.save(reward);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> settings(String initData) {
        requireAdmin(initData);
        return jdbcTemplate.queryForList("select key, value from tarot_settings order by key");
    }

    @Transactional
    public void saveSetting(String initData, String key, TarotAdminDto.SettingRequest request) {
        requireAdmin(initData);
        jdbcTemplate.update("""
                insert into tarot_settings(key, value) values (?, ?)
                on conflict (key) do update set value = excluded.value
                """, key, required(request.value(), "Значение настройки обязательно."));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> packages(String initData) {
        requireAdmin(initData);
        return jdbcTemplate.queryForList("select code, draws, stars_price, enabled from tarot_purchase_packages order by draws");
    }

    @Transactional
    public void savePackage(String initData, String code, TarotAdminDto.PackageRequest request) {
        requireAdmin(initData);
        jdbcTemplate.update("""
                insert into tarot_purchase_packages(code, draws, stars_price, enabled) values (?, ?, ?, ?)
                on conflict (code) do update set draws = excluded.draws, stars_price = excluded.stars_price, enabled = excluded.enabled
                """, code, request.draws(), request.starsPrice(), request.enabled());
    }

    private TarotSeason season(Long seasonId) {
        if (seasonId == null) {
            throw new ForcePlayException("seasonId обязателен.");
        }
        return seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ForcePlayException("Сезон не найден."));
    }

    private void requireAdmin(String initData) {
        TelegramWebAppUser user = authService.authenticate(initData);
        if (!adminService.isAdmin(user.id())) {
            throw new ForcePlayException("Недостаточно прав.");
        }
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ForcePlayException(message);
        }
        return value;
    }
}
