package com.forceplay.bot.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@ConfigurationProperties(prefix = "forceplay.lineage")
public class LineageServersProperties {

    private List<Server> servers = new ArrayList<>();

    public Optional<Server> findByName(String name) {
        return servers.stream()
                .filter(server -> server.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Data
    public static class Server {
        @NotBlank
        private String name;
        @NotBlank
        private String baseUrl;
        @NotBlank
        private String token;
    }
}
