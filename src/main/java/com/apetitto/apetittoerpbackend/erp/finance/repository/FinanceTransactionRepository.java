package com.apetitto.apetittoerpbackend.erp.finance.repository;

import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.FinancialFlatStats;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long>,
        JpaSpecificationExecutor<FinanceTransaction> {

    @Query("SELECT t FROM FinanceTransaction t " +
            "LEFT JOIN FETCH t.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE t.id = :id")
    Optional<FinanceTransaction> findByIdWithItems(@Param("id") Long id);

    @Query("""
                SELECT new com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.FinancialFlatStats(
                    t.operationType,
                    c.name,
                    sc.name,
                    SUM(t.amount)
                )
                FROM FinanceTransaction t
                LEFT JOIN t.category c
                LEFT JOIN t.subCategory sc
                WHERE t.transactionDate BETWEEN :dateFrom AND :dateTo
                AND t.status = 'COMPLETED'
                AND t.operationType IN :types
                GROUP BY t.operationType, c.name, sc.name
            """)
    List<FinancialFlatStats> getFinancialStats(
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("types") List<FinanceOperationType> types
    );

    @Query("SELECT t FROM FinanceTransaction t " +
            "WHERE (t.toAccount.id = :accountId OR t.fromAccount.id = :accountId) " +
            "AND t.transactionDate BETWEEN :from AND :to " +
            "AND t.status = 'COMPLETED' " +
            "ORDER BY t.transactionDate DESC")
    List<FinanceTransaction> findEmployeeHistory(
            @Param("accountId") Long accountId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
