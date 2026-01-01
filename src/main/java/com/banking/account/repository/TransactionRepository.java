// ==================== 2. TransactionRepository (Méthode manquante) ====================
package com.banking.account.repository;

import com.banking.account.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Query("""
SELECT t FROM Transaction t
WHERE t.accountId = :accountId
AND t.createdAt BETWEEN :startDate AND :endDate
ORDER BY t.createdAt ASC
""")
    List<Transaction> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );


    List<Transaction> findByReference(String reference);

    // ⭐ MÉTHODE MANQUANTE AJOUTÉE
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId " +
            "AND t.createdAt < :beforeDate " +
            "ORDER BY t.createdAt ASC")
    List<Transaction> findByAccountIdAndCreatedAtBefore(Long accountId, LocalDateTime beforeDate);
}