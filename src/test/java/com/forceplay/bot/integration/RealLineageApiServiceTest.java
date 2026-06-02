package com.forceplay.bot.integration;

import com.forceplay.bot.config.LineageServersProperties;
import com.forceplay.bot.util.ForcePlayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RealLineageApiServiceTest {

    @Mock
    private RestClient restClient;

    @Test
    void shouldFailFastForUnknownServerBeforeAnyHttpCall() {
        LineageServersProperties properties = new LineageServersProperties();
        RealLineageApiService service = new RealLineageApiService(restClient, properties);

        assertThatThrownBy(() -> service.requestAccountLink("missing", "Hero", 77L))
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("Unknown server: missing");

        assertThatThrownBy(() -> service.getCharacterProfile("missing", "acc-1", 1001L))
                .isInstanceOf(ForcePlayException.class)
                .hasMessage("Unknown server: missing");

        verifyNoInteractions(restClient);
    }
}
