package com.forceplay.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarot_draws")
public class TarotDraw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private TarotSeason season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private TarotDeck deck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TarotDrawStatus status;

    @Column(name = "guaranteed_royal", nullable = false)
    private boolean guaranteedRoyal;

    @Column(name = "selected_card_index")
    private Integer selectedCardIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_reward_id")
    private TarotReward selectedReward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_arcana_id")
    private TarotArcana selectedArcana;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "revealed_at")
    private OffsetDateTime revealedAt;

    @Column(name = "claimed_at")
    private OffsetDateTime claimedAt;

    @Version
    private Long version;
}
