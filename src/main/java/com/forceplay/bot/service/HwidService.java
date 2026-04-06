package com.forceplay.bot.service;

import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.dto.HwidConfirmCommand;
import com.forceplay.bot.integration.LineageApiService;
import com.forceplay.bot.model.Account;
import com.forceplay.bot.model.HwidRequest;
import com.forceplay.bot.model.HwidRequestStatus;
import com.forceplay.bot.repository.AccountRepository;
import com.forceplay.bot.repository.HwidRequestRepository;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HwidService {

    private final AccountRepository accountRepository;
    private final HwidRequestRepository hwidRequestRepository;
    private final LineageApiService lineageApiService;
    private final TelegramBotProperties botProperties;
    private final RedisStateService redisStateService;

    @Transactional
    public HwidRequest createRequest(String serverName, String externalAccountId, String newHwid) {
        Account account = accountRepository.findByExternalAccountIdAndServerName(externalAccountId, serverName)
                .orElseThrow(() -> new ForcePlayException("Account not found"));

        HwidRequest request = hwidRequestRepository.save(HwidRequest.builder()
                .account(account)
                .newHwid(newHwid)
                .status(HwidRequestStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusSeconds(botProperties.getHwidTtlSeconds()))
                .build());

        redisStateService.put(redisKey(request.getId()), newHwid, Duration.ofSeconds(botProperties.getHwidTtlSeconds()));
        redisStateService.enqueueNotification("HWID:" + request.getId());
        return request;
    }

    @Transactional
    public void resolve(Long requestId, boolean approved) {
        HwidRequest request = hwidRequestRepository.findById(requestId)
                .orElseThrow(() -> new ForcePlayException("HWID request not found"));
        if (request.getStatus() != HwidRequestStatus.PENDING) {
            return;
        }

        request.setStatus(approved ? HwidRequestStatus.APPROVED : HwidRequestStatus.DENIED);
        if (approved) {
            request.getAccount().setHwid(request.getNewHwid());
            accountRepository.save(request.getAccount());
        }
        hwidRequestRepository.save(request);
        lineageApiService.confirmHwid(new HwidConfirmCommand(
                request.getAccount().getServerName(),
                request.getAccount().getExternalAccountId(),
                request.getNewHwid(),
                approved
        ));
        redisStateService.delete(redisKey(requestId));
    }

    @Transactional
    public int expirePendingRequests() {
        List<HwidRequest> expired = hwidRequestRepository.findAllByStatusAndExpiresAtBefore(HwidRequestStatus.PENDING, OffsetDateTime.now());
        expired.forEach(request -> {
            request.setStatus(HwidRequestStatus.DENIED);
            lineageApiService.confirmHwid(new HwidConfirmCommand(
                    request.getAccount().getServerName(),
                    request.getAccount().getExternalAccountId(),
                    request.getNewHwid(),
                    false
            ));
            redisStateService.delete(redisKey(request.getId()));
        });
        hwidRequestRepository.saveAll(expired);
        return expired.size();
    }

    private String redisKey(Long requestId) {
        return "forceplay:hwid:" + requestId;
    }
}
