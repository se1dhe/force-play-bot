package com.forceplay.bot.service;

import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.HwidRequest;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.util.ForcePlayException;
import com.forceplay.bot.util.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InlineMenuService {

    private final AccountLinkService accountLinkService;
    private final PlayerActionService playerActionService;
    private final InfoService infoService;
    private final PromoService promoService;
    private final AdminService adminService;
    private final HwidService hwidService;
    private final TelegramGateway telegramGateway;
    private final PendingInputStateService pendingInputStateService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final AccountRepository accountRepository;
    private final MessageResolver messageResolver;

    public void showMainMenu(Long chatId, Long telegramId) {
        telegramGateway.sendText(chatId, "Главное меню ForcePlay", inlineKeyboardFactory.mainMenu(adminService.isAdmin(telegramId)));
    }

    public void showMainMenu(Long chatId, Integer messageId, Long telegramId) {
        telegramGateway.editText(chatId, messageId, "Главное меню ForcePlay", inlineKeyboardFactory.mainMenu(adminService.isAdmin(telegramId)));
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long telegramId = callbackQuery.getFrom().getId();
        String language = callbackQuery.getFrom().getLanguageCode();

        if (data.startsWith("hwid:")) {
            handleHwidCallback(callbackQuery, chatId);
            return;
        }

        String[] parts = data.split("\\|");
        if (!"menu".equals(parts[0])) {
            telegramGateway.answerCallback(callbackQuery.getId(), "Неизвестное действие");
            return;
        }

        switch (parts[1]) {
            case "main" -> showMainMenu(chatId, messageId, telegramId);
            case "link" -> handleLinkMenu(parts, chatId, messageId, telegramId, language, callbackQuery.getId());
            case "tradekey" -> handleTradeKeyMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "bonus" -> handleBonusMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "bosses" -> handleWorldInfoMenu(parts, chatId, messageId, "bosses", callbackQuery.getId());
            case "events" -> handleWorldInfoMenu(parts, chatId, messageId, "events", callbackQuery.getId());
            case "promo" -> handlePromoMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            case "admin" -> handleAdminMenu(parts, chatId, messageId, telegramId, callbackQuery.getId());
            default -> telegramGateway.answerCallback(callbackQuery.getId(), "Неизвестное действие");
        }
    }

    public boolean hasPendingInput(Long telegramId) {
        return pendingInputStateService.exists(telegramId);
    }

    public void handlePendingInput(Message message) {
        Long chatId = message.getChatId();
        Long telegramId = message.getFrom().getId();
        String language = message.getFrom().getLanguageCode();
        PendingInputStateService.PendingInputState state = pendingInputStateService.get(telegramId)
                .orElseThrow(() -> new ForcePlayException("Нет активного шага ввода"));

        switch (state.action()) {
            case LINK_CHARACTER -> {
                if (!message.hasText()) {
                    throw new ForcePlayException("Ожидаю текст с ником персонажа.");
                }
                pendingInputStateService.clear(telegramId);
                LinkRequestResult result = accountLinkService.requestLink(telegramId, language, state.payload(), message.getText().trim());
                telegramGateway.sendText(
                        chatId,
                        messageResolver.get("link.requested", "Запрос на привязку аккаунта отправлен.")
                                + "\nСервер: " + result.serverName()
                                + "\nПерсонаж: " + result.characterName()
                                + "\nrequestId: " + result.requestId(),
                        inlineKeyboardFactory.confirmLink(result.serverName(), result.requestId())
                );
            }
            case PROMO_CODE -> {
                if (!message.hasText()) {
                    throw new ForcePlayException("Ожидаю текст с промокодом.");
                }
                pendingInputStateService.clear(telegramId);
                Account account = getUserAccount(telegramId, Long.parseLong(state.payload()));
                String code = promoService.redeem(telegramId, language, account.getServerName(), account.getExternalAccountId(), message.getText().trim());
                telegramGateway.sendText(chatId, "Промокод активирован: " + code, inlineKeyboardFactory.resultBackToMain());
            }
            case ADMIN_BROADCAST -> {
                if (!adminService.isAdmin(telegramId)) {
                    throw new ForcePlayException(messageResolver.get("access.denied", "Недостаточно прав."));
                }
                pendingInputStateService.clear(telegramId);
                if (message.hasText()) {
                    adminService.getBroadcastTargets().forEach(userId -> telegramGateway.sendText(userId, message.getText().trim()));
                } else {
                    adminService.getBroadcastTargets().forEach(userId -> telegramGateway.copyMessage(chatId, message.getMessageId(), userId));
                }
                telegramGateway.sendText(chatId, messageResolver.get("broadcast.done", "Рассылка завершена."), inlineKeyboardFactory.resultBackToMain());
            }
        }
    }

    public void sendHwidNotification(HwidRequest request) {
        telegramGateway.sendText(
                request.getAccount().getUser().getTelegramId(),
                "Новый HWID запрос #%d\nАккаунт: %s\nHWID: %s".formatted(
                        request.getId(),
                        request.getAccount().getExternalAccountId(),
                        request.getNewHwid()
                ),
                inlineKeyboardFactory.hwidDecision(request.getId())
        );
    }

    private void handleHwidCallback(CallbackQuery callbackQuery, Long chatId) {
        String[] parts = callbackQuery.getData().split(":");
        boolean approved = "approve".equals(parts[1]);
        Long requestId = Long.parseLong(parts[2]);
        hwidService.resolve(requestId, approved);
        telegramGateway.answerCallback(callbackQuery.getId(), approved ? "HWID подтвержден" : "HWID отклонен");
        telegramGateway.editText(
                chatId,
                callbackQuery.getMessage().getMessageId(),
                approved ? "Новый HWID подтвержден." : "Запрос на HWID отклонен.",
                inlineKeyboardFactory.resultBackToMain()
        );
    }

    private void handleLinkMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String language, String callbackId) {
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId, "Выберите сервер");
            telegramGateway.editText(chatId, messageId, "Выберите сервер для привязки аккаунта", inlineKeyboardFactory.serverSelection("menu|link|server"));
            return;
        }
        if ("server".equals(parts[2])) {
            pendingInputStateService.save(telegramId, PendingInputAction.LINK_CHARACTER, parts[3]);
            telegramGateway.answerCallback(callbackId, "Отправьте ник персонажа");
            telegramGateway.editText(chatId, messageId, "Отправьте ник персонажа для сервера " + parts[3], inlineKeyboardFactory.promptBack("menu|main"));
            return;
        }
        if ("confirm".equals(parts[2])) {
            LinkConfirmResult result = accountLinkService.confirmLink(telegramId, language, parts[3], parts[4]);
            telegramGateway.answerCallback(callbackId, "Аккаунт привязан");
            telegramGateway.editText(
                    chatId,
                    messageId,
                    messageResolver.get("link.success", "Аккаунт успешно привязан.")
                            + "\nАккаунт: " + result.externalAccountId()
                            + "\nПерсонажи: " + String.join(", ", result.characters()),
                    inlineKeyboardFactory.resultBackToMain()
            );
        }
    }

    private void handleTradeKeyMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        if (parts.length == 2) {
            List<Account> accounts = accountRepository.findAllByUserTelegramId(telegramId);
            if (accounts.isEmpty()) {
                telegramGateway.answerCallback(callbackId, "Сначала привяжите аккаунт");
                telegramGateway.editText(chatId, messageId, "У вас пока нет привязанных аккаунтов.", inlineKeyboardFactory.resultBackToMain());
                return;
            }
            telegramGateway.answerCallback(callbackId, "Выберите аккаунт");
            telegramGateway.editText(chatId, messageId, "Выберите аккаунт для смены Trade Key", inlineKeyboardFactory.accountSelection(accounts, "menu|tradekey|account"));
            return;
        }
        Account account = getUserAccount(telegramId, Long.parseLong(parts[3]));
        String message = playerActionService.changeTradeKey(account.getServerName(), account.getExternalAccountId()).message();
        telegramGateway.answerCallback(callbackId, "Trade Key обновлен");
        telegramGateway.editText(chatId, messageId, message, inlineKeyboardFactory.resultBackToMain());
    }

    private void handleBonusMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        if (parts.length == 2) {
            List<Account> accounts = accountRepository.findAllByUserTelegramId(telegramId);
            if (accounts.isEmpty()) {
                telegramGateway.answerCallback(callbackId, "Сначала привяжите аккаунт");
                telegramGateway.editText(chatId, messageId, "У вас пока нет привязанных аккаунтов.", inlineKeyboardFactory.resultBackToMain());
                return;
            }
            telegramGateway.answerCallback(callbackId, "Выберите аккаунт");
            telegramGateway.editText(chatId, messageId, "Выберите аккаунт для получения бонуса", inlineKeyboardFactory.accountSelection(accounts, "menu|bonus|account"));
            return;
        }
        Account account = getUserAccount(telegramId, Long.parseLong(parts[3]));
        String result = playerActionService.claimBonus(telegramId, account.getServerName(), account.getExternalAccountId());
        telegramGateway.answerCallback(callbackId, result);
        telegramGateway.editText(chatId, messageId, result, inlineKeyboardFactory.resultBackToMain());
    }

    private void handleWorldInfoMenu(String[] parts, Long chatId, Integer messageId, String type, String callbackId) {
        if (parts.length == 2) {
            telegramGateway.answerCallback(callbackId, "Выберите сервер");
            telegramGateway.editText(chatId, messageId, "Выберите сервер", inlineKeyboardFactory.serverSelection("menu|" + type + "|server"));
            return;
        }
        String serverName = parts[3];
        String result = "bosses".equals(type)
                ? infoService.getBosses(serverName).stream()
                .map(boss -> boss.name() + " at " + boss.spawnAt())
                .collect(Collectors.joining("\n"))
                : infoService.getEvents(serverName).stream()
                .map(event -> event.title() + " at " + event.startAt())
                .collect(Collectors.joining("\n"));
        if (result.isBlank()) {
            result = "Нет данных для сервера " + serverName;
        }
        telegramGateway.answerCallback(callbackId, "Готово");
        telegramGateway.editText(chatId, messageId, result, inlineKeyboardFactory.resultBackToMain());
    }

    private void handlePromoMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        if (parts.length == 2) {
            List<Account> accounts = accountRepository.findAllByUserTelegramId(telegramId);
            if (accounts.isEmpty()) {
                telegramGateway.answerCallback(callbackId, "Сначала привяжите аккаунт");
                telegramGateway.editText(chatId, messageId, "У вас пока нет привязанных аккаунтов.", inlineKeyboardFactory.resultBackToMain());
                return;
            }
            telegramGateway.answerCallback(callbackId, "Выберите аккаунт");
            telegramGateway.editText(chatId, messageId, "Выберите аккаунт для ввода промокода", inlineKeyboardFactory.accountSelection(accounts, "menu|promo|account"));
            return;
        }
        pendingInputStateService.save(telegramId, PendingInputAction.PROMO_CODE, parts[3]);
        telegramGateway.answerCallback(callbackId, "Отправьте промокод");
        telegramGateway.editText(chatId, messageId, "Отправьте текстом промокод", inlineKeyboardFactory.promptBack("menu|main"));
    }

    private void handleAdminMenu(String[] parts, Long chatId, Integer messageId, Long telegramId, String callbackId) {
        if (!adminService.isAdmin(telegramId)) {
            telegramGateway.answerCallback(callbackId, messageResolver.get("access.denied", "Недостаточно прав."));
            telegramGateway.editText(chatId, messageId, messageResolver.get("access.denied", "Недостаточно прав."), inlineKeyboardFactory.resultBackToMain());
            return;
        }
        pendingInputStateService.save(telegramId, PendingInputAction.ADMIN_BROADCAST, "");
        telegramGateway.answerCallback(callbackId, "Отправьте текст или медиа");
        telegramGateway.editText(chatId, messageId, "Отправьте следующим сообщением текст, фото, видео или документ для рассылки", inlineKeyboardFactory.promptBack("menu|main"));
    }

    private Account getUserAccount(Long telegramId, Long accountId) {
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().getTelegramId().equals(telegramId))
                .orElseThrow(() -> new ForcePlayException("Аккаунт не найден"));
    }
}
