package com.forceplay.bot.repository;

import com.forceplay.bot.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUserTelegramId(Long telegramId);
    Optional<Account> findByExternalAccountIdAndServerName(String externalAccountId, String serverName);
}
