package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.finance.controller.api.FinanceDashboardApi;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.CompanyFinancialStateDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.ExpenseReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.IncomeReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.PartnersAnalysisReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class FinanceDashboardController implements FinanceDashboardApi {

    private final FinanceDashboardService dashboardService;

    @Override
    public ResponseEntity<CompanyFinancialStateDto> getCompanyState() {
        return ResponseEntity.ok(dashboardService.getCompanyState());
    }

    @Override
    public ResponseEntity<ExpenseReportDto> getExpenseReport(Instant dateFrom, Instant dateTo) {
        return ResponseEntity.ok(dashboardService.getExpenseReport(dateFrom, dateTo));
    }

    @Override
    public ResponseEntity<IncomeReportDto> getIncomeReport(Instant dateFrom, Instant dateTo) {
        return ResponseEntity.ok(dashboardService.getIncomeReport(dateFrom, dateTo));
    }

    @Override
    public ResponseEntity<PartnersAnalysisReportDto> getPartnerAnalysis(Instant dateFrom, Instant dateTo, boolean isSupplier) {
        return ResponseEntity.ok(dashboardService.getPartnerAnalysis(dateFrom, dateTo, isSupplier));
    }

}
