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

    private List<Long> adminIds = List.of();

    private long hwidTtlSeconds = 120;

    private boolean referralEnabled = true;

    private long referralItemId = 57L;

    private int referralItemCount = 1;

    private int rateLimitPerMinute = 30;

    private String integrationMode = "mock";

    private boolean pollingEnabled = true;
}
