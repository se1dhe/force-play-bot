package com.forceplay.bot.service;

import com.forceplay.bot.dto.HwidUnlinkResult;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.GameCharacter;
import com.forceplay.bot.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerActionServiceTest {

    @Mock
    private LineageApiService lineageApiService;
    @Mock
    private BonusService bonusService;
    @Mock
    private TelegramBotPropertiesAccessor telegramBotPropertiesAccessor;

    @InjectMocks
    private PlayerActionService playerActionService;

    @Test
    void shouldForwardHwidUnlinkToIntegration() {
        GameCharacter character = GameCharacter.builder()
                .id(15L)
                .externalCharacterId(1001L)
                .name("Hero")
                .account(Account.builder()
                        .externalAccountId("acc-1")
                        .serverName("x25_old")
                        .user(User.builder().telegramId(77L).build())
                        .build())
                .build();
        when(lineageApiService.unlinkHwid("x25_old", "acc-1", 1001L))
                .thenReturn(new HwidUnlinkResult(1001L, true, "HWID отвязан"));

        HwidUnlinkResult result = playerActionService.unlinkHwid(character);

        assertThat(result.unlinked()).isTrue();
        assertThat(result.message()).isEqualTo("HWID отвязан");
    }

    @Test
    void shouldChangeTradeKeyForCharacter() {
        GameCharacter character = GameCharacter.builder()
                .id(15L)
                .externalCharacterId(1001L)
                .name("Hero")
                .account(Account.builder()
                        .externalAccountId("acc-1")
                        .serverName("x25_old")
                        .user(User.builder().telegramId(77L).build())
                        .build())
                .build();
        when(lineageApiService.changeTradeKey("x25_old", "acc-1", 1001L, "TEST1234"))
                .thenReturn(new com.forceplay.bot.dto.TradeKeyResult(1001L, true, "Trade key обновлен"));

        com.forceplay.bot.dto.TradeKeyResult result = playerActionService.changeTradeKey(character, "TEST1234");

        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("Trade key обновлен");
    }
}
