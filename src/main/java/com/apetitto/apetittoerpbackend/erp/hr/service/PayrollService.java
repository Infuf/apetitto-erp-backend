package com.apetitto.apetittoerpbackend.erp.hr.service;

import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollAccrualDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PayrollService {

    void calculateAndAccruePayroll(PayrollRequestDto request);

    void cancelPayroll(Long id);

    PayrollAccrualDto getPayrollById(Long id);

    Page<PayrollAccrualDto> getPayrolls(LocalDate dateFrom, LocalDate dateTo, Long departmentId, Long employeeId, Pageable pageable);
}
