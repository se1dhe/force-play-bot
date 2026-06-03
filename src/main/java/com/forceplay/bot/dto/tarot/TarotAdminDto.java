package com.forceplay.bot.dto.tarot;

import com.forceplay.bot.model.TarotRarity;

import java.time.OffsetDateTime;

public final class TarotAdminDto {

    private TarotAdminDto() {
    }

    public record SeasonRequest(String name, String description, OffsetDateTime startDate, OffsetDateTime endDate, boolean active) {
    }

    public record DeckRequest(String name, String description, boolean active, Long seasonId) {
    }

    public record ArcanaRequest(String name, String image, String description, TarotRarity rarity, Long seasonId) {
    }

    public record RewardRequest(String name, String description, String icon, TarotRarity rarity, int weight, boolean enabled, Long seasonId) {
    }

    public record SettingRequest(String value) {
    }

    public record PackageRequest(int draws, int starsPrice, boolean enabled) {
    }
}
