package com.apetitto.apetittoerpbackend.erp.finance.repository;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long>,
        JpaSpecificationExecutor<FinanceTransaction> {
}
