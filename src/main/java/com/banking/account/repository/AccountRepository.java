package com.banking.account.repository;

import com.banking.account.model.Account;
import com.banking.account.model.Account.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserId(Long userId);

    List<Account> findByStatus(AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = :status")
    List<Account> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AccountStatus status);

    boolean existsByAccountNumber(String accountNumber);

    // =========================================================================
    // Ajouts utiles pour le futur (recherches admin, statistiques, etc.)
    // =========================================================================

    List<Account> findByStatusAndBalanceGreaterThan(AccountStatus status, java.math.BigDecimal balance);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'ACTIVE'")
    long countActiveAccounts();
}