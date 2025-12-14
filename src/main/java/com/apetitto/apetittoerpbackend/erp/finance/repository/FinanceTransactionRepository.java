package com.apetitto.apetittoerpbackend.erp.finance.repository;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long>,
        JpaSpecificationExecutor<FinanceTransaction> {

    @Query("SELECT t FROM FinanceTransaction t " +
            "LEFT JOIN FETCH t.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE t.id = :id")
    Optional<FinanceTransaction> findByIdWithItems(@Param("id") Long id);
}
