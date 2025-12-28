package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Процессы: Перемещения", description = "API для управления перемещениями товаров между складами")
@RequestMapping("/api/v1/transfers")
public interface TransferApi {

    @Operation(summary = "Создание нового заказа на перемещение")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<TransferOrderDto> createTransfer(@RequestBody TransferOrderRequestDto requestDto);

    @Operation(summary = "Получение списка перемещений с фильтрацией и пагинацией")
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_STORE_MANAGER','ROLE_OWNER')")
    ResponseEntity<Page<TransferOrderDto>> getTransfers(
            @Parameter(description = "Фильтр по статусу") @RequestParam(required = false) TransferStatus status,
            @Parameter(description = "Фильтр по ID склада-получателя") @RequestParam(required = false) Long destinationWarehouseId,
            @Parameter(description = "Начало периода (формат ISO, UTC)") @RequestParam(required = false) Instant dateFrom,
            @Parameter(description = "Конец периода (формат ISO, UTC)") @RequestParam(required = false) Instant dateTo,
            Pageable pageable
    );

    @Operation(summary = "Получение заказа на перемещение по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_STORE_MANAGER')")
    ResponseEntity<TransferOrderDto> getTransferById(@PathVariable Long id);

    @Operation(summary = "Отправка перемещения", description = "Меняет статус на SHIPPED и списывает товары со склада-отправителя.")
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<TransferOrderDto> shipTransfer(@PathVariable Long id);

    @Operation(summary = "Приемка перемещения", description = "Меняет статус на RECEIVED и оприходует товары на склад-получатель.")
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_STORE_MANAGER')")
    ResponseEntity<TransferOrderDto> receiveTransfer(@PathVariable Long id);
}