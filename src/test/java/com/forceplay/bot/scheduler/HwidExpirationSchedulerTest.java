package com.forceplay.bot.scheduler;

import com.forceplay.bot.service.HwidService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HwidExpirationSchedulerTest {

    @Mock
    private HwidService hwidService;

    @InjectMocks
    private HwidExpirationScheduler scheduler;

    @Test
    void shouldDelegateExpirationRunToService() {
        when(hwidService.expirePendingRequests()).thenReturn(2);

        scheduler.expireRequests();

        verify(hwidService).expirePendingRequests();
    }
}
