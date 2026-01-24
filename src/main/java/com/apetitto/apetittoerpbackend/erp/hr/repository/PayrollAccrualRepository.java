package com.apetitto.apetittoerpbackend.erp.hr.repository;

import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface PayrollAccrualRepository extends JpaRepository<PayrollAccrual, Long>,
        JpaSpecificationExecutor<PayrollAccrual> {

    boolean existsByEmployeeIdAndPeriodStartAndPeriodEndAndStatusNot(Long id, LocalDate periodStart, LocalDate periodEnd, PayrollStatus status);

}
