package com.apetitto.apetittoerpbackend.erp.finance.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.*;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.ExpenseReportDto.SubCategoryExpenseDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.IncomeReportDto.SubCategoryIncomeDto;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
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
                    } else if (stat.getOperationType() == PAYMENT_TO_SUPP) {
                        return "Оплата поставщикам";
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

            Map<String, BigDecimal> mergedSubItems = subItems.stream()
                    .map(i -> {
                        String name = null;
                        if (i.getSubCategoryName() != null) {
                            name = i.getSubCategoryName();
                        } else if (i.getToAccountName() != null)
                            name = i.getToAccountName();

                        if (name != null) {
                            return Map.entry(name, i.getAmount());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            BigDecimal::add
                    ));

            List<SubCategoryExpenseDto> subDtos = mergedSubItems.entrySet().stream()
                    .map(e -> new SubCategoryExpenseDto(e.getKey(), e.getValue()))
                    .sorted(comparing(SubCategoryExpenseDto::getAmount).reversed())
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
                .collect(groupingBy(stat -> {
                    if (stat.getCategoryName() != null) {
                        return stat.getCategoryName();
                    } else if (stat.getOperationType() == PAYMENT_FROM_DLR) {
                        return "Оплата от дилеров";
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

            Map<String, BigDecimal> mergedSubItems = subItems.stream()
                    .map(i -> {
                        String name = null;
                        if (i.getFromAccountName() != null) {
                            name = i.getFromAccountName();
                        } else if (i.getSubCategoryName() != null) {
                            name = i.getSubCategoryName();
                        }

                        if (name != null) {
                            return Map.entry(name, i.getAmount());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            BigDecimal::add
                    ));

            List<SubCategoryIncomeDto> subDtos = mergedSubItems.entrySet().stream()
                    .map(e -> new SubCategoryIncomeDto(e.getKey(), e.getValue()))
                    .sorted(comparing(SubCategoryIncomeDto::getAmount).reversed())
                    .toList();

            categoryDtos.add(new IncomeReportDto.CategoryIncomeDto(catName, catTotal, percent, subDtos));
        }

        categoryDtos.sort(comparing(IncomeReportDto.CategoryIncomeDto::getAmount).reversed());

        return IncomeReportDto.builder()
                .totalIncome(totalIncome)
                .categories(categoryDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PartnersAnalysisReportDto getPartnerAnalysis(Instant from, Instant to, boolean isSupplier) {

        FinanceOperationType type = isSupplier
                ? FinanceOperationType.SUPPLIER_INVOICE
                : FinanceOperationType.DEALER_INVOICE;

        List<PartnerProductFlatDto> flatStats = transactionRepository.getPartnerStats(from, to, type);

        if (flatStats.isEmpty()) {
            return PartnersAnalysisReportDto.builder()
                    .grandTotalAmount(BigDecimal.ZERO)
                    .grandTotalQuantity(BigDecimal.ZERO)
                    .partners(List.of())
                    .build();
        }

        BigDecimal grandTotalAmount = flatStats.stream()
                .map(PartnerProductFlatDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotalQuantity = flatStats.stream()
                .map(PartnerProductFlatDto::getTotalQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, List<PartnerProductFlatDto>> grouped = flatStats.stream()
                .collect(groupingBy(
                        PartnerProductFlatDto::getPartnerId,
                        LinkedHashMap::new,
                        toList()
                ));

        List<PartnersAnalysisReportDto.PartnerDto> partnerDtos = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            List<PartnerProductFlatDto> items = entry.getValue();
            String partnerName = items.get(0).getPartnerName();

            BigDecimal partnerTotalAmount = BigDecimal.ZERO;
            BigDecimal partnerTotalQuantity = BigDecimal.ZERO;
            List<PartnersAnalysisReportDto.ProductDto> productDtos = new ArrayList<>();

            for (var item : items) {
                partnerTotalAmount = partnerTotalAmount.add(item.getTotalAmount());
                partnerTotalQuantity = partnerTotalQuantity.add(item.getTotalQuantity());

                BigDecimal avgPrice = BigDecimal.ZERO;
                if (item.getTotalQuantity().compareTo(BigDecimal.ZERO) != 0) {
                    avgPrice = item.getTotalAmount().divide(item.getTotalQuantity(), 2, RoundingMode.HALF_UP);
                }

                productDtos.add(PartnersAnalysisReportDto.ProductDto.builder()
                        .productName(item.getProductName())
                        .unit(item.getProductUnit())
                        .quantity(item.getTotalQuantity())
                        .amount(item.getTotalAmount())
                        .averagePrice(avgPrice)
                        .build());
            }

            BigDecimal share = BigDecimal.ZERO;
            if (grandTotalAmount.compareTo(BigDecimal.ZERO) != 0) {
                share = partnerTotalAmount.divide(grandTotalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            partnerDtos.add(PartnersAnalysisReportDto.PartnerDto.builder()
                    .partnerId(entry.getKey())
                    .partnerName(partnerName)
                    .totalAmount(partnerTotalAmount)
                    .totalQuantity(partnerTotalQuantity)
                    .shareInGrandTotal(share)
                    .products(productDtos)
                    .build());
        }

        partnerDtos.sort(comparing(PartnersAnalysisReportDto.PartnerDto::getTotalAmount)
                .reversed());

        return PartnersAnalysisReportDto.builder()
                .grandTotalAmount(grandTotalAmount)
                .grandTotalQuantity(grandTotalQuantity)
                .partners(partnerDtos)
                .build();
    }


    private List<CompanyFinancialStateDto.AccountSummary> mapToSummary(List<FinanceAccount> accounts, boolean invertSign) {
        return accounts.stream()
                .map(a -> new CompanyFinancialStateDto.AccountSummary(
                        a.getId(),
                        a.getName(),
                        invertSign ? a.getBalance().abs() : a.getBalance()
                ))
                .collect(toList());
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
