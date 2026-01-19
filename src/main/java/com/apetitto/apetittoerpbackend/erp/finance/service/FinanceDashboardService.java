package com.apetitto.apetittoerpbackend.erp.finance.service;

import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.CompanyFinancialStateDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.ExpenseReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.IncomeReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.PartnersAnalysisReportDto;

import java.time.Instant;

public interface FinanceDashboardService {
    CompanyFinancialStateDto getCompanyState();

    ExpenseReportDto getExpenseReport(Instant dateFrom, Instant dateTo);

    IncomeReportDto getIncomeReport(Instant dateFrom, Instant dateTo);

    PartnersAnalysisReportDto getPartnerAnalysis(Instant from, Instant to, boolean isSupplier);
}
