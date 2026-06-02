package com.forceplay.bot.integration;

import com.forceplay.bot.dto.LinkConfirmResult;
import com.forceplay.bot.dto.HwidUnlinkResult;
import com.forceplay.bot.dto.LinkRequestResult;
import com.forceplay.bot.dto.BonusClaimResult;
import com.forceplay.bot.dto.TradeKeyResult;
import com.forceplay.bot.dto.CharacterProfileResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FakeLineageApiServiceTest {

    private final FakeLineageApiService service = new FakeLineageApiService();

    @Test
    void shouldReturnDeterministicMockLinkConfirmation() {
        LinkConfirmResult result = service.confirmAccountLink("x25_old", "rq-1", 77L);

        assertThat(result.externalAccountId()).isEqualTo("acc-77");
        assertThat(result.serverName()).isEqualTo("x25_old");
        assertThat(result.hwid()).isEqualTo("mock-hwid");
        assertThat(result.linkedCharacters())
                .extracting(LinkConfirmResult.LinkedCharacter::externalCharacterId, LinkConfirmResult.LinkedCharacter::name)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(7701L, "MainChar"),
                        org.assertj.core.groups.Tuple.tuple(7702L, "Buffer"),
                        org.assertj.core.groups.Tuple.tuple(7703L, "Spoiler")
                );
    }

    @Test
    void shouldReturnLinkRequestForRequestedCharacter() {
        LinkRequestResult result = service.requestAccountLink("x25_old", "Hero", 77L);

        assertThat(result.requestId()).isNotBlank();
        assertThat(result.serverName()).isEqualTo("x25_old");
        assertThat(result.characterName()).isEqualTo("Hero");
    }

    @Test
    void shouldReturnSuccessfulMockPlayerActions() {
        HwidUnlinkResult hwid = service.unlinkHwid("x25_old", "acc-1", 1001L);
        TradeKeyResult tradeKey = service.changeTradeKey("x25_old", "acc-1", 1001L, "LOCK1001");
        BonusClaimResult bonus = service.claimBonus("x25_old", "acc-1", 1001L, 77L, 57L, 10);
        CharacterProfileResult profile = service.getCharacterProfile("x25_old", "acc-1", 1001L);
        com.forceplay.bot.dto.AutofarmStatusResult autofarm = service.getAutofarmStatus("x25_old", "acc-1", 1004L);
        com.forceplay.bot.dto.CharacterActionResult revive = service.reviveAutofarm("x25_old", "acc-1", 1004L);
        com.forceplay.bot.dto.CharacterActionResult town = service.teleportToTown("x25_old", "acc-1", 1004L);

        assertThat(hwid.unlinked()).isTrue();
        assertThat(hwid.message()).isEqualTo("HWID отвязан");
        assertThat(tradeKey.externalCharacterId()).isEqualTo(1001L);
        assertThat(tradeKey.success()).isTrue();
        assertThat(tradeKey.message()).isEqualTo("Trade key обновлен");
        assertThat(bonus.success()).isTrue();
        assertThat(bonus.message()).isEqualTo("Bonus claimed for character 1001: 57 x10");
        assertThat(profile.level()).isEqualTo(76);
        assertThat(profile.className()).isEqualTo("Spoiler");
        assertThat(profile.online()).isTrue();
        assertThat(autofarm.activeAutofarm()).isTrue();
        assertThat(autofarm.autofarming()).isFalse();
        assertThat(autofarm.alive()).isTrue();
        assertThat(revive.success()).isTrue();
        assertThat(town.success()).isTrue();
    }
}
