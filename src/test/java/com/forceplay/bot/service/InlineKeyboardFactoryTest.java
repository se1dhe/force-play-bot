package com.forceplay.bot.service;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InlineKeyboardFactoryTest {

    @Test
    void shouldBuildMainMenuWithoutAdminButtonForRegularUser() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old", "x10_new"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.mainMenu(false);

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("🎁 Получить Бонус", "👤 Мои персонажи", "🤖 Автофарм", "🔐 Безопасность", "🔔 Уведомления", "👥 Пригласить друга", "🛒 Магазин", "💬 Discord", "Telegram Чат", "📢 Telegram Канал", "📩 Updater", "🆘 Поддержка", "🌐 Language(RU)");
        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getStyle)
                .contains("primary", "success");
    }

    @Test
    void shouldBuildMainMenuWithCurrentLanguageCode() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.mainMenu(false, "ua");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .contains("🌐 Language(UA)");
    }

    @Test
    void shouldBuildMainMenuWithAdminButtonForAdmin() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.mainMenu(true);

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .contains("Админка");
        assertThat(flattenButtons(keyboard))
                .filteredOn(button -> "Админка".equals(button.getText()))
                .extracting(InlineKeyboardButton::getStyle)
                .containsExactly("primary");
    }

    @Test
    void shouldBuildServerSelectionWithBackButton() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old", "x10_new"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.serverSelection("menu|link|server");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getCallbackData)
                .containsExactly("menu|link|server|x25_old", "menu|link|server|x10_new", "menu|main");
    }

    @Test
    void shouldBuildTradeKeyConfirmation() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.tradeKeyConfirmation(15L, "menu|tradekey");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("Ввести новый Trade Key", "Назад");
        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getStyle)
                .containsExactly("success", "primary");
    }

    @Test
    void shouldBuildPromptBackWithCancelButton() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.promptBack("menu|main");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("Отмена");
        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getStyle)
                .containsExactly("primary");
    }

    @Test
    void shouldBuildBonusConfirmationWithPositiveAndNeutralActions() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.bonusConfirmation(15L, "menu|bonus");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("Получить бонус", "[← Назад]");
        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getStyle)
                .containsExactly("success", "primary");
    }

    @Test
    void shouldBuildHwidDecisionWithSuccessAndDangerStyles() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.hwidDecision(15L);

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("Разрешить", "Запретить");
        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getStyle)
                .containsExactly("success", "danger");
    }

    @Test
    void shouldBuildNotificationSettingsKeyboard() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.notificationSettings(21, "ru");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly(
                        "[✅ Респавн боссов]",
                        "[⬜ Старт ивентов]",
                        "[✅ Автофарм — убили]",
                        "[⬜ Рестарт сервера]",
                        "[✅ Новый HWID вход]",
                        "[💾 Сохранить]",
                        "[← Назад]"
                );
    }

    @Test
    void shouldBuildLanguageSelectionExactlyLikeLanguageScreen() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.languageSelection("ru");

        assertThat(keyboard.getKeyboard()).hasSize(4);
        assertThat(keyboard.getKeyboard())
                .extracting(row -> row.get(0).getText())
                .containsExactly("[🇷🇺 Русский]", "[🇬🇧 English]", "[🇺🇦 Українська]", "[← Назад]");
        assertThat(keyboard.getKeyboard())
                .extracting(row -> row.get(0).getCallbackData())
                .containsExactly("menu|language|set|ru", "menu|language|set|en", "menu|language|set|ua", "menu|main");
    }

    @Test
    void shouldBuildSecurityCharacterMenu() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.securityCharacterMenu(15L, "menu|security|server|x25_old", "ru");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("[💻 Управление HWID]", "[🔑 Сменить TradeKey]", "[← Назад]");
        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getCallbackData)
                .containsExactly("menu|security|hwid|15", "menu|security|tradekey|15", "menu|security|server|x25_old");
    }

    @Test
    void shouldBuildCharacterCardMenu() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.characterCardMenu(15L, "menu|characters|server|x25_old", "ru");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("[🗺️ Телепорт в город]", "[← Назад]");
        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getCallbackData)
                .containsExactly("menu|characters|town|15", "menu|characters|server|x25_old");
    }

    @Test
    void shouldBuildAutofarmActionsForAliveCharacter() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.autofarmActions(15L, false, "menu|autofarm|server|x25_old", "ru");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("[← Назад]");
    }

    @Test
    void shouldBuildAutofarmActionsForDeadCharacter() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.autofarmActions(15L, true, "menu|autofarm|server|x25_old", "ru");

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("[⚔️ Реснуть]", "[← Назад]");
    }

    @Test
    void shouldBuildBonusMenuLikeSubscriptionScreen() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.bonusMenu("ru", false);

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("[📢 ForcePlay Канал]", "[💬 ForcePlay Чат]", "[✅ Проверить подписку]", "[🎟 Промокод дня]", "[← Назад]");
    }

    @Test
    void shouldBuildBonusMenuWithClaimedButtonState() {
        InlineKeyboardFactory factory = new InlineKeyboardFactory(serversProperties("x25_old"), resolver(), accessor());

        InlineKeyboardMarkup keyboard = factory.bonusMenu("ru", true);

        assertThat(flattenButtons(keyboard))
                .extracting(InlineKeyboardButton::getText)
                .containsExactly("[📢 ForcePlay Канал]", "[💬 ForcePlay Чат]", "[⛔ Бонус получен]", "[🎟 Промокод дня]", "[← Назад]");
        assertThat(flattenButtons(keyboard).get(2).getStyle()).isEqualTo("danger");
        assertThat(flattenButtons(keyboard).get(2).getCallbackData()).isEqualTo("menu|bonus");
    }

    private LineageServersProperties serversProperties(String... serverNames) {
        LineageServersProperties properties = new LineageServersProperties();
        properties.setServers(List.of(serverNames).stream().map(name -> {
            LineageServersProperties.Server server = new LineageServersProperties.Server();
            server.setName(name);
            server.setBaseUrl("https://example.com/" + name);
            server.setToken("token-" + name);
            return server;
        }).toList());
        return properties;
    }

    private List<InlineKeyboardButton> flattenButtons(InlineKeyboardMarkup markup) {
        return markup.getKeyboard().stream()
                .flatMap(List::stream)
                .toList();
    }

    private MessageResolver resolver() {
        MessageResolver resolver = mock(MessageResolver.class);
        when(resolver.get(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(resolver.get(anyString(), anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(2));
        return resolver;
    }

    private TelegramBotPropertiesAccessor accessor() {
        TelegramBotPropertiesAccessor accessor = mock(TelegramBotPropertiesAccessor.class);
        when(accessor.shopUrl()).thenReturn("https://example.com/shop");
        when(accessor.discordUrl()).thenReturn("https://example.com/discord");
        when(accessor.telegramChatUrl()).thenReturn("https://example.com/chat");
        when(accessor.telegramChannelUrl()).thenReturn("https://example.com/channel");
        when(accessor.updaterUrl()).thenReturn("https://example.com/updater");
        when(accessor.supportUrl()).thenReturn("https://example.com/support");
        return accessor;
    }
}
