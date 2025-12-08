package com.apetitto.apetittoerpbackend.erp.finance.repository;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceSubCategoryRepository extends JpaRepository<FinanceSubCategory, Long> {
}

