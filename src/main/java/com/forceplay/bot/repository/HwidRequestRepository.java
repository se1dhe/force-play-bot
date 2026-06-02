package com.forceplay.bot.repository;

import com.forceplay.bot.model.HwidRequest;
import com.forceplay.bot.model.HwidRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface HwidRequestRepository extends JpaRepository<HwidRequest, Long> {
    Optional<HwidRequest> findFirstByCharacterIdAndStatusOrderByCreatedAtDesc(Long characterId, HwidRequestStatus status);
    List<HwidRequest> findAllByStatusAndExpiresAtBefore(HwidRequestStatus status, OffsetDateTime timestamp);
}
