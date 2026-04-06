package com.forceplay.bot;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.config.MessagesProperties;
import com.forceplay.bot.config.TelegramBotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        TelegramBotProperties.class,
        LineageServersProperties.class,
        MessagesProperties.class
})
public class ForcePlayBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForcePlayBotApplication.class, args);
    }
}
