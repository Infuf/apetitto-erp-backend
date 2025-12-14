package com.apetitto.apetittoerpbackend.erp.integration;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DisplayName("Интеграция Склад -> Финансы")
class WarehouseFinanceIntegrationTest {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private FinanceAccountRepository accountRepository;

    @Autowired
    private FinanceTransactionRepository transactionRepository;
    private final Long WAREHOUSE_ID_MAIN = 101L;
    private final Long PRODUCT_ID_RICE = 302L;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("INBOUND от Поставщика должен создать долг (SUPPLIER_INVOICE)")
    void inboundFromSupplier_shouldCreateDebt() {
        FinanceAccount supplier = createAccount("Поставщик Мука", FinanceAccountType.SUPPLIER);

        StockMovementRequestDto request = new StockMovementRequestDto();
        request.setWarehouseId(WAREHOUSE_ID_MAIN);
        request.setMovementType(MovementType.INBOUND);
        request.setFinanceAccountId(supplier.getId());
        request.setComment("Поставка сырья");

        StockMovementRequestDto.Item item = new StockMovementRequestDto.Item();
        item.setProductId(PRODUCT_ID_RICE);
        item.setQuantity(new BigDecimal("100"));
        item.setCostPrice(new BigDecimal("5000"));
        request.setItems(List.of(item));

        warehouseService.processStockMovement(request);

        List<FinanceTransaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        FinanceTransaction trx = transactions.get(0);

        assertThat(trx.getOperationType()).isEqualTo(FinanceOperationType.SUPPLIER_INVOICE);
        assertThat(trx.getAmount()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(trx.getFromAccount().getId()).isEqualTo(supplier.getId());

        FinanceAccount updatedSupplier = accountRepository.findById(supplier.getId()).orElseThrow();
        assertThat(updatedSupplier.getBalance()).isEqualByComparingTo(new BigDecimal("-500000"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("OUTBOUND Дилеру должен создать дебиторку (DEALER_INVOICE)")
    void outboundToDealer_shouldCreateReceivable() {
        FinanceAccount dealer = createAccount("Дилер Алишер", FinanceAccountType.DEALER);
        StockMovementRequestDto request = new StockMovementRequestDto();
        request.setWarehouseId(WAREHOUSE_ID_MAIN);
        request.setMovementType(MovementType.OUTBOUND);
        request.setFinanceAccountId(dealer.getId());

        StockMovementRequestDto.Item item = new StockMovementRequestDto.Item();
        item.setProductId(PRODUCT_ID_RICE);
        item.setQuantity(new BigDecimal("10"));
        request.setItems(List.of(item));

        warehouseService.processStockMovement(request);

        List<FinanceTransaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        FinanceTransaction trx = transactions.get(0);

        assertThat(trx.getOperationType()).isEqualTo(FinanceOperationType.DEALER_INVOICE);
        assertThat(trx.getAmount()).isEqualByComparingTo(new BigDecimal("250000.00"));
        assertThat(trx.getToAccount().getId()).isEqualTo(dealer.getId());

        FinanceAccount updatedDealer = accountRepository.findById(dealer.getId()).orElseThrow();
        assertThat(updatedDealer.getBalance()).isEqualByComparingTo(new BigDecimal("250000.00"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("SELL (POS) не должен создавать финансовых транзакций")
    void sellOperation_shouldNotTouchFinance() {
        FinanceAccount shop = createAccount("Магазин", FinanceAccountType.CASHBOX);

        StockMovementRequestDto request = new StockMovementRequestDto();
        request.setWarehouseId(WAREHOUSE_ID_MAIN);
        request.setMovementType(MovementType.SELL);
        request.setFinanceAccountId(shop.getId());

        StockMovementRequestDto.Item item = new StockMovementRequestDto.Item();
        item.setProductId(PRODUCT_ID_RICE);
        item.setQuantity(new BigDecimal("1"));
        request.setItems(List.of(item));

        warehouseService.processStockMovement(request);

        assertThat(transactionRepository.findAll()).isEmpty();

        FinanceAccount updatedShop = accountRepository.findById(shop.getId()).orElseThrow();
        assertThat(updatedShop.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Внутреннее перемещение (без партнера) не создает транзакций")
    void internalMovement_shouldNotTouchFinance() {
        StockMovementRequestDto request = new StockMovementRequestDto();
        request.setWarehouseId(WAREHOUSE_ID_MAIN);
        request.setMovementType(MovementType.OUTBOUND);
        request.setFinanceAccountId(null);

        StockMovementRequestDto.Item item = new StockMovementRequestDto.Item();
        item.setProductId(PRODUCT_ID_RICE);
        item.setQuantity(new BigDecimal("5"));
        request.setItems(List.of(item));

        warehouseService.processStockMovement(request);

        assertThat(transactionRepository.findAll()).isEmpty();
    }

    private FinanceAccount createAccount(String name, FinanceAccountType type) {
        FinanceAccount account = new FinanceAccount();
        account.setName(name);
        account.setType(type);
        account.setBalance(BigDecimal.ZERO);
        account.setIsActive(true);
        return accountRepository.save(account);
    }
}