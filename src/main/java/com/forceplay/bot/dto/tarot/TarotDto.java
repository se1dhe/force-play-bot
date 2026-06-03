package com.forceplay.bot.dto.tarot;

import com.forceplay.bot.model.TarotRarity;

import java.time.OffsetDateTime;
import java.util.List;

public final class TarotDto {

    private TarotDto() {
    }

    public record AssetConfig(
            String background,
            List<String> cardBacks
    ) {
    }

    public record PurchasePackageDto(
            String code,
            int draws,
            int starsPrice,
            boolean enabled
    ) {
    }

    public record ProfileDto(
            int availableDraws,
            int freeDrawsLimit,
            int freeDrawsUsed,
            int purchasedDraws,
            int pityCounter,
            int pityThreshold,
            long collectionOpened,
            long collectionTotal,
            boolean vip,
            OffsetDateTime vipUntil
    ) {
    }

    public record DrawCardDto(
            int cardIndex,
            boolean revealed,
            boolean selected,
            RewardDto reward,
            ArcanaDto arcana
    ) {
    }

    public record DrawDto(
            Long id,
            String status,
            boolean guaranteedRoyal,
            List<DrawCardDto> cards
    ) {
    }

    public record RewardDto(
            Long id,
            String name,
            String description,
            String icon,
            TarotRarity rarity
    ) {
    }

    public record ArcanaDto(
            Long id,
            String name,
            String image,
            String description,
            TarotRarity rarity
    ) {
    }

    public record StateResponse(
            ProfileDto profile,
            DrawDto activeDraw,
            AssetConfig assets,
            List<PurchasePackageDto> packages
    ) {
    }

    public record RevealRequest(int cardIndex) {
    }

    public record RevealResponse(
            DrawDto draw,
            RewardDto reward,
            ArcanaDto arcana,
            boolean royal,
            boolean newlyCollected,
            ProfileDto profile
    ) {
    }

    public record ClaimResponse(
            Long drawId,
            String status
    ) {
    }

    public record HistoryItemDto(
            Long drawId,
            OffsetDateTime createdAt,
            RewardDto reward,
            ArcanaDto arcana
    ) {
    }

    public record FeedItemDto(
            Long id,
            String username,
            OffsetDateTime createdAt,
            RewardDto reward,
            ArcanaDto arcana
    ) {
    }

    public record InvoiceRequest(String packageCode) {
    }

    public record InvoiceResponse(String invoiceUrl) {
    }

    public record AdminStatsResponse(
            long totalDraws,
            long commonRewards,
            long rareRewards,
            long royalRewards,
            long paidPurchases,
            long starsRevenue
    ) {
    }
}
