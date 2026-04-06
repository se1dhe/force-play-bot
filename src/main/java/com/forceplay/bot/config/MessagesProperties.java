package com.forceplay.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "forceplay.messages")
public class MessagesProperties {

    private Map<String, String> texts = new HashMap<>();
}
