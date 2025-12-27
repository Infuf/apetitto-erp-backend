package com.apetitto.apetittoerpbackend.erp.finance.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.CompanyFinancialStateDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.ExpenseReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.FinancialFlatStats;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.IncomeReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class FinanceDashboardServiceImpl implements FinanceDashboardService {

    private final FinanceAccountRepository accountRepository;
    private final FinanceTransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public CompanyFinancialStateDto getCompanyState() {
        List<FinanceAccount> allAccounts = accountRepository.findAll().stream()
                .filter(FinanceAccount::getIsActive)
                .toList();

        List<FinanceAccount> moneyAccounts = allAccounts.stream()
                .filter(a -> a.getType() == FinanceAccountType.CASHBOX || a.getType() == FinanceAccountType.BANK)
                .toList();

        BigDecimal totalMoney = moneyAccounts.stream()
                .map(FinanceAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FinanceAccount> receivableAccounts = allAccounts.stream()
                .filter(a -> a.getType() == FinanceAccountType.DEALER && a.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .sorted(comparing(FinanceAccount::getBalance).reversed())
                .toList();

        BigDecimal totalReceivables = receivableAccounts.stream()
                .map(FinanceAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FinanceAccount> payableAccounts = allAccounts.stream()
                .filter(a -> (a.getType() == FinanceAccountType.SUPPLIER || a.getType() == FinanceAccountType.EMPLOYEE)
                        && a.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .sorted(comparing(FinanceAccount::getBalance))
                .toList();

        BigDecimal totalPayables = payableAccounts.stream()
                .map(FinanceAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();

        return CompanyFinancialStateDto.builder()
                .netBalance(totalMoney.add(totalReceivables).subtract(totalPayables))
                .money(CompanyFinancialStateDto.MoneyState.builder()
                        .totalAmount(totalMoney)
                        .details(mapToSummary(moneyAccounts, false))
                        .build())
                .receivables(CompanyFinancialStateDto.DebtState.builder()
                        .totalAmount(totalReceivables)
                        .topDebtors(mapToSummary(receivableAccounts, false))
                        .build())
                .payables(CompanyFinancialStateDto.DebtState.builder()
                        .totalAmount(totalPayables)
                        .topDebtors(mapToSummary(payableAccounts, true))
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseReportDto getExpenseReport(Instant dateFrom, Instant dateTo) {

        if (dateFrom == null || dateTo == null) {
            throw new InvalidRequestException("Data from or Data to is NULL");
        }

        var expenseTypes = List.of(
                EXPENSE,
                PAYMENT_TO_SUPP,
                SALARY_PAYOUT,
                OWNER_WITHDRAW
        );

        var stats = transactionRepository.getFinancialStats(dateFrom, dateTo, expenseTypes);

        var totalExpense = stats.stream()
                .map(FinancialFlatStats::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalExpense.compareTo(BigDecimal.ZERO) == 0) {
            return ExpenseReportDto.builder()
                    .totalExpense(BigDecimal.ZERO)
                    .categories(List.of())
                    .build();
        }

        Map<String, List<FinancialFlatStats>> groupedByCat = stats.stream()
                .collect(groupingBy(stat -> {
                    if (stat.getCategoryName() != null) {
                        return stat.getCategoryName();
                    }
                    return mapOperationTypeToLabel(stat.getOperationType());
                }));

        List<ExpenseReportDto.CategoryExpenseDto> categoryDtos = new ArrayList<>();

        for (var entry : groupedByCat.entrySet()) {
            String catName = entry.getKey();
            List<FinancialFlatStats> subItems = entry.getValue();

            BigDecimal catTotal = subItems.stream()
                    .map(FinancialFlatStats::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal percent = catTotal.divide(totalExpense, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            List<ExpenseReportDto.SubCategoryExpenseDto> subDtos = subItems.stream()
                    .filter(i -> i.getSubCategoryName() != null)
                    .map(i -> new ExpenseReportDto.SubCategoryExpenseDto(i.getSubCategoryName(), i.getAmount()))
                    .toList();

            categoryDtos.add(new ExpenseReportDto.CategoryExpenseDto(catName, catTotal, percent, subDtos));
        }

        categoryDtos.sort(comparing(ExpenseReportDto.CategoryExpenseDto::getAmount).reversed());

        return ExpenseReportDto.builder()
                .totalExpense(totalExpense)
                .categories(categoryDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeReportDto getIncomeReport(Instant dateFrom, Instant dateTo) {

        if (dateFrom == null || dateTo == null) {
            throw new InvalidRequestException("Data from or Data to is NULL");
        }

        List<FinanceOperationType> incomeTypes = List.of(
                FinanceOperationType.INCOME,
                FinanceOperationType.PAYMENT_FROM_DLR
        );

        List<FinancialFlatStats> stats = transactionRepository.getFinancialStats(dateFrom, dateTo, incomeTypes);

        BigDecimal totalIncome = stats.stream()
                .map(FinancialFlatStats::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return IncomeReportDto.builder()
                    .totalIncome(BigDecimal.ZERO)
                    .categories(List.of())
                    .build();
        }

        Map<String, List<FinancialFlatStats>> groupedByCat = stats.stream()
                .collect(Collectors.groupingBy(stat -> {
                    if (stat.getCategoryName() != null) {
                        return stat.getCategoryName();
                    }
                    return mapIncomeTypeToLabel(stat.getOperationType());
                }));

        List<IncomeReportDto.CategoryIncomeDto> categoryDtos = new ArrayList<>();

        for (var entry : groupedByCat.entrySet()) {
            String catName = entry.getKey();
            List<FinancialFlatStats> subItems = entry.getValue();

            BigDecimal catTotal = subItems.stream()
                    .map(FinancialFlatStats::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal percent = catTotal.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            List<IncomeReportDto.SubCategoryIncomeDto> subDtos = subItems.stream()
                    .filter(i -> i.getSubCategoryName() != null)
                    .map(i -> new IncomeReportDto.SubCategoryIncomeDto(i.getSubCategoryName(), i.getAmount()))
                    .toList();

            categoryDtos.add(new IncomeReportDto.CategoryIncomeDto(catName, catTotal, percent, subDtos));
        }

        categoryDtos.sort(comparing(IncomeReportDto.CategoryIncomeDto::getAmount).reversed());

        return IncomeReportDto.builder()
                .totalIncome(totalIncome)
                .categories(categoryDtos)
                .build();
    }

    private List<CompanyFinancialStateDto.AccountSummary> mapToSummary(List<FinanceAccount> accounts, boolean invertSign) {
        return accounts.stream()
                .map(a -> new CompanyFinancialStateDto.AccountSummary(
                        a.getId(),
                        a.getName(),
                        invertSign ? a.getBalance().abs() : a.getBalance()
                ))
                .collect(Collectors.toList());
    }

    private String mapOperationTypeToLabel(FinanceOperationType type) {
        return switch (type) {
            case PAYMENT_TO_SUPP -> "Оплата Поставщикам";
            case SALARY_PAYOUT -> "Выплата Зарплаты";
            case OWNER_WITHDRAW -> "Вывод средств (Владелец)";
            default -> "Прочие расходы";
        };
    }

    private String mapIncomeTypeToLabel(FinanceOperationType type) {
        return switch (type) {
            case INCOME -> "Прочие доходы";
            case PAYMENT_FROM_DLR -> "Оплата от Дилеров";
            default -> "Прочие поступления";
        };
    }
}
