package com.forceplay.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(name = "language_selected", nullable = false)
    @Builder.Default
    private boolean languageSelected = false;

    @Column(name = "announce_boss_spawn", nullable = false)
    @Builder.Default
    private boolean announceBossSpawn = false;

    @Column(name = "announce_event_start", nullable = false)
    @Builder.Default
    private boolean announceEventStart = false;

    @Column(name = "announce_autofarm_death", nullable = false)
    @Builder.Default
    private boolean announceAutofarmDeath = false;

    @Column(name = "announce_server_restart", nullable = false)
    @Builder.Default
    private boolean announceServerRestart = false;

    @Column(name = "announce_new_hwid_login", nullable = false)
    @Builder.Default
    private boolean announceNewHwidLogin = true;

    @Column(name = "bonus_claimed_at")
    private OffsetDateTime bonusClaimedAt;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
