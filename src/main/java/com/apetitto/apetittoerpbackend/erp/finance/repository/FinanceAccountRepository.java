package com.apetitto.apetittoerpbackend.erp.finance.repository;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceAccountRepository extends JpaRepository<FinanceAccount, Long>, JpaSpecificationExecutor<FinanceAccount> {

    List<FinanceAccount> findByTypeAndIsActiveTrue(FinanceAccountType type);
}
