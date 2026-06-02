package com.forceplay.bot.service;

import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.dto.HwidUnlinkResult;
import com.forceplay.bot.dto.TradeKeyResult;
import com.forceplay.bot.dto.AutofarmStatusResult;
import com.forceplay.bot.dto.CharacterActionResult;
import com.forceplay.bot.dto.CharacterProfileResult;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.model.HwidRequest;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.GameCharacterRepository;
import com.forceplay.bot.util.ForcePlayException;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InlineMenuService {

    private static final DateTimeFormatter HWID_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Europe/Kiev");

    private final AccountLinkService accountLinkService;
    private final PlayerActionService playerActionService;
    private final CharacterProfileService characterProfileService;
    private final AutofarmService autofarmService;
    private final CharacterFunctionService characterFunctionService;
    private final AutofarmQuizService autofarmQuizService;
    private final AutofarmQuizStateService autofarmQuizStateService;
    private final DailyPromoDistributionService dailyPromoDistributionService;
    private final AdminService adminService;
    private final HwidService hwidService;
    private final ReferralService referralService;
    private final TelegramBotPropertiesAccessor telegramBotPropertiesAccessor;
    private final TelegramGateway telegramGateway;
    private final PendingInputStateService pendingInputStateService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final AccountRepository accountRepository;
    private final GameCharacterRepository gameCharacterRepository;
    private final MessageResolver messageResolver;
    private final UserService userService;

    public void showMainMenu(Long chatId, Long telegramId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        telegramGateway.sendText(chatId, text(language, "menu.main.title", "AA ✈️"), inlineKeyboardFactory.mainMenu(adminService.isAdmin(telegramId), language));
    }

    public void showMainMenu(Long chatId, Integer messageId, Long telegramId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        telegramGateway.editText(chatId, messageId, text(language, "menu.main.title", "AA ✈️"), inlineKeyboardFactory.mainMenu(adminService.isAdmin(telegramId), language));
    }

    public void showLanguageMenu(Long chatId, Long telegramId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        telegramGateway.sendText(chatId, text(language, "language.select.text", "Выбери язык:"), inlineKeyboardFactory.languageSelection(language));
    }

    public void showLanguageMenu(Long chatId, Integer messageId, Long telegramId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        telegramGateway.editText(chatId, messageId, text(language, "language.select.text", "Выбери язык:"), inlineKeyboardFactory.languageSelection(language));
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long telegramId = callbackQuery.getFrom().getId();
        String language = userService.resolveLanguage(telegramId, callbackQuery.getFrom().getLanguageCode());

        if (data.startsWith("hwid:")) {
            handleHwidCallback(callbackQuery, chatId);
            return;
        }

        clearPendingInputIfExists(telegramId);

        String[] parts = data.split("\\|");
        if (!"menu".equals(parts[0])) {
            telegramGateway.answerCallback(callbackQuery.getId(), text(language, "menu.unknown.action", "Неизвестное действие"));
            return;
        }

        switch (parts[1]) {
            case "main" -> {
                if (userService.findByTelegramId(telegramId).map(com.forceplay.bot.model.User::isLanguageSelected).orElse(false)) {
                    showMainMenu(chatId, messageId, telegramId);
                } else {
                    showLanguageMenu(chatId, messageId, telegramId);
                }
            }
            case "language" -> handleLanguageMenu(parts, chatId, messageId, telegramId, callbackQuery.getId(), language);
            case "characters" -> handleCharactersMenu(parts, chatId, messageId, telegramId, callbackQuery.getId(), language);
            case "security" -> handleSecurityMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "link" -> handleLinkMenu(parts, chatId, messageId, telegramId, language, callbackQuery.getId());
            case "hwid" -> handleHwidMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "tradekey" -> handleTradeKeyMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "bonus" -> handleBonusMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "referrals" -> handleReferralMenu(chatId, messageId, telegramId, callbackQuery.getId());
            case "autofarm" -> handleAutofarmMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "functions" -> handleFunctionsMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "notifications" -> handleNotificationMenu(parts, chatId, messageId, telegramId, language, callbackQuery.getId());
            case "promo" -> handlePromoMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "admin" -> handleAdminMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            default -> telegramGateway.answerCallback(callbackQuery.getId(), text(language, "menu.unknown.action", "Неизвестное действие"));
        }
    }

    public boolean hasPendingInput(Long telegramId) {
        return pendingInputStateService.exists(telegramId);
    }

    public void handlePendingInput(Message message) {
        Long chatId = message.getChatId();
        Long telegramId = message.getFrom().getId();
        String language = userService.resolveLanguage(telegramId, message.getFrom().getLanguageCode());
        PendingInputStateService.PendingInputState state = pendingInputStateService.get(telegramId)
                .orElseThrow(() -> new ForcePlayException(text(language, "pending.input.missing", "Нет активного шага ввода")));

        switch (state.action()) {
            case LINK_CHARACTER -> {
                if (!message.hasText()) {
                    throw new ForcePlayException(text(language, "link.character.input.expected", "Ожидаю текст с ником персонажа."));
                }
                pendingInputStateService.clear(telegramId);
                LinkRequestResult result = accountLinkService.requestLink(telegramId, language, state.payload(), message.getText().trim());
                telegramGateway.sendText(
                        chatId,
                        text(language, "link.request.result.template", "%s\nСервер: %s\nПерсонаж: %s\nrequestId: %s").formatted(
                                text(language, "link.requested", "Запрос на привязку аккаунта отправлен."),
                                result.serverName(),
                                result.characterName(),
                                result.requestId()
                        ),
                        inlineKeyboardFactory.confirmLink(result.serverName(), result.requestId(), language)
                );
            }
            case ADMIN_BROADCAST -> {
                if (!adminService.isAdmin(telegramId)) {
                    throw new ForcePlayException(messageResolver.get(language, "access.denied", "Недостаточно прав."));
                }
                pendingInputStateService.clear(telegramId);
                if (message.hasText()) {
                    adminService.getBroadcastTargets().forEach(userId -> telegramGateway.sendText(userId, message.getText().trim()));
                } else {
                    adminService.getBroadcastTargets().forEach(userId -> telegramGateway.copyMessage(chatId, message.getMessageId(), userId));
                }
                telegramGateway.sendText(chatId, messageResolver.get(language, "broadcast.done", "Рассылка завершена."), inlineKeyboardFactory.resultBackToMain(language));
            }
            case TRADE_KEY_CHANGE -> {
                if (!message.hasText()) {
                    throw new ForcePlayException(text(language, "tradekey.input.set", "Введите новый Trade Key."));
                }
                String password = message.getText().trim();
                validateTradeKeyPassword(language, password);
                pendingInputStateService.clear(telegramId);
                GameCharacter character = getUserCharacter(telegramId, Long.parseLong(state.payload()));
                TradeKeyResult result = playerActionService.changeTradeKey(character, password);
                telegramGateway.sendText(chatId, tradeKeyResultMessage(language, character, result), inlineKeyboardFactory.resultBackToMain(language));
            }
        }
    }

    public void sendHwidNotification(HwidRequest request) {
        if (!request.getCharacter().getAccount().getUser().isAnnounceNewHwidLogin()) {
            return;
        }
        String language = request.getCharacter().getAccount().getUser().getLanguage();
        String time = request.getCreatedAt()
                .atZoneSameInstant(DISPLAY_ZONE)
                .format(HWID_TIME_FORMATTER);
        telegramGateway.sendText(
                request.getCharacter().getAccount().getUser().getTelegramId(),
                text(language, "hwid.notification.template", "⚠️ Попытка входа с нового устройства!\nАккаунт: %s\nВремя: %s").formatted(
                        request.getCharacter().getAccount().getExternalAccountId(),
                        time
                ),
                inlineKeyboardFactory.hwidDecision(request.getId(), language)
        );
    }

    private void handleHwidCallback(CallbackQuery callbackQuery, Long chatId) {
        String[] parts = callbackQuery.getData().split(":");
        boolean approved = "approve".equals(parts[1]);
        Long requestId = Long.parseLong(parts[2]);
        String language = userService.resolveLanguage(callbackQuery.getFrom().getId(), callbackQuery.getFrom().getLanguageCode());
        hwidService.resolve(requestId, approved);
        telegramGateway.answerCallback(
                callbackQuery.getId(),
                approved
                        ? text(language, "hwid.resolve.approve.callback", "HWID подтвержден")
                        : text(language, "hwid.resolve.deny.callback", "HWID отклонен")
        );
        telegramGateway.editText(
                chatId,
                callbackQuery.getMessage().getMessageId(),
                approved
                        ? text(language, "hwid.approved", "Новый HWID подтвержден.")
                        : text(language, "hwid.denied", "Запрос на смену HWID отклонен."),
                inlineKeyboardFactory.resultBackToMain(language)
        );
    }

    private void handleLanguageMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId, String language) {
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            showLanguageMenu(chatId, messageId, telegramId);
            return;
        }
        if ("set".equals(parts[2])) {
            String selectedLanguage = parts[3];
            userService.updateLanguage(telegramId, selectedLanguage);
            telegramGateway.answerCallback(callbackId);
            showMainMenu(chatId, messageId, telegramId);
        }
    }

    private void handleCharactersMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId, String language) {
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            if (accountRepository.findAllByUserTelegramId(telegramId).isEmpty()) {
                telegramGateway.editText(
                        chatId,
                        messageId,
                        text(language, "characters.menu.empty", "У вас пока нет привязанных аккаунтов и персонажей."),
                        inlineKeyboardFactory.backTo("menu|main", language)
                );
                return;
            }
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "characters.server.select.text", "Выбери сервер:"),
                    inlineKeyboardFactory.serverSelection("menu|characters|server", "menu|main", language)
            );
            return;
        }

        if ("server".equals(parts[2])) {
            String serverName = parts[3];
            List<GameCharacter> characters = getUserCharactersByServer(telegramId, serverName);
            telegramGateway.answerCallback(callbackId);
            if (characters.isEmpty()) {
                telegramGateway.editText(
                        chatId,
                        messageId,
                        text(language, "characters.server.empty", "На этом сервере пока нет привязанных персонажей."),
                        inlineKeyboardFactory.backTo("menu|characters", language)
                );
                return;
            }
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "characters.menu.select.text", "Выбери персонажа:"),
                    inlineKeyboardFactory.characterSelectionWithLink(characters, "menu|characters|view", serverName, "menu|characters", language)
            );
            return;
        }

        if ("view".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            CharacterProfileResult profile = characterProfileService.getProfile(character);
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    characterProfileMessage(language, character, profile),
                    inlineKeyboardFactory.characterCardMenu(character.getId(), "menu|characters|server|" + character.getAccount().getServerName(), language)
            );
            return;
        }

        if ("town".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            CharacterActionResult result = characterFunctionService.teleportToTown(character);
            telegramGateway.answerCallback(callbackId, text(language, "functions.town.callback", "Функция выполнена"));
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "functions.town.result", "В город\n\n%s").formatted(result.message()),
                    inlineKeyboardFactory.resultBackToMain(language)
            );
        }
    }

    private void handleSecurityMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "security.server.select.text", "Выбери сервер:"),
                    inlineKeyboardFactory.serverSelection("menu|security|server", "menu|main", language)
            );
            return;
        }
        if ("server".equals(parts[2])) {
            String serverName = parts[3];
            List<GameCharacter> characters = getUserCharactersByServer(telegramId, serverName);
            if (characters.isEmpty()) {
                telegramGateway.answerCallback(callbackId, text(language, "security.character.missing.callback", "Сначала привяжите персонажа"));
                telegramGateway.editText(
                        chatId,
                        messageId,
                        text(language, "security.character.missing.text", "У вас нет привязанных персонажей на этом сервере."),
                        inlineKeyboardFactory.resultBackToMain(language)
                );
                return;
            }
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "security.character.select.text", "Выбери персонажа:"),
                    inlineKeyboardFactory.characterSelection(characters, "menu|security|character", "menu|security", language)
            );
            return;
        }
        if ("character".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    securityCharacterMessage(language, character),
                    inlineKeyboardFactory.securityCharacterMenu(character.getId(), "menu|security|server|" + character.getAccount().getServerName(), language)
            );
            return;
        }
        if ("hwid".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            HwidUnlinkResult result = playerActionService.unlinkHwid(character);
            telegramGateway.answerCallback(callbackId, text(language, "hwid.unlink.callback", "HWID обновлен"));
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "hwid.unlink.result", "HWID\n\nПерсонаж: %s\n%s").formatted(character.getName(), result.message()),
                    inlineKeyboardFactory.backTo("menu|security|character|" + character.getId(), language)
            );
            return;
        }
        if ("tradekey".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            pendingInputStateService.save(telegramId, PendingInputAction.TRADE_KEY_CHANGE, String.valueOf(character.getId()));
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "tradekey.change.prompt", "Введите новым сообщением новый Trade Key для персонажа. Разрешены только латинские буквы и цифры, длина 4-16."),
                    inlineKeyboardFactory.promptBack("menu|security|character|" + character.getId(), language)
            );
        }
    }

    private void handleNotificationMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String language, String callbackId) {
        if (parts.length == 2) {
            showNotificationSettings(chatId, messageId, callbackId, telegramId, language);
            return;
        }
        if ("toggle".equals(parts[2])) {
            int mask = Integer.parseInt(parts[3]);
            int updatedMask = switch (parts[4]) {
                case "boss" -> mask ^ 1;
                case "event" -> mask ^ 2;
                case "autofarm" -> mask ^ 4;
                case "restart" -> mask ^ 8;
                case "hwid" -> mask ^ 16;
                default -> -1;
            };
            if (updatedMask < 0) {
                telegramGateway.answerCallback(callbackId, text(language, "menu.unknown.action", "Неизвестное действие"));
                return;
            }
            telegramGateway.answerCallback(callbackId);
            showNotificationSettings(chatId, messageId, null, telegramId, language, updatedMask);
            return;
        }
        if ("save".equals(parts[2])) {
            int mask = Integer.parseInt(parts[3]);
            userService.saveNotificationSettings(
                    telegramId,
                    language,
                    maskEnabled(mask, 1),
                    maskEnabled(mask, 2),
                    maskEnabled(mask, 4),
                    maskEnabled(mask, 8),
                    maskEnabled(mask, 16)
            );
            telegramGateway.answerCallback(callbackId);
            showNotificationSettings(chatId, messageId, null, telegramId, language, mask);
        }
    }

    private void handleHwidMenu(Long chatId, Integer messageId, String callbackId, String language) {
        telegramGateway.answerCallback(callbackId);
        telegramGateway.editText(chatId, messageId, text(language, "hwid.server.select.text", "Выберите сервер для отвязки HWID"), inlineKeyboardFactory.serverSelection("menu|hwid|server", "menu|main", language));
    }

    private void handleHwidMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        if (parts.length == 2) {
            handleHwidMenu(chatId, messageId, callbackId, language);
            return;
        }
        if ("server".equals(parts[2])) {
            String serverName = parts[3];
            List<GameCharacter> characters = getUserCharactersByServer(telegramId, serverName);
            if (characters.isEmpty()) {
                telegramGateway.answerCallback(callbackId, text(language, "hwid.account.missing.callback", "Сначала привяжите аккаунт"));
                telegramGateway.editText(chatId, messageId, text(language, "hwid.account.missing.text", "У вас нет привязанных аккаунтов на этом сервере."), inlineKeyboardFactory.resultBackToMain(language));
                return;
            }
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "hwid.character.select.text", "Выберите персонажа для отвязки HWID"),
                    inlineKeyboardFactory.characterSelectionWithLink(characters, "menu|hwid|character", serverName, "menu|hwid", language)
            );
            return;
        }
        if ("character".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            HwidUnlinkResult result = playerActionService.unlinkHwid(character);
            telegramGateway.answerCallback(callbackId, text(language, "hwid.unlink.callback", "HWID обновлен"));
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "hwid.unlink.result", "HWID\n\nПерсонаж: %s\n%s").formatted(character.getName(), result.message()),
                    inlineKeyboardFactory.resultBackToMain(language)
            );
        }
    }

    private void handleReferralMenu(Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        telegramGateway.answerCallback(callbackId);
        String botUsername = telegramBotPropertiesAccessor.username();
        if (botUsername == null || botUsername.isBlank()) {
            botUsername = "forceplay_bot";
        }
        String referralLink = "https://t.me/" + botUsername.replace("@", "") + "?start=ref_" + telegramId;
        long invited = referralService.countInvitedUsers(telegramId);
        telegramGateway.editText(
                chatId,
                messageId,
                text(language, "referrals.menu.text", "Рефералы\n\nВаша ссылка: %s\nПриглашено пользователей: %d").formatted(referralLink, invited),
                inlineKeyboardFactory.resultBackToMain(language)
        );
    }

    private void handleAutofarmMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "autofarm.server.select.text", "Выберите сервер для проверки автофарма"),
                    inlineKeyboardFactory.serverSelection("menu|autofarm|server", "menu|main", language)
            );
            return;
        }
        if ("server".equals(parts[2])) {
            String serverName = parts[3];
            List<GameCharacter> characters = getUserCharactersByServer(telegramId, serverName);
            if (characters.isEmpty()) {
                telegramGateway.answerCallback(callbackId, text(language, "autofarm.account.missing.callback", "Сначала привяжите аккаунт"));
                telegramGateway.editText(chatId, messageId, text(language, "autofarm.account.missing.text", "У вас нет привязанных аккаунтов на этом сервере."), inlineKeyboardFactory.resultBackToMain(language));
                return;
            }
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "autofarm.character.select.text", "Выберите персонажа для проверки автофарма"),
                    inlineKeyboardFactory.characterSelection(characters, "menu|autofarm|status", "menu|autofarm", language)
            );
            return;
        }
        if ("status".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            AutofarmStatusResult status = autofarmService.getStatus(character);
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    autofarmStatusMessage(language, character, status),
                    inlineKeyboardFactory.autofarmActions(character.getId(), !status.alive(), "menu|autofarm|server|" + character.getAccount().getServerName(), language)
            );
            return;
        }
        if ("revive".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            AutofarmQuizService.QuizQuestion question = autofarmQuizService.randomQuestion();
            autofarmQuizStateService.save(telegramId, character.getId(), question.correctAnswerIndex());
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "autofarm.quiz.text", "Викторина для воскрешения\n\n%s").formatted(question.question()),
                    inlineKeyboardFactory.autofarmQuiz(question.answers(), language)
            );
            return;
        }
        if ("quiz".equals(parts[2])) {
            AutofarmQuizStateService.QuizState quizState = autofarmQuizStateService.get(telegramId)
                    .orElseThrow(() -> new ForcePlayException(text(language, "autofarm.quiz.missing", "Нет активной викторины.")));
            int answerIndex = Integer.parseInt(parts[3]);
            autofarmQuizStateService.clear(telegramId);
            GameCharacter character = getUserCharacter(telegramId, quizState.characterId());
            if (quizState.correctAnswerIndex() != answerIndex) {
                telegramGateway.answerCallback(callbackId, text(language, "autofarm.quiz.failed.callback", "Неверный ответ"));
                telegramGateway.editText(chatId, messageId, text(language, "autofarm.quiz.failed.text", "Неверный ответ. Попробуйте снова открыть автофарм и повторить попытку."), inlineKeyboardFactory.resultBackToMain(language));
                return;
            }
            CharacterActionResult result = autofarmService.revive(character);
            telegramGateway.answerCallback(callbackId, text(language, "autofarm.quiz.success.callback", "Персонаж воскрешен"));
            telegramGateway.editText(chatId, messageId, text(language, "autofarm.revive.result", "Автофарм\n\n%s").formatted(result.message()), inlineKeyboardFactory.resultBackToMain(language));
        }
    }

    private void handleFunctionsMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "functions.menu.text", "Функции\n\nВыберите действие для персонажа."),
                    inlineKeyboardFactory.functionsMenu(language)
            );
            return;
        }
        if ("town".equals(parts[2])) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "functions.server.select.text", "Выберите сервер для телепорта в город"),
                    inlineKeyboardFactory.serverSelection("menu|functions|server", "menu|functions", language)
            );
            return;
        }
        if ("server".equals(parts[2])) {
            String serverName = parts[3];
            List<GameCharacter> characters = getUserCharactersByServer(telegramId, serverName);
            if (characters.isEmpty()) {
                telegramGateway.answerCallback(callbackId, text(language, "functions.account.missing.callback", "Сначала привяжите аккаунт"));
                telegramGateway.editText(chatId, messageId, text(language, "functions.account.missing.text", "У вас нет привязанных аккаунтов на этом сервере."), inlineKeyboardFactory.resultBackToMain(language));
                return;
            }
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "functions.character.select.text", "Выберите персонажа для телепорта в город"),
                    inlineKeyboardFactory.characterSelectionWithLink(characters, "menu|functions|character", serverName, "menu|functions|town", language)
            );
            return;
        }
        if ("character".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            CharacterActionResult result = characterFunctionService.teleportToTown(character);
            telegramGateway.answerCallback(callbackId, text(language, "functions.town.callback", "Функция выполнена"));
            telegramGateway.editText(chatId, messageId, text(language, "functions.town.result", "В город\n\n%s").formatted(result.message()), inlineKeyboardFactory.resultBackToMain(language));
        }
    }

    private void handleLinkMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String language, String callbackId) {
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(chatId, messageId, text(language, "link.server.select", "Выберите сервер для привязки аккаунта"), inlineKeyboardFactory.serverSelection("menu|link|server", "menu|main", language));
            return;
        }
        if ("server".equals(parts[2])) {
            pendingInputStateService.save(telegramId, PendingInputAction.LINK_CHARACTER, parts[3]);
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(chatId, messageId, text(language, "link.character.prompt.text", "Привязка аккаунта\nСервер: %s\n\nОтправьте ник персонажа для запроса на привязку.").formatted(parts[3]), inlineKeyboardFactory.promptBack("menu|main", language));
            return;
        }
        if ("confirm".equals(parts[2])) {
            LinkConfirmResult result = accountLinkService.confirmLink(telegramId, language, parts[3], parts[4]);
            telegramGateway.answerCallback(callbackId, text(language, "link.confirm.callback", "Аккаунт привязан"));
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "link.result.template", "%s\nАккаунт: %s\nПерсонажи: %s").formatted(
                            text(language, "link.success", "Аккаунт успешно привязан."),
                            result.externalAccountId(),
                            String.join(", ", result.characters())
                    ),
                    inlineKeyboardFactory.resultBackToMain(language)
            );
        }
    }

    private void showNotificationSettings(Long chatId, Integer messageId, String callbackId, Long telegramId, String language) {
        com.forceplay.bot.model.User user = userService.getOrCreateUser(telegramId, language);
        int mask = 0;
        if (user.isAnnounceBossSpawn()) {
            mask |= 1;
        }
        if (user.isAnnounceEventStart()) {
            mask |= 2;
        }
        if (user.isAnnounceAutofarmDeath()) {
            mask |= 4;
        }
        if (user.isAnnounceServerRestart()) {
            mask |= 8;
        }
        if (user.isAnnounceNewHwidLogin()) {
            mask |= 16;
        }
        showNotificationSettings(chatId, messageId, callbackId, telegramId, language, mask);
    }

    private void showNotificationSettings(Long chatId, Integer messageId, String callbackId, Long telegramId, String language, int mask) {
        if (callbackId != null) {
            telegramGateway.answerCallback(callbackId);
        }
        telegramGateway.editText(
                chatId,
                messageId,
                text(language, "notifications.screen.template", "Выбери что получать:"),
                inlineKeyboardFactory.notificationSettings(mask, language)
        );
    }

    private void handleTradeKeyMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(chatId, messageId, text(language, "tradekey.server.select.text", "Выберите сервер для смены Trade Key"), inlineKeyboardFactory.serverSelection("menu|tradekey|server", "menu|main", language));
            return;
        }
        if ("server".equals(parts[2])) {
            String serverName = parts[3];
            List<GameCharacter> characters = getUserCharactersByServer(telegramId, serverName);
            if (characters.isEmpty()) {
                telegramGateway.answerCallback(callbackId, text(language, "tradekey.account.missing.callback", "Сначала привяжите аккаунт"));
                telegramGateway.editText(chatId, messageId, text(language, "tradekey.account.missing.text", "У вас нет привязанных аккаунтов на этом сервере."), inlineKeyboardFactory.resultBackToMain(language));
                return;
            }
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "tradekey.character.select.text", "Выберите персонажа для смены Trade Key"),
                    inlineKeyboardFactory.characterSelectionWithLink(characters, "menu|tradekey|character", serverName, "menu|tradekey", language)
            );
            return;
        }
        if ("character".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "tradekey.character.confirm.text", "Trade Key\n\nСервер: %s\nАккаунт: %s\nПерсонаж: %s\n\nНажмите кнопку ниже, чтобы ввести новый Trade Key.")
                            .formatted(character.getAccount().getServerName(), character.getAccount().getExternalAccountId(), character.getName()),
                    inlineKeyboardFactory.tradeKeyConfirmation(character.getId(), "menu|tradekey|server|" + character.getAccount().getServerName(), language)
            );
            return;
        }
        if ("change".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            pendingInputStateService.save(telegramId, PendingInputAction.TRADE_KEY_CHANGE, String.valueOf(character.getId()));
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "tradekey.change.prompt", "Введите новым сообщением новый Trade Key для персонажа. Разрешены только латинские буквы и цифры, длина 4-16."),
                    inlineKeyboardFactory.promptBack("menu|tradekey|character|" + character.getId(), language)
            );
        }
    }

    private void handleBonusMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        boolean alreadyClaimed = userService.findByTelegramId(telegramId)
                .map(user -> user.getBonusClaimedAt() != null)
                .orElse(false);
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "bonus.menu.text", "📢 Подпишись и получи бонус:"),
                    inlineKeyboardFactory.bonusMenu(language, alreadyClaimed)
            );
            return;
        }
        if ("promo".equals(parts[2])) {
            telegramGateway.answerCallback(callbackId);
            String promoCode = dailyPromoDistributionService.latestIssuedCode(telegramId)
                    .orElse(text(language, "promo.menu.missing", "Пока не выдан"));
            telegramGateway.editHtml(
                    chatId,
                    messageId,
                    text(language, "bonus.promo.text", "🎟 Промокод дня: <code>%s</code>").formatted(promoCode),
                    inlineKeyboardFactory.backTo("menu|bonus", language)
            );
            return;
        }
        if (alreadyClaimed) {
            telegramGateway.answerCallback(callbackId, text(language, "bonus.already.claimed", "Бонус за подписку уже получен."));
            showMainMenu(chatId, messageId, telegramId);
            return;
        }
        if ("check".equals(parts[2])) {
            if (!telegramGateway.isSubscribed(telegramId, telegramBotPropertiesAccessor.channelUsername())) {
                telegramGateway.answerCallback(callbackId);
                telegramGateway.editText(
                        chatId,
                        messageId,
                        text(language, "bonus.subscription.required.screen", "Для получения бонуса нужно подписаться на канал ForcePlay."),
                        inlineKeyboardFactory.backTo("menu|bonus", language)
                );
                return;
            }
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(chatId, messageId, text(language, "bonus.server.select.text", "Выберите сервер для получения бонуса"), inlineKeyboardFactory.serverSelection("menu|bonus|server", "menu|bonus", language));
            return;
        }
        if ("server".equals(parts[2])) {
            String serverName = parts[3];
            List<GameCharacter> characters = getUserCharactersByServer(telegramId, serverName);
            if (characters.isEmpty()) {
                telegramGateway.answerCallback(callbackId, text(language, "bonus.account.missing.callback", "Сначала привяжите аккаунт"));
                telegramGateway.editText(chatId, messageId, text(language, "bonus.account.missing.text", "У вас нет привязанных аккаунтов на этом сервере."), inlineKeyboardFactory.resultBackToMain(language));
                return;
            }
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "bonus.character.select.text", "Выберите персонажа для получения бонуса"),
                    inlineKeyboardFactory.characterSelection(characters, "menu|bonus|character", "menu|bonus|check", language)
            );
            return;
        }
        if ("character".equals(parts[2])) {
            GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "bonus.confirm.text", "Получение бонуса\nСервер: %s\nАккаунт: %s\nПерсонаж: %s\n\nНажмите кнопку ниже, чтобы проверить подписку и забрать бонус.")
                            .formatted(character.getAccount().getServerName(), character.getAccount().getExternalAccountId(), character.getName()),
                    inlineKeyboardFactory.bonusConfirmation(character.getId(), "menu|bonus|server|" + character.getAccount().getServerName(), language)
            );
            return;
        }
        GameCharacter character = getUserCharacter(telegramId, Long.parseLong(parts[3]));
        String result = playerActionService.claimBonus(telegramId, language, character);
        telegramGateway.answerCallback(callbackId, result);
        telegramGateway.editText(chatId, messageId, result, inlineKeyboardFactory.resultBackToMain(language));
    }

    private void handlePromoMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        telegramGateway.answerCallback(callbackId);
        String latestCode = dailyPromoDistributionService.latestIssuedCode(telegramId)
                .orElse(text(language, "promo.menu.missing", "Пока не выдан"));
        telegramGateway.editText(
                chatId,
                messageId,
                text(language, "promo.menu.text", "Промокод\n\nПоследний выданный код: %s\nЕжедневная раздача выполняется автоматически.").formatted(latestCode),
                inlineKeyboardFactory.resultBackToMain(language)
        );
    }

    private void handleAdminMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        String language = userService.resolveLanguage(telegramId, "ru");
        if (!adminService.isAdmin(telegramId)) {
            telegramGateway.answerCallback(callbackId, messageResolver.get(language, "access.denied", "Недостаточно прав."));
            telegramGateway.editText(chatId, messageId, messageResolver.get(language, "access.denied", "Недостаточно прав."), inlineKeyboardFactory.resultBackToMain(language));
            return;
        }
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "admin.menu.text", "Админка\n\nВыберите действие."),
                    inlineKeyboardFactory.adminMenu(language)
            );
            return;
        }
        if ("promos".equals(parts[2])) {
            DailyPromoDistributionService.PromoStats stats = dailyPromoDistributionService.getStats();
            telegramGateway.answerCallback(callbackId);
            telegramGateway.editText(
                    chatId,
                    messageId,
                    text(language, "admin.promos.text", "Промокоды\n\nОсталось в файле: %d\nПорог уведомления: %d\nCron раздачи: %s")
                            .formatted(stats.remainingCount(), stats.lowStockThreshold(), stats.dailyCron()),
                    inlineKeyboardFactory.backTo("menu|admin", language)
            );
            return;
        }
        pendingInputStateService.save(telegramId, PendingInputAction.ADMIN_BROADCAST, "");
        telegramGateway.answerCallback(callbackId);
        telegramGateway.editText(chatId, messageId, text(language, "broadcast.input.prompt", "Отправьте следующим сообщением текст, фото, видео или документ для рассылки"), inlineKeyboardFactory.promptBack("menu|main", language));
    }

    private Account getUserAccount(Long telegramId, Long accountId) {
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().getTelegramId().equals(telegramId))
                .orElseThrow(() -> new ForcePlayException(text(userService.resolveLanguage(telegramId, "ru"), "entity.account.not_found", "Аккаунт не найден")));
    }

    private List<GameCharacter> getUserCharactersByServer(Long telegramId, String serverName) {
        return accountRepository.findAllByUserTelegramId(telegramId).stream()
                .filter(account -> serverName.equals(account.getServerName()))
                .flatMap(account -> gameCharacterRepository.findAllByAccountId(account.getId()).stream())
                .toList();
    }

    private GameCharacter getUserCharacter(Long telegramId, Long characterId) {
        return gameCharacterRepository.findByIdAndAccountUserTelegramId(characterId, telegramId)
                .orElseThrow(() -> new ForcePlayException(text(userService.resolveLanguage(telegramId, "ru"), "entity.character.not_found", "Персонаж не найден")));
    }

    private String tradeKeyResultMessage(String language, GameCharacter character, TradeKeyResult result) {
        return text(language, "tradekey.result.template", "Trade Key\n\nСервер: %s\nАккаунт: %s\nПерсонаж: %s\n%s").formatted(
                character.getAccount().getServerName(),
                character.getAccount().getExternalAccountId(),
                character.getName(),
                result.message()
        );
    }

    private String securityCharacterMessage(String language, GameCharacter character) {
        boolean hwidLinked = character.getAccount().getHwid() != null && !character.getAccount().getHwid().isBlank();
        return text(language, "security.character.text", "🔐 Безопасность — %s\n\nHWID: %s %s").formatted(
                character.getName(),
                hwidLinked
                        ? text(language, "security.hwid.linked", "привязан")
                        : text(language, "security.hwid.unlinked", "не привязан"),
                hwidLinked ? "✅" : "⬜"
        );
    }

    private String autofarmStatusMessage(String language, GameCharacter character, AutofarmStatusResult status) {
        if (!status.alive()) {
            String killer = status.killerName() == null || status.killerName().isBlank()
                    ? text(language, "autofarm.killer.unknown", "Неизвестно")
                    : status.killerName();
            return text(language, "autofarm.status.dead.card", "🤖 Автофарм — %s\n\nСтатус: ❌ Мёртв\nУбил: %s")
                    .formatted(character.getName(), killer);
        }
        if (status.autofarming()) {
            return text(language, "autofarm.status.alive.card", "🤖 Автофарм — %s\n\nСтатус: ✅ Фармит")
                    .formatted(character.getName());
        }
        return text(language, "autofarm.status.idle.card", "🤖 Автофарм — %s\n\nСтатус: ⏸ Не фармит")
                .formatted(character.getName());
    }

    private String characterProfileMessage(String language, GameCharacter character, CharacterProfileResult profile) {
        String clan = profile.clanName() == null || profile.clanName().isBlank()
                ? text(language, "characters.profile.no_clan", "Нет")
                : profile.clanName();
        return text(language, "characters.menu.detail.card", "⚔️ %s\nУровень: %d | Класс: %s\nPvP: %d | PK: %d\nКлан: %s\nСтатус: %s")
                .formatted(
                        character.getName(),
                        profile.level(),
                        profile.className(),
                        profile.pvp(),
                        profile.pk(),
                        clan,
                        profile.online()
                                ? text(language, "characters.profile.online", "🟢 Онлайн")
                                : text(language, "characters.profile.offline", "⚫ Оффлайн")
                );
    }

    private String[] requireParts(Message message, int count, String errorText) {
        if (!message.hasText()) {
            throw new ForcePlayException(errorText);
        }
        String[] parts = message.getText().trim().split("\\s+");
        if (parts.length < count) {
            throw new ForcePlayException(errorText);
        }
        return parts;
    }

    private void validateTradeKeyPassword(String language, String value) {
        if (!value.matches("[A-Za-z0-9]{4,16}")) {
            throw new ForcePlayException(text(language, "tradekey.input.password_format", "Пароль должен содержать 4-16 латинских букв или цифр."));
        }
    }

    private void clearPendingInputIfExists(Long telegramId) {
        if (pendingInputStateService.exists(telegramId)) {
            pendingInputStateService.clear(telegramId);
        }
    }

    private String text(String language, String key, String defaultValue) {
        return messageResolver.get(language, key, defaultValue);
    }

    private String notificationState(String language, boolean enabled) {
        return enabled
                ? text(language, "notifications.state.enabled", "включен")
                : text(language, "notifications.state.disabled", "выключен");
    }

    private boolean maskEnabled(int mask, int bit) {
        return (mask & bit) == bit;
    }
}
