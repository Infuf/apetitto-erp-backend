package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.hr.controller.api.PayrollApi;
import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollAccrualDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollRequestDto;
import com.apetitto.apetittoerpbackend.erp.hr.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class PayrollController implements PayrollApi {

    private final PayrollService payrollService;

    @Override
    public ResponseEntity<Void> calculatePayroll(PayrollRequestDto request) {
        payrollService.calculateAndAccruePayroll(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Page<PayrollAccrualDto>> getPayrolls(LocalDate dateFrom, LocalDate dateTo, Long departmentId, Long employeeId, Pageable pageable) {
        Page<PayrollAccrualDto> payrolls = payrollService.getPayrolls(dateFrom, dateTo, departmentId, employeeId, pageable);
        return ResponseEntity.ok(payrolls);
    }

    @Override
    public ResponseEntity<PayrollAccrualDto> getPayrollById(Long id) {
        return ResponseEntity.ok(payrollService.getPayrollById(id));
    }

    @Override
    public ResponseEntity<Void> cancelPayroll(Long id) {
        payrollService.cancelPayroll(id);
        return ResponseEntity.ok().build();
    }
}