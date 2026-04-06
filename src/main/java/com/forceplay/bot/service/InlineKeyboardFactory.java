package com.forceplay.bot.service;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.model.Account;
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

    public InlineKeyboardMarkup mainMenu(boolean admin) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(button("Привязать аккаунт", "menu|link", STYLE_PRIMARY)));
        rows.add(new InlineKeyboardRow(
                button("Trade Key", "menu|tradekey", STYLE_PRIMARY),
                button("Бонус", "menu|bonus", STYLE_SUCCESS)
        ));
        rows.add(new InlineKeyboardRow(
                button("Боссы", "menu|bosses", STYLE_PRIMARY),
                button("Ивенты", "menu|events", STYLE_PRIMARY)
        ));
        rows.add(new InlineKeyboardRow(button("Промокод", "menu|promo", STYLE_PRIMARY)));
        if (admin) {
            rows.add(new InlineKeyboardRow(button("Админ-рассылка", "menu|admin|broadcast", STYLE_DANGER)));
        }
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup serverSelection(String actionPrefix) {
        List<InlineKeyboardRow> rows = serversProperties.getServers().stream()
                .map(server -> new InlineKeyboardRow(button(server.getName(), actionPrefix + "|" + server.getName(), STYLE_PRIMARY)))
                .toList();
        return withBack(rows);
    }

    public InlineKeyboardMarkup accountSelection(List<Account> accounts, String actionPrefix) {
        List<InlineKeyboardRow> rows = accounts.stream()
                .map(account -> new InlineKeyboardRow(button(
                        account.getServerName() + " / " + account.getExternalAccountId(),
                        actionPrefix + "|" + account.getId(),
                        STYLE_PRIMARY
                )))
                .toList();
        return withBack(rows);
    }

    public InlineKeyboardMarkup confirmLink(String serverName, String requestId) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button("Подтвердить в игре", "menu|link|confirm|" + serverName + "|" + requestId, STYLE_SUCCESS)),
                new InlineKeyboardRow(button("Назад", "menu|main", STYLE_DANGER))
        ));
    }

    public InlineKeyboardMarkup promptBack(String backTarget) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button("Назад", backTarget, STYLE_DANGER))
        ));
    }

    public InlineKeyboardMarkup resultBackToMain() {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button("В меню", "menu|main", STYLE_PRIMARY))
        ));
    }

    public InlineKeyboardMarkup hwidDecision(Long requestId) {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(
                        button("Подтвердить", "hwid:approve:" + requestId, STYLE_SUCCESS),
                        button("Отклонить", "hwid:deny:" + requestId, STYLE_DANGER)
                )
        ));
    }

    private InlineKeyboardMarkup withBack(List<InlineKeyboardRow> rows) {
        List<InlineKeyboardRow> mutableRows = new ArrayList<>(rows);
        mutableRows.add(new InlineKeyboardRow(button("Назад", "menu|main", STYLE_DANGER)));
        return new InlineKeyboardMarkup(mutableRows);
    }

    private InlineKeyboardButton button(String text, String callbackData, String style) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        button.setStyle(style);
        return button;
    }
}
