package com.forceplay.bot.service;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InlineKeyboardFactory {

    private static final String STYLE_PRIMARY = "primary";
    private static final String STYLE_SUCCESS = "success";
    private static final String STYLE_DANGER = "danger";

    private final LineageServersProperties serversProperties;
    private final MessageResolver messageResolver;
    private final TelegramBotPropertiesAccessor botPropertiesAccessor;

    public InlineKeyboardMarkup mainMenu(boolean admin) {
        return mainMenu(admin, "ru");
    }

    public InlineKeyboardMarkup mainMenu(boolean admin, String language) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(button(text(language, "button.bonus", "🎁 Получить Бонус"), "menu|bonus", STYLE_SUCCESS)));
        rows.add(new InlineKeyboardRow(
                button(text(language, "button.characters", "👤 Мои персонажи"), "menu|characters", STYLE_PRIMARY),
                button(text(language, "button.autofarm", "🤖 Автофарм"), "menu|autofarm", STYLE_PRIMARY)
        ));
        rows.add(new InlineKeyboardRow(
                button(text(language, "button.security", "🔐 Безопасность"), "menu|security", STYLE_PRIMARY),
                button(text(language, "button.notifications", "🔔 Уведомления"), "menu|notifications", STYLE_PRIMARY)
        ));
        rows.add(new InlineKeyboardRow(
                button(text(language, "button.referrals", "👥 Пригласить друга"), "menu|referrals", STYLE_PRIMARY),
                linkButton(text(language, "button.shop", "🛒 Магазин"), botPropertiesAccessor.shopUrl(), STYLE_PRIMARY)
        ));
        rows.add(new InlineKeyboardRow(
                linkButton(text(language, "button.discord", "💬 Discord"), botPropertiesAccessor.discordUrl(), STYLE_PRIMARY),
                linkButton(text(language, "button.telegram.chat", "Telegram Чат"), botPropertiesAccessor.telegramChatUrl(), STYLE_PRIMARY),
                linkButton(text(language, "button.telegram.channel", "📢 Telegram Канал"), botPropertiesAccessor.telegramChannelUrl(), STYLE_PRIMARY)
        ));
        rows.add(new InlineKeyboardRow(
                linkButton(text(language, "button.updater", "📩 Updater"), botPropertiesAccessor.updaterUrl(), STYLE_PRIMARY),
                linkButton(text(language, "button.support", "🆘 Поддержка"), botPropertiesAccessor.supportUrl(), STYLE_PRIMARY)
        ));
        rows.add(new InlineKeyboardRow(
                button(languageButtonText(language), "menu|language", STYLE_PRIMARY)
        ));
        if (admin) {
            rows.add(new InlineKeyboardRow(button(text(language, "button.admin", "Админка"), "menu|admin", STYLE_PRIMARY)));
        }
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup languageSelection(String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.language.ru", "[🇷🇺 Русский]"), "menu|language|set|ru", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.language.en", "[🇬🇧 English]"), "menu|language|set|en", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.language.ua", "[🇺🇦 Українська]"), "menu|language|set|ua", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.language.home", "[← Назад]"), "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup serverSelection(String actionPrefix) {
        return serverSelection(actionPrefix, "menu|main", "ru");
    }

    public InlineKeyboardMarkup serverSelection(String actionPrefix, String backTarget) {
        return serverSelection(actionPrefix, backTarget, "ru");
    }

    private String languageButtonText(String language) {
        String code = switch (language == null ? "ru" : language.toLowerCase()) {
            case "en" -> "EN";
            case "ua" -> "UA";
            default -> "RU";
        };
        return text(language, "button.language.current", "🌐 Language(%s)").formatted(code);
    }

    public InlineKeyboardMarkup serverSelection(String actionPrefix, String backTarget, String language) {
        List<InlineKeyboardRow> rows = serversProperties.getServers().stream()
                .map(server -> new InlineKeyboardRow(button(bracketed(serverLabel(server)), actionPrefix + "|" + server.getName(), STYLE_PRIMARY)))
                .toList();
        return withBack(rows, backTarget, language);
    }

    public InlineKeyboardMarkup accountSelection(List<Account> accounts, String actionPrefix) {
        return accountSelection(accounts, actionPrefix, "menu|main", "ru");
    }

    public InlineKeyboardMarkup accountSelection(List<Account> accounts, String actionPrefix, String backTarget) {
        return accountSelection(accounts, actionPrefix, backTarget, "ru");
    }

    public InlineKeyboardMarkup accountSelection(List<Account> accounts, String actionPrefix, String backTarget, String language) {
        List<InlineKeyboardRow> rows = accounts.stream()
                .map(account -> new InlineKeyboardRow(button(
                        account.getServerName() + " / " + account.getExternalAccountId(),
                        actionPrefix + "|" + account.getId(),
                        STYLE_PRIMARY
                )))
                .toList();
        return withBack(rows, backTarget, language);
    }

    public InlineKeyboardMarkup characterSelection(List<GameCharacter> characters, String actionPrefix) {
        return characterSelection(characters, actionPrefix, "menu|main", "ru");
    }

    public InlineKeyboardMarkup characterSelection(List<GameCharacter> characters, String actionPrefix, String backTarget) {
        return characterSelection(characters, actionPrefix, backTarget, "ru");
    }

    public InlineKeyboardMarkup characterSelection(List<GameCharacter> characters, String actionPrefix, String backTarget, String language) {
        List<InlineKeyboardRow> rows = characters.stream()
                .map(character -> new InlineKeyboardRow(button(
                        bracketed(character.getName()),
                        actionPrefix + "|" + character.getId(),
                        STYLE_PRIMARY
                )))
                .toList();
        return withBack(rows, backTarget, language);
    }

    public InlineKeyboardMarkup characterSelectionWithLink(List<GameCharacter> characters, String actionPrefix, String serverName, String backTarget, String language) {
        List<InlineKeyboardRow> rows = new ArrayList<>(characters.stream()
                .map(character -> new InlineKeyboardRow(button(
                        bracketed(character.getName()),
                        actionPrefix + "|" + character.getId(),
                        STYLE_PRIMARY
                )))
                .toList());
        rows.add(new InlineKeyboardRow(
                button(text(language, "button.link.character", "[✝ Привязать персонажа]"), "menu|link|server|" + serverName, STYLE_DANGER)
        ));
        return withBack(rows, backTarget, language);
    }

    public InlineKeyboardMarkup charactersMenu(List<GameCharacter> characters, String language) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(
                button(text(language, "button.link.character", "[✝ Привязать персонажа]"), "menu|link", STYLE_DANGER)
        ));
        rows.addAll(characters.stream()
                .map(character -> new InlineKeyboardRow(button(
                        bracketed(serverLabel(character.getAccount().getServerName()) + " / " + character.getName()),
                        "menu|characters|view|" + character.getId(),
                        STYLE_PRIMARY
                )))
                .toList());
        rows.add(new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), "menu|main", STYLE_PRIMARY)));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup characterCardMenu(Long characterId, String backTarget, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.function.town.card", "[🗺️ Телепорт в город]"), "menu|characters|town|" + characterId, STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), backTarget, STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup confirmLink(String serverName, String requestId, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.confirm.game", "Подтвердить в игре"), "menu|link|confirm|" + serverName + "|" + requestId, STYLE_SUCCESS)),
                new InlineKeyboardRow(button(text(language, "button.back", "Назад"), "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup confirmLink(String serverName, String requestId) {
        return confirmLink(serverName, requestId, "ru");
    }

    public InlineKeyboardMarkup promptBack(String backTarget, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.cancel", "Отмена"), backTarget, STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup promptBack(String backTarget) {
        return promptBack(backTarget, "ru");
    }

    public InlineKeyboardMarkup resultBackToMain(String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.to_menu", "В меню"), "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup resultBackToMain() {
        return resultBackToMain("ru");
    }

    public InlineKeyboardMarkup backTo(String target, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), target, STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup backTo(String target) {
        return backTo(target, "ru");
    }

    public InlineKeyboardMarkup bonusConfirmation(Long characterId, String backTarget, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.bonus.claim", "Получить бонус"), "menu|bonus|confirm|" + characterId, STYLE_SUCCESS)),
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), backTarget, STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup bonusConfirmation(Long characterId, String backTarget) {
        return bonusConfirmation(characterId, backTarget, "ru");
    }

    public InlineKeyboardMarkup hwidDecision(Long requestId, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        button(text(language, "button.hwid.approve", "Разрешить"), "hwid:approve:" + requestId, STYLE_SUCCESS),
                        button(text(language, "button.hwid.deny", "Запретить"), "hwid:deny:" + requestId, STYLE_DANGER)
                )
        ));
    }

    public InlineKeyboardMarkup hwidDecision(Long requestId) {
        return hwidDecision(requestId, "ru");
    }

    public InlineKeyboardMarkup tradeKeyConfirmation(Long characterId, String backTarget, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.tradekey.change", "Ввести новый Trade Key"), "menu|tradekey|change|" + characterId, STYLE_SUCCESS)),
                new InlineKeyboardRow(button(text(language, "button.back", "Назад"), backTarget, STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup tradeKeyConfirmation(Long characterId, String backTarget) {
        return tradeKeyConfirmation(characterId, backTarget, "ru");
    }

    public InlineKeyboardMarkup notificationSettings(int mask, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(notificationText(language, isEnabled(mask, 1), "button.notifications.boss", "[✅ Респавн боссов]", "[⬜ Респавн боссов]"), "menu|notifications|toggle|" + mask + "|boss", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(notificationText(language, isEnabled(mask, 2), "button.notifications.event", "[✅ Старт ивентов]", "[⬜ Старт ивентов]"), "menu|notifications|toggle|" + mask + "|event", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(notificationText(language, isEnabled(mask, 4), "button.notifications.autofarm.death", "[✅ Автофарм — убили]", "[⬜ Автофарм — убили]"), "menu|notifications|toggle|" + mask + "|autofarm", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(notificationText(language, isEnabled(mask, 8), "button.notifications.restart", "[✅ Рестарт сервера]", "[⬜ Рестарт сервера]"), "menu|notifications|toggle|" + mask + "|restart", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(notificationText(language, isEnabled(mask, 16), "button.notifications.hwid", "[✅ Новый HWID вход]", "[⬜ Новый HWID вход]"), "menu|notifications|toggle|" + mask + "|hwid", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.save", "[💾 Сохранить]"), "menu|notifications|save|" + mask, STYLE_SUCCESS)),
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup notificationSettings(boolean announceBossSpawn, boolean announceEventStart) {
        int mask = 0;
        if (announceBossSpawn) {
            mask |= 1;
        }
        if (announceEventStart) {
            mask |= 2;
        }
        return notificationSettings(mask, "ru");
    }

    private boolean isEnabled(int mask, int bit) {
        return (mask & bit) == bit;
    }

    private String notificationText(String language, boolean enabled, String key, String enabledDefault, String disabledDefault) {
        return text(language, enabled ? key + ".enabled" : key + ".disabled", enabled ? enabledDefault : disabledDefault);
    }

    public InlineKeyboardMarkup functionsMenu(String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.function.town", "В город"), "menu|functions|town", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.back", "Назад"), "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup functionsMenu() {
        return functionsMenu("ru");
    }

    public InlineKeyboardMarkup autofarmActions(Long characterId, boolean canRevive, String backTarget, String language) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        if (canRevive) {
            rows.add(new InlineKeyboardRow(button(text(language, "button.autofarm.revive", "[⚔️ Реснуть]"), "menu|autofarm|revive|" + characterId, STYLE_PRIMARY)));
        }
        rows.add(new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), backTarget, STYLE_PRIMARY)));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup autofarmActions(Long characterId, boolean canRevive, String backTarget) {
        return autofarmActions(characterId, canRevive, backTarget, "ru");
    }

    public InlineKeyboardMarkup autofarmQuiz(List<String> answers, String language) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            rows.add(new InlineKeyboardRow(button(answers.get(i), "menu|autofarm|quiz|" + i, STYLE_PRIMARY)));
        }
        rows.add(new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), "menu|autofarm", STYLE_PRIMARY)));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup autofarmQuiz(List<String> answers) {
        return autofarmQuiz(answers, "ru");
    }

    public InlineKeyboardMarkup adminMenu(String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.admin.broadcast", "Рассылка"), "menu|admin|broadcast", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.admin.promos", "Промокоды"), "menu|admin|promos", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup adminMenu() {
        return adminMenu("ru");
    }

    public InlineKeyboardMarkup securityMenu(String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        button(text(language, "button.hwid", "HWID"), "menu|hwid", STYLE_PRIMARY),
                        button(text(language, "button.tradekey", "Trade Key"), "menu|tradekey", STYLE_PRIMARY)
                ),
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup securityCharacterMenu(Long characterId, String backTarget, String language) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(text(language, "button.security.hwid.manage", "[💻 Управление HWID]"), "menu|security|hwid|" + characterId, STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.security.tradekey.change", "[🔑 Сменить TradeKey]"), "menu|security|tradekey|" + characterId, STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), backTarget, STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup bonusMenu(String language, boolean alreadyClaimed) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(linkButton(text(language, "button.bonus.channel", "[📢 ForcePlay Канал]"), botPropertiesAccessor.telegramChannelUrl(), STYLE_PRIMARY)),
                new InlineKeyboardRow(linkButton(text(language, "button.bonus.chat", "[💬 ForcePlay Чат]"), botPropertiesAccessor.telegramChatUrl(), STYLE_PRIMARY)),
                new InlineKeyboardRow(button(
                        alreadyClaimed
                                ? text(language, "button.bonus.claimed", "[⛔ Бонус получен]")
                                : text(language, "button.bonus.check_subscription", "[✅ Проверить подписку]"),
                        alreadyClaimed ? "menu|bonus" : "menu|bonus|check",
                        alreadyClaimed ? STYLE_DANGER : STYLE_SUCCESS
                )),
                new InlineKeyboardRow(button(text(language, "button.bonus.daily_promo", "[🎟 Промокод дня]"), "menu|bonus|promo", STYLE_PRIMARY)),
                new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), "menu|main", STYLE_PRIMARY))
        ));
    }

    private InlineKeyboardMarkup withBack(List<InlineKeyboardRow> rows, String backTarget, String language) {
        List<InlineKeyboardRow> mutableRows = new ArrayList<>(rows);
        mutableRows.add(new InlineKeyboardRow(button(text(language, "button.back.arrow", "[← Назад]"), backTarget, STYLE_PRIMARY)));
        return new InlineKeyboardMarkup(mutableRows);
    }

    private String serverLabel(LineageServersProperties.Server server) {
        return server.getDisplayName() == null || server.getDisplayName().isBlank() ? server.getName() : server.getDisplayName();
    }

    private String serverLabel(String serverName) {
        return serversProperties.findByName(serverName)
                .map(this::serverLabel)
                .orElse(serverName);
    }

    private String bracketed(String value) {
        return "[" + value + "]";
    }

    private String text(String language, String key, String defaultValue) {
        return messageResolver.get(language, key, defaultValue);
    }

    private InlineKeyboardButton button(String text, String callbackData, String style) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        button.setStyle(style);
        return button;
    }

    private InlineKeyboardButton linkButton(String text, String url, String style) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build();
        button.setStyle(style);
        return button;
    }

}
