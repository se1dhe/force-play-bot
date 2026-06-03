package com.forceplay.bot.dto.tarot;

public record TelegramWebAppUser(
        Long id,
        String firstName,
        String lastName,
        String username,
        String languageCode
) {
    public String displayName() {
        if (username != null && !username.isBlank()) {
            return "@" + username;
        }
        String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        return fullName.isBlank() ? String.valueOf(id) : fullName;
    }
}
