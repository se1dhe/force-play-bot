package com.forceplay.bot.repository;

import com.forceplay.bot.model.TarotFeedEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarotFeedEntryRepository extends JpaRepository<TarotFeedEntry, Long> {
    List<TarotFeedEntry> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
