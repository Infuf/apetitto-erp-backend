package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.dto.CancellationRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceCategory;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.TransactionStatus;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.finance.service.implementation.FinanceTransactionServiceImpl;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType.*;
import static java.time.Instant.now;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("API Финансов: Транзакции и Балансы")
class FinanceTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FinanceTransactionRepository transactionRepository;
    @Autowired
    private FinanceAccountRepository accountRepository;
    @Autowired
    private FinanceCategoryRepository categoryRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private FinanceAccount cashbox;
    private FinanceAccount bankAccount;
    private FinanceAccount supplier;
    private FinanceCategory categorySales;
    @Autowired
    private FinanceTransactionServiceImpl financeTransactionServiceImpl;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        cashbox = createAccount("Касса Магазина", FinanceAccountType.CASHBOX, new BigDecimal("1000.00"));
        bankAccount = createAccount("Основной счет", FinanceAccountType.BANK, new BigDecimal("50000.00"));
        supplier = createAccount("Поставщик Мука", FinanceAccountType.SUPPLIER, new BigDecimal("0.00"));

        categorySales = new FinanceCategory();
        categorySales.setName("Выручка");
        categorySales.setType("INCOME");
        categoryRepository.save(categorySales);
        User user = new User();
        user.setUsername("user");
        user.setPassword("123");
        userRepository.save(user);
    }

    private FinanceAccount createAccount(String name, FinanceAccountType type, BigDecimal balance) {
        FinanceAccount account = new FinanceAccount();
        account.setName(name);
        account.setType(type);
        account.setBalance(balance);
        account.setIsActive(true);
        return accountRepository.save(account);
    }

    @Nested
    @DisplayName("Создание транзакций (POST /)")
    class CreateTransactionTests {

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("INCOME: Приход денег должен увеличивать баланс")
        void createIncome_shouldIncreaseBalance() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("5000.00"));
            request.setOperationType(INCOME);
            request.setToAccountId(cashbox.getId());
            request.setCategoryId(categorySales.getId());
            request.setDescription("Выручка за день");

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.amount", is(5000.00)))
                    .andExpect(jsonPath("$.toAccountName", is("Касса Магазина")));

            FinanceAccount updatedCashbox = accountRepository.findById(cashbox.getId()).orElseThrow();
            assertEquals(0, new BigDecimal("6000.00").compareTo(updatedCashbox.getBalance()),
                    "Баланс кассы должен увеличиться");
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("EXPENSE: Расход денег должен уменьшать баланс")
        void createExpense_shouldDecreaseBalance() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("200.00"));
            request.setOperationType(EXPENSE);
            request.setFromAccountId(cashbox.getId());
            request.setDescription("Покупка воды");

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            FinanceAccount updatedCashbox = accountRepository.findById(cashbox.getId()).orElseThrow();
            assertEquals(0, new BigDecimal("800.00").compareTo(updatedCashbox.getBalance()));
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("TRANSFER: Перевод должен менять оба баланса")
        void createTransfer_shouldMoveMoneyBetweenAccounts() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("10000.00"));
            request.setOperationType(TRANSFER);
            request.setFromAccountId(bankAccount.getId());
            request.setToAccountId(cashbox.getId());
            request.setDescription("Инкассация/Снятие");

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            FinanceAccount updatedBank = accountRepository.findById(bankAccount.getId()).orElseThrow();
            FinanceAccount updatedCashbox = accountRepository.findById(cashbox.getId()).orElseThrow();

            assertEquals(0, new BigDecimal("40000.00").compareTo(updatedBank.getBalance()));
            assertEquals(0, new BigDecimal("11000.00").compareTo(updatedCashbox.getBalance()));
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("SUPPLIER_INVOICE: Покупка в долг должна уводить поставщика в минус")
        void createSupplierInvoice_shouldCreateDebt() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("5000.00"));
            request.setOperationType(SUPPLIER_INVOICE);
            request.setFromAccountId(supplier.getId());
            request.setDescription("Поступление муки");

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            FinanceAccount updatedSupplier = accountRepository.findById(supplier.getId()).orElseThrow();
            assertEquals(0, new BigDecimal("-5000.00").compareTo(updatedSupplier.getBalance()));
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Детализация чека: Товары должны сохраняться")
        void createTransaction_withItems_shouldSaveDetails() throws Exception {
            TransactionCreateRequestDto.TransactionItemDto itemDto = new TransactionCreateRequestDto.TransactionItemDto();
            Long PRODUCT_ID_RICE = 301L;
            itemDto.setProductId(PRODUCT_ID_RICE);
            itemDto.setQuantity(new BigDecimal("10"));
            itemDto.setPriceSnapshot(new BigDecimal("20000"));

            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("200000"));
            request.setOperationType(INCOME);
            request.setToAccountId(cashbox.getId());
            request.setItems(List.of(itemDto));

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Ошибка валидации: Не указан счет получателя для INCOME")
        void createIncome_withoutAccount_shouldFail() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("100.00"));
            request.setOperationType(INCOME);
            request.setToAccountId(null);

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Поиск и Журнал (GET /)")
    class GetTransactionsTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Фильтр по счету: должен возвращать операции только этого счета")
        void getTransactions_byAccount_shouldFilterCorrectly() throws Exception {
            createTransactionInDb(EXPENSE, cashbox, null, new BigDecimal("100"));
            createTransactionInDb(EXPENSE, bankAccount, null, new BigDecimal("200"));

            mockMvc.perform(get("/api/v1/finance/transactions")
                            .param("accountId", cashbox.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].amount", is(100)));
        }
    }

    @Test
    @WithMockUser(roles = "FINANCE_OFFICER")
    @DisplayName("PAYMENT_FROM_DLR: Оплата от дилера уменьшает его долг и увеличивает кассу")
    void createPaymentFromDealer_shouldReduceDebtAndIncreaseCash() throws Exception {
        FinanceAccount dealerAccount = createAccount("Дилер Алишер", FinanceAccountType.DEALER, new BigDecimal("5000000.00"));

        FinanceAccount shopCashbox = createAccount("Касса Офиса", FinanceAccountType.CASHBOX, new BigDecimal("1000000.00"));

        BigDecimal paymentAmount = new BigDecimal("2000000.00");

        TransactionCreateRequestDto request = new TransactionCreateRequestDto();
        request.setAmount(paymentAmount);
        request.setOperationType(FinanceOperationType.PAYMENT_FROM_DLR);
        request.setFromAccountId(dealerAccount.getId());
        request.setToAccountId(shopCashbox.getId());
        request.setDescription("Частичное погашение долга");

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        FinanceAccount updatedDealer = accountRepository.findById(dealerAccount.getId()).orElseThrow();
        FinanceAccount updatedCashbox = accountRepository.findById(shopCashbox.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("3000000.00").compareTo(updatedDealer.getBalance()),
                "Баланс дилера (его долг нам) должен уменьшиться");

        assertEquals(0, new BigDecimal("3000000.00").compareTo(updatedCashbox.getBalance()),
                "Баланс кассы должен увеличиться");
    }

    @Test
    @WithMockUser(roles = "FINANCE_OFFICER")
    @DisplayName("OWNER_WITHDRAW: Вывод денег владельцем уменьшает кассу и фиксирует изъятие")
    void createOwnerWithdraw_shouldMoveMoneyToOwnerAccount() throws Exception {
        FinanceAccount companySafe = createAccount("Сейф Фирмы", FinanceAccountType.CASHBOX, new BigDecimal("50000000.00"));

        FinanceAccount ownerPocket = createAccount("Личный кошелек Шефа", FinanceAccountType.OWNER, new BigDecimal("0.00"));

        BigDecimal withdrawAmount = new BigDecimal("10000000.00");

        TransactionCreateRequestDto request = new TransactionCreateRequestDto();
        request.setAmount(withdrawAmount);
        request.setOperationType(FinanceOperationType.OWNER_WITHDRAW);
        request.setFromAccountId(companySafe.getId());
        request.setToAccountId(ownerPocket.getId());
        request.setDescription("На отпуск");

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        FinanceAccount updatedSafe = accountRepository.findById(companySafe.getId()).orElseThrow();
        FinanceAccount updatedOwner = accountRepository.findById(ownerPocket.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("40000000.00").compareTo(updatedSafe.getBalance()),
                "Баланс сейфа должен уменьшиться");

        assertEquals(0, new BigDecimal("10000000.00").compareTo(updatedOwner.getBalance()),
                "Счет владельца должен показать сумму изъятия");
    }

    private void createTransactionInDb(FinanceOperationType type, FinanceAccount from, FinanceAccount to, BigDecimal amount) {
        var trx = new TransactionCreateRequestDto();
        trx.setAmount(amount);
        trx.setOperationType(type);
        trx.setTransactionDate(now());
        trx.setDescription("Test transaction");

        if (from != null) {
            trx.setFromAccountId(from.getId());
        }

        if (to != null) {
            trx.setToAccountId(to.getId());
        }

        financeTransactionServiceImpl.createTransaction(trx);
    }

    @Nested
    @DisplayName("Отмена транзакций (POST /{id}/cancel)")
    class CancelTransactionTests {

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Отмена РАСХОДА должна вернуть деньги в кассу")
        void cancelExpense_shouldRestoreSourceBalance() throws Exception {
            var expenseAmount = new BigDecimal("100.00");

            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(expenseAmount);
            request.setOperationType(FinanceOperationType.EXPENSE);
            request.setFromAccountId(cashbox.getId());
            request.setDescription("Ошибочный расход");

            String responseJson = mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long trxId = objectMapper.readTree(responseJson).get("id").asLong();

            FinanceAccount cashboxAfterExpense = accountRepository.findById(cashbox.getId()).orElseThrow();
            assertEquals(0, new BigDecimal("900.00").compareTo(cashboxAfterExpense.getBalance()));

            var cancelDto = new CancellationRequestDto();
            cancelDto.setReason("Ошибка ввода");

            mockMvc.perform(post("/api/v1/finance/transactions/" + trxId + "/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDto)))
                    .andExpect(status().isOk());

            FinanceTransaction cancelledTrx = transactionRepository.findById(trxId).orElseThrow();
            assertEquals(TransactionStatus.CANCELLED, cancelledTrx.getStatus());
            assertEquals("Ошибка ввода", cancelledTrx.getCancellationReason());

            FinanceAccount restoredCashbox = accountRepository.findById(cashbox.getId()).orElseThrow();
            assertEquals(0, new BigDecimal("1000.00").compareTo(restoredCashbox.getBalance()),
                    "Баланс должен восстановиться после отмены расхода");
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Отмена ПЕРЕВОДА должна откатить оба счета")
        void cancelTransfer_shouldRollbackBothAccounts() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("5000.00"));
            request.setOperationType(FinanceOperationType.TRANSFER);
            request.setFromAccountId(bankAccount.getId());
            request.setToAccountId(cashbox.getId());

            String responseJson = mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString();
            long trxId = objectMapper.readTree(responseJson).get("id").asLong();

            CancellationRequestDto cancelDto = new CancellationRequestDto();
            cancelDto.setReason("Тест отката");

            mockMvc.perform(post("/api/v1/finance/transactions/" + trxId + "/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDto)))
                    .andExpect(status().isOk());

            FinanceAccount bank = accountRepository.findById(bankAccount.getId()).orElseThrow();
            FinanceAccount cash = accountRepository.findById(cashbox.getId()).orElseThrow();

            assertEquals(0, new BigDecimal("50000.00").compareTo(bank.getBalance()));
            assertEquals(0, new BigDecimal("1000.00").compareTo(cash.getBalance()));
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Обычный сотрудник НЕ может отменить старую транзакцию (>72ч)")
        void cancelOldTransaction_whenRegularUser_shouldFail() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("100.00"));
            request.setOperationType(FinanceOperationType.EXPENSE);
            request.setFromAccountId(cashbox.getId());

            String responseJson = mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString();
            Long trxId = objectMapper.readTree(responseJson).get("id").asLong();

            jdbcTemplate.update("UPDATE finance_transaction SET created_at = ? WHERE id = ?",
                    Timestamp.from(now().minus(4, ChronoUnit.DAYS)),
                    trxId);

            var cancelDto = new CancellationRequestDto();
            cancelDto.setReason("Попытка обмана истории");

            entityManager.clear();

            mockMvc.perform(post("/api/v1/finance/transactions/" + trxId + "/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("Cancellation period has expired")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Админ МОЖЕТ отменить старую транзакцию (>72ч)")
        void cancelOldTransaction_whenAdmin_shouldSucceed() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("100.00"));
            request.setOperationType(FinanceOperationType.EXPENSE);
            request.setFromAccountId(cashbox.getId());

            String responseJson = mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString();
            Long trxId = objectMapper.readTree(responseJson).get("id").asLong();

            jdbcTemplate.update("UPDATE finance_transaction SET created_at = ? WHERE id = ?",
                    Timestamp.from(now().minus(5, ChronoUnit.DAYS)),
                    trxId);

            CancellationRequestDto cancelDto = new CancellationRequestDto();
            cancelDto.setReason("Административная корректировка");

            mockMvc.perform(post("/api/v1/finance/transactions/" + trxId + "/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDto)))
                    .andExpect(status().isOk());

            var trx = transactionRepository.findById(trxId).orElseThrow();
            assertEquals(TransactionStatus.CANCELLED, trx.getStatus());
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Нельзя отменить транзакцию дважды")
        void cancelTransaction_alreadyCancelled_shouldFail() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("10.00"));
            request.setOperationType(FinanceOperationType.EXPENSE);
            request.setFromAccountId(cashbox.getId());

            String responseJson = mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString();
            long trxId = objectMapper.readTree(responseJson).get("id").asLong();

            CancellationRequestDto cancelDto = new CancellationRequestDto();
            cancelDto.setReason("Первая отмена");

            mockMvc.perform(post("/api/v1/finance/transactions/" + trxId + "/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDto)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/finance/transactions/" + trxId + "/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("This transaction is already")));
        }
    }

    @Nested
    @DisplayName("Зарплатный проект (SALARY операций)")
    class SalaryTests {

        private FinanceAccount employeeAccount;
        private FinanceCategory salaryCategory;

        @BeforeEach
        void setUpSalaryData() {
            employeeAccount = createAccount("Сотрудник Али", FinanceAccountType.EMPLOYEE, BigDecimal.ZERO);

            salaryCategory = new FinanceCategory();
            salaryCategory.setName("Фонд Оплаты Труда");
            salaryCategory.setType("EXPENSE");
            categoryRepository.save(salaryCategory);
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Сценарий 1: Выдача Аванса (SALARY_PAYOUT)")
        void giveAdvance_shouldIncreaseEmployeeBalance() throws Exception {
            cashbox.setBalance(new BigDecimal("10000000.00"));
            accountRepository.save(cashbox);

            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("1000000.00"));
            request.setOperationType(FinanceOperationType.SALARY_PAYOUT);
            request.setFromAccountId(cashbox.getId());
            request.setToAccountId(employeeAccount.getId());
            request.setDescription("Аванс за Октябрь");

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            FinanceAccount updatedEmployee = accountRepository.findById(employeeAccount.getId()).orElseThrow();
            FinanceAccount updatedCashbox = accountRepository.findById(cashbox.getId()).orElseThrow();

            assertEquals(0, new BigDecimal("1000000.00").compareTo(updatedEmployee.getBalance()),
                    "Баланс сотрудника должен стать положительным после аванса");

            assertEquals(0, new BigDecimal("9000000.00").compareTo(updatedCashbox.getBalance()));
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Сценарий 2: Начисление Зарплаты (SALARY_ACCRUAL)")
        void accrueSalary_shouldDecreaseEmployeeBalance() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("5000000.00"));
            request.setOperationType(FinanceOperationType.SALARY_ACCRUAL);
            request.setFromAccountId(employeeAccount.getId());
            request.setToAccountId(null);
            request.setCategoryId(salaryCategory.getId());
            request.setDescription("ЗП за Октябрь");

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            FinanceAccount updatedEmployee = accountRepository.findById(employeeAccount.getId()).orElseThrow();

            assertEquals(0, new BigDecimal("-5000000.00").compareTo(updatedEmployee.getBalance()),
                    "Баланс сотрудника должен стать отрицательным после начисления");
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Сценарий 3: Полный цикл (Аванс -> Начисление -> Расчет)")
        void fullSalaryCycle_shouldResultInZeroBalance() throws Exception {
            cashbox.setBalance(new BigDecimal("10000000.00"));
            accountRepository.save(cashbox);

            createTransactionInDb(FinanceOperationType.SALARY_PAYOUT, cashbox, employeeAccount, new BigDecimal("1000000"));

            createTransactionInDb(FinanceOperationType.SALARY_ACCRUAL, employeeAccount, null, new BigDecimal("5000000"));

            FinanceAccount midEmployee = accountRepository.findById(employeeAccount.getId()).orElseThrow();
            assertEquals(0, new BigDecimal("-4000000.00").compareTo(midEmployee.getBalance()));

            TransactionCreateRequestDto payoutRequest = new TransactionCreateRequestDto();
            payoutRequest.setAmount(new BigDecimal("4000000.00"));
            payoutRequest.setOperationType(FinanceOperationType.SALARY_PAYOUT);
            payoutRequest.setFromAccountId(cashbox.getId());
            payoutRequest.setToAccountId(employeeAccount.getId());
            payoutRequest.setDescription("Выплата остатка");

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payoutRequest)))
                    .andExpect(status().isCreated());

            FinanceAccount finalEmployee = accountRepository.findById(employeeAccount.getId()).orElseThrow();
            FinanceAccount finalCashbox = accountRepository.findById(cashbox.getId()).orElseThrow();

            assertEquals(0, BigDecimal.ZERO.compareTo(finalEmployee.getBalance()),
                    "После полного расчета баланс сотрудника должен быть 0");

            assertEquals(0, new BigDecimal("5000000.00").compareTo(finalCashbox.getBalance()));
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        @DisplayName("Валидация: Нельзя начислить ЗП с кассы (ошибка типов)")
        void accrueSalary_wrongAccountType_shouldFail() throws Exception {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto();
            request.setAmount(new BigDecimal("100.00"));
            request.setOperationType(FinanceOperationType.SALARY_ACCRUAL);
            request.setFromAccountId(cashbox.getId());
            request.setToAccountId(null);

            mockMvc.perform(post("/api/v1/finance/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("(From) of type EMPLOYEE")));
        }
    }
}