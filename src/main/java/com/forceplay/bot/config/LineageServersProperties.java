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
    private Endpoints endpoints = new Endpoints();

    public Optional<Server> findByName(String name) {
        return servers.stream()
                .filter(server -> server.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Data
    public static class Server {
        @NotBlank
        private String name;
        private String displayName;
        @NotBlank
        private String baseUrl;
        @NotBlank
        private String token;
    }

    @Data
    public static class Endpoints {
        private String accountLinkRequest = "/api/{server}/account/link/request";
        private String accountLinkConfirm = "/api/{server}/account/link/confirm";
        private String hwidConfirm = "/api/{server}/hwid/confirm";
        private String hwidUnlink = "/api/{server}/hwid/unlink";
        private String tradeKeyChange = "/api/{server}/account/tradekey";
        private String bonusClaim = "/api/{server}/bonus/claim";
        private String characterProfile = "/api/{server}/characters/profile";
        private String autofarmStatus = "/api/{server}/autofarm/status";
        private String autofarmRevive = "/api/{server}/autofarm/revive";
        private String functionTown = "/api/{server}/functions/town";
    }
}
