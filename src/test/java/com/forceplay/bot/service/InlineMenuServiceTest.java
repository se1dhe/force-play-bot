package com.forceplay.bot.service;

import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.HwidUnlinkResult;
import com.forceplay.bot.dto.TradeKeyResult;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.model.User;
import com.forceplay.bot.util.ForcePlayException;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.GameCharacterRepository;
import com.forceplay.bot.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class InlineMenuServiceTest {

    @Mock
    private AccountLinkService accountLinkService;
    @Mock
    private PlayerActionService playerActionService;
    @Mock
    private CharacterProfileService characterProfileService;
    @Mock
    private AutofarmService autofarmService;
    @Mock
    private CharacterFunctionService characterFunctionService;
    @Mock
    private AutofarmQuizService autofarmQuizService;
    @Mock
    private AutofarmQuizStateService autofarmQuizStateService;
    @Mock
    private DailyPromoDistributionService dailyPromoDistributionService;
    @Mock
    private AdminService adminService;
    @Mock
    private HwidService hwidService;
    @Mock
    private ReferralService referralService;
    @Mock
    private TelegramBotPropertiesAccessor telegramBotPropertiesAccessor;
    @Mock
    private TelegramGateway telegramGateway;
    @Mock
    private PendingInputStateService pendingInputStateService;
    @Mock
    private InlineKeyboardFactory inlineKeyboardFactory;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private GameCharacterRepository gameCharacterRepository;
    @Mock
    private MessageResolver messageResolver;
    @Mock
    private UserService userService;

    @InjectMocks
    private InlineMenuService inlineMenuService;

    @BeforeEach
    void setUp() {
        lenient().when(messageResolver.get(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(messageResolver.get(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));
        lenient().when(userService.resolveLanguage(any(), any())).thenReturn("ru");
    }

    @Test
    void shouldOpenLinkServerSelection() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(inlineKeyboardFactory.serverSelection("menu|link|server", "menu|main", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|link", 100L, 10, 77L, "ru", "cb-1"));

        verify(telegramGateway).answerCallback("cb-1");
        verify(telegramGateway).editText(100L, 10, "Выберите сервер для привязки аккаунта", keyboard);
    }

    @Test
    void shouldShowPromoScreenWithLatestIssuedCode() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(dailyPromoDistributionService.latestIssuedCode(77L)).thenReturn(Optional.of("VAEXZ59Q"));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|promo", 100L, 10, 77L, "ru", "cb-2"));

        verify(telegramGateway).answerCallback("cb-2");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                Промокод
                
                Последний выданный код: VAEXZ59Q
                Ежедневная раздача выполняется автоматически.
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldShowBonusScreenWithDailyPromoCode() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(userService.findByTelegramId(77L)).thenReturn(Optional.of(User.builder().telegramId(77L).language("ru").build()));
        when(inlineKeyboardFactory.bonusMenu("ru", false)).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|bonus", 100L, 10, 77L, "ru", "cb-bonus-menu"));

        verify(telegramGateway).answerCallback("cb-bonus-menu");
        verify(telegramGateway).editText(
                100L,
                10,
                "📢 Подпишись и получи бонус:",
                keyboard
        );
    }

    @Test
    void shouldShowClaimedStateOnBonusScreen() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(userService.findByTelegramId(77L)).thenReturn(Optional.of(User.builder()
                .telegramId(77L)
                .language("ru")
                .bonusClaimedAt(OffsetDateTime.now().minusDays(1))
                .build()));
        when(inlineKeyboardFactory.bonusMenu("ru", true)).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|bonus", 100L, 10, 77L, "ru", "cb-bonus-claimed"));

        verify(telegramGateway).answerCallback("cb-bonus-claimed");
        verify(telegramGateway).editText(100L, 10, "📢 Подпишись и получи бонус:", keyboard);
    }

    @Test
    void shouldShowDailyPromoOnSeparateScreen() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(userService.findByTelegramId(77L)).thenReturn(Optional.of(User.builder()
                .telegramId(77L)
                .language("ru")
                .bonusClaimedAt(OffsetDateTime.now().minusDays(1))
                .build()));
        when(dailyPromoDistributionService.latestIssuedCode(77L)).thenReturn(Optional.of("VAEXZ59Q"));
        when(inlineKeyboardFactory.backTo("menu|bonus", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|bonus|promo", 100L, 10, 77L, "ru", "cb-bonus-promo"));

        verify(telegramGateway).answerCallback("cb-bonus-promo");
        verify(telegramGateway).editHtml(100L, 10, "🎟 Промокод дня: <code>VAEXZ59Q</code>", keyboard);
    }

    @Test
    void shouldStayOnBonusScreenWhenSubscriptionIsMissing() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(userService.findByTelegramId(77L)).thenReturn(Optional.of(User.builder().telegramId(77L).language("ru").build()));
        when(telegramGateway.isSubscribed(77L, "@forceplay")).thenReturn(false);
        when(telegramBotPropertiesAccessor.channelUsername()).thenReturn("@forceplay");
        when(inlineKeyboardFactory.backTo("menu|bonus", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|bonus|check", 100L, 10, 77L, "ru", "cb-bonus-check-fail"));

        verify(telegramGateway).answerCallback("cb-bonus-check-fail");
        verify(telegramGateway).editText(100L, 10, "Для получения бонуса нужно подписаться на канал ForcePlay.", keyboard);
        verify(telegramGateway, never()).editText(eq(100L), eq(10), eq("Выберите сервер для получения бонуса"), any());
    }

    @Test
    void shouldOpenBonusServerSelectionOnlyWhenSubscriptionExists() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(userService.findByTelegramId(77L)).thenReturn(Optional.of(User.builder().telegramId(77L).language("ru").build()));
        when(telegramGateway.isSubscribed(77L, "@forceplay")).thenReturn(true);
        when(telegramBotPropertiesAccessor.channelUsername()).thenReturn("@forceplay");
        when(inlineKeyboardFactory.serverSelection("menu|bonus|server", "menu|bonus", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|bonus|check", 100L, 10, 77L, "ru", "cb-bonus-check-ok"));

        verify(telegramGateway).answerCallback("cb-bonus-check-ok");
        verify(telegramGateway).editText(100L, 10, "Выберите сервер для получения бонуса", keyboard);
    }

    @Test
    void shouldDenyAdminMenuForRegularUser() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(adminService.isAdmin(77L)).thenReturn(false);
        when(messageResolver.get(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|admin|broadcast", 100L, 10, 77L, "ru", "cb-3"));

        verify(telegramGateway).answerCallback("cb-3", "Недостаточно прав.");
        verify(telegramGateway).editText(100L, 10, "Недостаточно прав.", keyboard);
        verify(pendingInputStateService, never()).save(any(), any(), any());
    }

    @Test
    void shouldPrepareAdminBroadcastForAdmin() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(adminService.isAdmin(77L)).thenReturn(true);
        when(inlineKeyboardFactory.promptBack("menu|main", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|admin|broadcast", 100L, 10, 77L, "ru", "cb-4"));

        verify(pendingInputStateService).save(77L, PendingInputAction.ADMIN_BROADCAST, "");
        verify(telegramGateway).answerCallback("cb-4");
        verify(telegramGateway).editText(100L, 10, "Отправьте следующим сообщением текст, фото, видео или документ для рассылки", keyboard);
    }

    @Test
    void shouldShowHwidStubScreen() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(inlineKeyboardFactory.serverSelection("menu|hwid|server", "menu|main", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|hwid", 100L, 10, 77L, "ru", "cb-hwid"));

        verify(telegramGateway).answerCallback("cb-hwid");
        verify(telegramGateway).editText(
                100L,
                10,
                "Выберите сервер для отвязки HWID",
                keyboard
        );
    }

    @Test
    void shouldOpenHwidAccountSelectionForServer() {
        Account account = account(15L, 77L, "x25_old", "acc-1");
        GameCharacter character = character(25L, 1001L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(accountRepository.findAllByUserTelegramId(77L)).thenReturn(List.of(account));
        when(gameCharacterRepository.findAllByAccountId(15L)).thenReturn(List.of(character));
        when(inlineKeyboardFactory.characterSelectionWithLink(List.of(character), "menu|hwid|character", "x25_old", "menu|hwid", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|hwid|server|x25_old", 100L, 10, 77L, "ru", "cb-hwid-server"));

        verify(telegramGateway).answerCallback("cb-hwid-server");
        verify(telegramGateway).editText(100L, 10, "Выберите персонажа для отвязки HWID", keyboard);
    }

    @Test
    void shouldUnlinkHwidForSelectedCharacter() {
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(playerActionService.unlinkHwid(character)).thenReturn(new HwidUnlinkResult(1001L, true, "HWID отвязан"));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|hwid|character|15", 100L, 10, 77L, "ru", "cb-hwid-char"));

        verify(telegramGateway).answerCallback("cb-hwid-char", "HWID обновлен");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                HWID

                Персонаж: Hero
                HWID отвязан
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldShowReferralScreen() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(telegramBotPropertiesAccessor.username()).thenReturn("forceplay_bot");
        when(referralService.countInvitedUsers(77L)).thenReturn(3L);
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|referrals", 100L, 10, 77L, "ru", "cb-ref"));

        verify(telegramGateway).answerCallback("cb-ref");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                Рефералы

                Ваша ссылка: https://t.me/forceplay_bot?start=ref_77
                Приглашено пользователей: 3
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldShowCharactersAsButtons() {
        Account account = account(99L, 77L, "x25_old", "acc-1259547081");
        GameCharacter main = character(15L, 1001L, 77L, "x25_old", "MainChar", "acc-1259547081");
        GameCharacter spoiler = character(16L, 1002L, 77L, "x25_old", "Spoiler", "acc-1259547081");
        InlineKeyboardMarkup keyboard = emptyKeyboard();

        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(accountRepository.findAllByUserTelegramId(77L)).thenReturn(List.of(account));
        when(inlineKeyboardFactory.serverSelection("menu|characters|server", "menu|main", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|characters", 100L, 10, 77L, "ru", "cb-characters"));

        verify(telegramGateway).answerCallback("cb-characters");
        verify(telegramGateway).editText(100L, 10, "Выбери сервер:", keyboard);
    }

    @Test
    void shouldShowCharactersForSelectedServer() {
        Account account = account(99L, 77L, "x25_old", "acc-1259547081");
        GameCharacter main = character(15L, 1001L, 77L, "x25_old", "MainChar", "acc-1259547081");
        GameCharacter spoiler = character(16L, 1002L, 77L, "x25_old", "Spoiler", "acc-1259547081");
        InlineKeyboardMarkup keyboard = emptyKeyboard();

        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(accountRepository.findAllByUserTelegramId(77L)).thenReturn(List.of(account));
        when(gameCharacterRepository.findAllByAccountId(99L)).thenReturn(List.of(main, spoiler));
        when(inlineKeyboardFactory.characterSelectionWithLink(List.of(main, spoiler), "menu|characters|view", "x25_old", "menu|characters", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|characters|server|x25_old", 100L, 10, 77L, "ru", "cb-characters-server"));

        verify(telegramGateway).answerCallback("cb-characters-server");
        verify(telegramGateway).editText(100L, 10, "Выбери персонажа:", keyboard);
    }

    @Test
    void shouldShowSelectedCharacterDetails() {
        GameCharacter main = character(15L, 1001L, 77L, "x25_old", "MainChar", "acc-1259547081");
        com.forceplay.bot.dto.CharacterProfileResult profile = new com.forceplay.bot.dto.CharacterProfileResult(1001L, 78, "Paladin", 142, 3, "Immortal", true);
        InlineKeyboardMarkup keyboard = emptyKeyboard();

        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(main));
        when(characterProfileService.getProfile(main)).thenReturn(profile);
        when(inlineKeyboardFactory.characterCardMenu(15L, "menu|characters|server|x25_old", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|characters|view|15", 100L, 10, 77L, "ru", "cb-character-view"));

        verify(telegramGateway).answerCallback("cb-character-view");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                ⚔️ MainChar
                Уровень: 78 | Класс: Paladin
                PvP: 142 | PK: 3
                Клан: Immortal
                Статус: 🟢 Онлайн
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldTeleportToTownFromCharacterCard() {
        GameCharacter main = character(15L, 1001L, 77L, "x25_old", "MainChar", "acc-1259547081");
        InlineKeyboardMarkup keyboard = emptyKeyboard();

        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(main));
        when(characterFunctionService.teleportToTown(main)).thenReturn(new com.forceplay.bot.dto.CharacterActionResult(1001L, true, "Ваш персонаж был телепортирован в город"));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|characters|town|15", 100L, 10, 77L, "ru", "cb-character-town"));

        verify(telegramGateway).answerCallback("cb-character-town", "Функция выполнена");
        verify(telegramGateway).editText(100L, 10, """
                В город

                Ваш персонаж был телепортирован в город
                """.trim(), keyboard);
    }

    @Test
    void shouldShowSecurityCharacterCard() {
        Account account = account(99L, 77L, "x25_old", "acc-1");
        account.setHwid("HWID-1");
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Arthas", "acc-1");
        character.setAccount(account);
        InlineKeyboardMarkup keyboard = emptyKeyboard();

        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(inlineKeyboardFactory.securityCharacterMenu(15L, "menu|security|server|x25_old", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|security|character|15", 100L, 10, 77L, "ru", "cb-security"));

        verify(telegramGateway).answerCallback("cb-security");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                🔐 Безопасность — Arthas

                HWID: привязан ✅
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldOpenFunctionsMenu() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(inlineKeyboardFactory.functionsMenu("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|functions", 100L, 10, 77L, "ru", "cb-functions"));

        verify(telegramGateway).answerCallback("cb-functions");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                Функции

                Выберите действие для персонажа.
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldShowDeadAutofarmStatusForSelectedCharacter() {
        GameCharacter character = character(15L, 1002L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(autofarmService.getStatus(character)).thenReturn(new com.forceplay.bot.dto.AutofarmStatusResult(1002L, true, false, false, "PKiller", "Персонаж мертв, автофарм остановлен"));
        when(inlineKeyboardFactory.autofarmActions(15L, true, "menu|autofarm|server|x25_old", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|autofarm|status|15", 100L, 10, 77L, "ru", "cb-autofarm"));

        verify(telegramGateway).answerCallback("cb-autofarm");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                🤖 Автофарм — Hero

                Статус: ❌ Мёртв
                Убил: PKiller
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldShowAliveAutofarmStatusForSelectedCharacter() {
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Arthas", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(autofarmService.getStatus(character)).thenReturn(new com.forceplay.bot.dto.AutofarmStatusResult(1001L, true, true, true, null, "Автофарм работает"));
        when(inlineKeyboardFactory.autofarmActions(15L, false, "menu|autofarm|server|x25_old", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|autofarm|status|15", 100L, 10, 77L, "ru", "cb-autofarm-alive"));

        verify(telegramGateway).answerCallback("cb-autofarm-alive");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                🤖 Автофарм — Arthas

                Статус: ✅ Фармит
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldShowIdleAutofarmStatusForSelectedCharacter() {
        GameCharacter character = character(15L, 1004L, 77L, "x25_old", "DarkMage", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(autofarmService.getStatus(character)).thenReturn(new com.forceplay.bot.dto.AutofarmStatusResult(1004L, true, false, true, null, "Автофарм остановлен"));
        when(inlineKeyboardFactory.autofarmActions(15L, false, "menu|autofarm|server|x25_old", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|autofarm|status|15", 100L, 10, 77L, "ru", "cb-autofarm-idle"));

        verify(telegramGateway).answerCallback("cb-autofarm-idle");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                🤖 Автофарм — DarkMage

                Статус: ⏸ Не фармит
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldReturnToLanguageMenuWhenLanguageNotSelected() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        User user = new User();
        user.setTelegramId(77L);
        user.setLanguage("ru");
        user.setLanguageSelected(false);

        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(userService.findByTelegramId(77L)).thenReturn(Optional.of(user));
        when(inlineKeyboardFactory.languageSelection("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|main", 100L, 10, 77L, "ru", "cb-main"));

        verify(telegramGateway, times(1)).editText(
                100L,
                10,
                "Выбери язык:",
                keyboard
        );
    }

    @Test
    void shouldReviveCharacterAfterCorrectAutofarmQuizAnswer() {
        GameCharacter character = character(15L, 1002L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(autofarmQuizStateService.get(77L)).thenReturn(Optional.of(new AutofarmQuizStateService.QuizState(15L, 2)));
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(autofarmService.revive(character)).thenReturn(new com.forceplay.bot.dto.CharacterActionResult(1002L, true, "Персонаж воскрешен на месте, автофарм снова запущен"));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|autofarm|quiz|2", 100L, 10, 77L, "ru", "cb-quiz"));

        verify(autofarmQuizStateService).clear(77L);
        verify(telegramGateway).answerCallback("cb-quiz", "Персонаж воскрешен");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                Автофарм
                
                Персонаж воскрешен на месте, автофарм снова запущен
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldTeleportCharacterToTown() {
        GameCharacter character = character(15L, 1002L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(characterFunctionService.teleportToTown(character)).thenReturn(new com.forceplay.bot.dto.CharacterActionResult(1002L, true, "Ваш персонаж был телепортирован в город"));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|functions|character|15", 100L, 10, 77L, "ru", "cb-town"));

        verify(telegramGateway).answerCallback("cb-town", "Функция выполнена");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                В город
                
                Ваш персонаж был телепортирован в город
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldOpenBonusConfirmationForSelectedCharacter() {
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(inlineKeyboardFactory.bonusConfirmation(15L, "menu|bonus|server|x25_old", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|bonus|character|15", 100L, 10, 77L, "ru", "cb-bonus-confirm"));

        verify(telegramGateway).answerCallback("cb-bonus-confirm");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                Получение бонуса
                Сервер: x25_old
                Аккаунт: acc-1
                Персонаж: Hero

                Нажмите кнопку ниже, чтобы проверить подписку и забрать бонус.
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldClaimBonusAfterConfirmation() {
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(playerActionService.claimBonus(77L, "ru", character)).thenReturn("Бонус получен");
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|bonus|confirm|15", 100L, 10, 77L, "ru", "cb-bonus-claim"));

        verify(telegramGateway).answerCallback("cb-bonus-claim", "Бонус получен");
        verify(telegramGateway).editText(100L, 10, "Бонус получен", keyboard);
    }

    @Test
    void shouldOpenTradeKeyServerSelection() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(inlineKeyboardFactory.serverSelection("menu|tradekey|server", "menu|main", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|tradekey", 100L, 10, 77L, "ru", "cb-5"));

        verify(telegramGateway).answerCallback("cb-5");
        verify(telegramGateway).editText(100L, 10, "Выберите сервер для смены Trade Key", keyboard);
    }

    @Test
    void shouldOpenNotificationSettings() {
        User user = User.builder()
                .telegramId(77L)
                .language("ru")
                .announceBossSpawn(true)
                .announceEventStart(false)
                .announceAutofarmDeath(true)
                .announceServerRestart(false)
                .announceNewHwidLogin(true)
                .build();
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(userService.getOrCreateUser(77L, "ru")).thenReturn(user);
        when(inlineKeyboardFactory.notificationSettings(21, "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|notifications", 100L, 10, 77L, "ru", "cb-notify"));

        verify(telegramGateway).answerCallback("cb-notify");
        verify(telegramGateway).editText(100L, 10, "Выбери что получать:", keyboard);
    }

    @Test
    void shouldToggleBossNotificationSetting() {
        User user = User.builder()
                .telegramId(77L)
                .language("ru")
                .announceBossSpawn(true)
                .announceEventStart(false)
                .announceAutofarmDeath(true)
                .announceServerRestart(false)
                .announceNewHwidLogin(true)
                .build();
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(inlineKeyboardFactory.notificationSettings(20, "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|notifications|toggle|21|boss", 100L, 10, 77L, "ru", "cb-notify-toggle"));

        verify(telegramGateway).answerCallback("cb-notify-toggle");
        verify(telegramGateway).editText(100L, 10, "Выбери что получать:", keyboard);
    }

    @Test
    void shouldOpenTradeKeyConfirmationForSelectedCharacter() {
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(inlineKeyboardFactory.tradeKeyConfirmation(15L, "menu|tradekey|server|x25_old", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|tradekey|character|15", 100L, 10, 77L, "ru", "cb-5b"));

        verify(telegramGateway).answerCallback("cb-5b");
        verify(telegramGateway).editText(eq(100L), eq(10), eq("""
                Trade Key
                
                Сервер: x25_old
                Аккаунт: acc-1
                Персонаж: Hero
                
                Нажмите кнопку ниже, чтобы ввести новый Trade Key.
                """.trim()), eq(keyboard));
    }

    @Test
    void shouldConfirmLinkAndShowResult() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(accountLinkService.confirmLink(77L, "ru", "x25_old", "rq-1"))
                .thenReturn(new LinkConfirmResult(
                        "acc-1",
                        "x25_old",
                        "HWID-1",
                        List.of(
                                new LinkConfirmResult.LinkedCharacter(1001L, "Hero"),
                                new LinkConfirmResult.LinkedCharacter(1002L, "Mage")
                        )));
        when(messageResolver.get(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|link|confirm|x25_old|rq-1", 100L, 10, 77L, "ru", "cb-6"));

        verify(telegramGateway).answerCallback("cb-6", "Аккаунт привязан");
        verify(telegramGateway).editText(
                eq(100L),
                eq(10),
                eq("Аккаунт успешно привязан.\nАккаунт: acc-1\nПерсонажи: Hero, Mage"),
                eq(keyboard)
        );
    }

    @Test
    void shouldBroadcastTextForPendingAdminInput() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.get(77L))
                .thenReturn(Optional.of(new PendingInputStateService.PendingInputState(PendingInputAction.ADMIN_BROADCAST, "")));
        when(adminService.isAdmin(77L)).thenReturn(true);
        when(adminService.getBroadcastTargets()).thenReturn(List.of(101L, 102L));
        when(messageResolver.get(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handlePendingInput(message(100L, 55, 77L, "ru", "Привет всем"));

        verify(pendingInputStateService).clear(77L);
        verify(telegramGateway).sendText(101L, "Привет всем");
        verify(telegramGateway).sendText(102L, "Привет всем");
        verify(telegramGateway).sendText(100L, "Рассылка завершена.", keyboard);
    }

    @Test
    void shouldCopyMediaForPendingAdminBroadcast() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.get(77L))
                .thenReturn(Optional.of(new PendingInputStateService.PendingInputState(PendingInputAction.ADMIN_BROADCAST, "")));
        when(adminService.isAdmin(77L)).thenReturn(true);
        when(adminService.getBroadcastTargets()).thenReturn(List.of(101L, 102L));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handlePendingInput(messageWithoutText(100L, 55, 77L, "ru"));

        verify(pendingInputStateService).clear(77L);
        verify(telegramGateway).copyMessage(100L, 55, 101L);
        verify(telegramGateway).copyMessage(100L, 55, 102L);
        verify(telegramGateway).sendText(100L, "Рассылка завершена.", keyboard);
    }

    @Test
    void shouldRejectAdminPendingInputForRegularUser() {
        when(pendingInputStateService.get(77L))
                .thenReturn(Optional.of(new PendingInputStateService.PendingInputState(PendingInputAction.ADMIN_BROADCAST, "")));
        when(adminService.isAdmin(77L)).thenReturn(false);

        assertThatThrownBy(() -> inlineMenuService.handlePendingInput(message(100L, 55, 77L, "ru", "Тест")))
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("Недостаточно прав.");

        verify(pendingInputStateService, never()).clear(77L);
        verify(telegramGateway, never()).sendText(eq(100L), any(), any());
    }

    @Test
    void shouldPrepareTradeKeyChangePrompt() {
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(inlineKeyboardFactory.promptBack("menu|tradekey|character|15", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|tradekey|change|15", 100L, 10, 77L, "ru", "cb-7"));

        verify(pendingInputStateService).save(77L, PendingInputAction.TRADE_KEY_CHANGE, "15");
        verify(telegramGateway).answerCallback("cb-7");
        verify(telegramGateway).editText(
                100L,
                10,
                "Введите новым сообщением новый Trade Key для персонажа. Разрешены только латинские буквы и цифры, длина 4-16.",
                keyboard
        );
    }

    @Test
    void shouldChangeTradeKeyFromPendingInput() {
        GameCharacter character = character(15L, 1001L, 77L, "x25_old", "Hero", "acc-1");
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.get(77L))
                .thenReturn(Optional.of(new PendingInputStateService.PendingInputState(PendingInputAction.TRADE_KEY_CHANGE, "15")));
        when(gameCharacterRepository.findByIdAndAccountUserTelegramId(15L, 77L)).thenReturn(Optional.of(character));
        when(playerActionService.changeTradeKey(character, "Test1234"))
                .thenReturn(new TradeKeyResult(1001L, true, "Trade key обновлен"));
        when(inlineKeyboardFactory.resultBackToMain("ru")).thenReturn(keyboard);

        inlineMenuService.handlePendingInput(message(100L, 55, 77L, "ru", "Test1234"));

        verify(pendingInputStateService).clear(77L);
        verify(telegramGateway).sendText(
                100L,
                """
                Trade Key
                
                Сервер: x25_old
                Аккаунт: acc-1
                Персонаж: Hero
                Trade key обновлен
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldRejectLinkPendingInputWhenTextMissing() {
        when(pendingInputStateService.get(77L))
                .thenReturn(Optional.of(new PendingInputStateService.PendingInputState(PendingInputAction.LINK_CHARACTER, "x25_old")));

        assertThatThrownBy(() -> inlineMenuService.handlePendingInput(messageWithoutText(100L, 55, 77L, "ru")))
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("Ожидаю текст с ником персонажа.");

        verify(pendingInputStateService, never()).clear(77L);
        verify(accountLinkService, never()).requestLink(any(), any(), any(), any());
    }

    @Test
    void shouldRejectTradeKeyWhenFormatIsInvalid() {
        when(pendingInputStateService.get(77L))
                .thenReturn(Optional.of(new PendingInputStateService.PendingInputState(PendingInputAction.TRADE_KEY_CHANGE, "15")));

        assertThatThrownBy(() -> inlineMenuService.handlePendingInput(message(100L, 55, 77L, "ru", "bad")))
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("Пароль должен содержать 4-16 латинских букв или цифр.");

        verify(pendingInputStateService, never()).clear(77L);
        verify(playerActionService, never()).changeTradeKey(any(), any());
        verify(telegramGateway, never()).sendText(eq(100L), any(), any());
    }

    @Test
    void shouldShowAdminPromoStats() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        when(pendingInputStateService.exists(77L)).thenReturn(false);
        when(adminService.isAdmin(77L)).thenReturn(true);
        when(dailyPromoDistributionService.getStats()).thenReturn(new DailyPromoDistributionService.PromoStats(87, 100, "0 0 12 * * *"));
        when(inlineKeyboardFactory.backTo("menu|admin", "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|admin|promos", 100L, 10, 77L, "ru", "cb-promos"));

        verify(telegramGateway).answerCallback("cb-promos");
        verify(telegramGateway).editText(
                100L,
                10,
                """
                Промокоды

                Осталось в файле: 87
                Порог уведомления: 100
                Cron раздачи: 0 0 12 * * *
                """.trim(),
                keyboard
        );
    }

    @Test
    void shouldClearPendingInputWhenUserNavigatesByCallback() {
        InlineKeyboardMarkup keyboard = emptyKeyboard();
        User user = new User();
        user.setTelegramId(77L);
        user.setLanguage("ru");
        user.setLanguageSelected(true);

        when(pendingInputStateService.exists(77L)).thenReturn(true);
        when(adminService.isAdmin(77L)).thenReturn(false);
        when(userService.findByTelegramId(77L)).thenReturn(Optional.of(user));
        when(inlineKeyboardFactory.mainMenu(false, "ru")).thenReturn(keyboard);

        inlineMenuService.handleCallback(callback("menu|main", 100L, 10, 77L, "ru", "cb-pending"));

        verify(pendingInputStateService).clear(77L);
        verify(telegramGateway).editText(100L, 10, "AA ✈️", keyboard);
    }

    private CallbackQuery callback(String data, Long chatId, Integer messageId, Long telegramId, String language, String callbackId) {
        org.telegram.telegrambots.meta.api.objects.User from =
                new org.telegram.telegrambots.meta.api.objects.User(telegramId, "user" + telegramId, false);
        from.setLanguageCode(language);

        Message message = new Message();
        message.setMessageId(messageId);
        message.setChat(new Chat(chatId, "private"));

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(callbackId);
        callbackQuery.setData(data);
        callbackQuery.setFrom(from);
        callbackQuery.setMessage(message);
        return callbackQuery;
    }

    private Message message(Long chatId, Integer messageId, Long telegramId, String language, String text) {
        org.telegram.telegrambots.meta.api.objects.User from =
                new org.telegram.telegrambots.meta.api.objects.User(telegramId, "user" + telegramId, false);
        from.setLanguageCode(language);

        Message message = new Message();
        message.setMessageId(messageId);
        message.setChat(new Chat(chatId, "private"));
        message.setFrom(from);
        message.setText(text);
        return message;
    }

    private Message messageWithoutText(Long chatId, Integer messageId, Long telegramId, String language) {
        org.telegram.telegrambots.meta.api.objects.User from =
                new org.telegram.telegrambots.meta.api.objects.User(telegramId, "user" + telegramId, false);
        from.setLanguageCode(language);

        Message message = new Message();
        message.setMessageId(messageId);
        message.setChat(new Chat(chatId, "private"));
        message.setFrom(from);
        return message;
    }

    private Account account(Long accountId, Long telegramId, String serverName, String externalAccountId) {
        User user = User.builder()
                .telegramId(telegramId)
                .build();
        return Account.builder()
                .id(accountId)
                .user(user)
                .serverName(serverName)
                .externalAccountId(externalAccountId)
                .build();
    }

    private GameCharacter character(Long characterId, Long externalCharacterId, Long telegramId, String serverName, String name, String externalAccountId) {
        return GameCharacter.builder()
                .id(characterId)
                .externalCharacterId(externalCharacterId)
                .name(name)
                .account(account(99L, telegramId, serverName, externalAccountId))
                .build();
    }

    private InlineKeyboardMarkup emptyKeyboard() {
        return new InlineKeyboardMarkup(List.<InlineKeyboardRow>of());
    }
}
