package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceCategory;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType.*;
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
    private UserRepository userRepository;

    private FinanceAccount cashbox;
    private FinanceAccount bankAccount;
    private FinanceAccount supplier;
    private FinanceCategory categorySales;

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


        private void createTransactionInDb(FinanceOperationType type, FinanceAccount from, FinanceAccount to, BigDecimal amount) {
            FinanceTransaction trx = new FinanceTransaction();
            trx.setAmount(amount);
            trx.setOperationType(type);
            trx.setTransactionDate(java.time.Instant.now());
            trx.setStatus("COMPLETED");
            trx.setDescription("Test transaction");

            if (from != null) trx.setFromAccount(from);
            if (to != null) trx.setToAccount(to);

            User admin = userRepository.findById(1L).orElse(null);
            trx.setCreatedBy(admin);

            transactionRepository.save(trx);
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
}