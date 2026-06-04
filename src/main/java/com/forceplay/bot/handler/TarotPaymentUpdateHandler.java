package com.forceplay.bot.handler;

import com.forceplay.bot.service.TarotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TarotPaymentUpdateHandler implements UpdateHandler {

    private final TarotService tarotService;
    private final TelegramClient telegramClient;

    @Override
    public boolean supports(Update update) {
        return update.hasPreCheckoutQuery()
                || (update.hasMessage() && update.getMessage().hasSuccessfulPayment());
    }

    @Override
    public void handle(Update update) {
        if (update.hasPreCheckoutQuery()) {
            String payload = update.getPreCheckoutQuery().getInvoicePayload();
            boolean accepted = tarotService.canAcceptPurchase(payload);
            answerPreCheckout(update.getPreCheckoutQuery().getId(), accepted);
            return;
        }
        SuccessfulPayment payment = update.getMessage().getSuccessfulPayment();
        tarotService.completePurchase(payment.getInvoicePayload(), payment.getTelegramPaymentChargeId(), payment.getTotalAmount());
    }

    private void answerPreCheckout(String queryId, boolean accepted) {
        try {
            telegramClient.execute(AnswerPreCheckoutQuery.builder()
                    .preCheckoutQueryId(queryId)
                    .ok(accepted)
                    .errorMessage(accepted ? null : "Покупка раскладов не найдена или уже оплачена.")
                    .build());
        } catch (TelegramApiException exception) {
            log.warn("Failed to answer tarot pre-checkout query {}", queryId, exception);
        }
    }
}
