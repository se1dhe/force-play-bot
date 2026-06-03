package com.forceplay.bot.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "forceplay.bot")
public class TelegramBotProperties {

    @NotBlank
    private String username;

    @NotBlank
    private String token;

    @NotBlank
    private String channelUsername;

    private String discordUrl = "https://forceplay.org/discordlink";

    private String telegramChatUrl = "https://t.me/forceplay_chat";

    private String telegramChannelUrl = "https://t.me/forceplay";

    private String shopUrl = "https://forceplay.org/ru/panel/donations";

    private String updaterUrl = "https://forceplay.org/ForcePlay.zip";

    private String supportUrl = "https://forceplay.org/panel/support";

    private String tarotWebappUrl = "http://localhost:8081/tarot/";

    private List<Long> adminIds = List.of();

    private long hwidTtlSeconds = 120;

    private boolean referralEnabled = true;

    private long referralItemId = 57L;

    private int referralItemCount = 1;

    private long bonusItemId = 57L;

    private int bonusItemCount = 1;

    private String promoFilePath = "promo.txt";

    private String promoDailyCron = "0 0 12 * * *";

    private int promoLowStockThreshold = 100;

    private String autofarmQuizFilePath = "autofarm-quiz.json";

    private int rateLimitPerMinute = 30;

    private String integrationMode = "mock";

    private boolean pollingEnabled = true;

    private boolean demoDataEnabled = false;

    private Long demoUserTelegramId;

    private String demoUserLanguage = "ru";
}
