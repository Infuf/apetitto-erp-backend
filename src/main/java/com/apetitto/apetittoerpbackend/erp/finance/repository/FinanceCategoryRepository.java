package com.apetitto.apetittoerpbackend.erp.finance.repository;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceCategoryRepository extends JpaRepository<FinanceCategory, Long> {
    List<FinanceCategory> findByType(String type);
}
