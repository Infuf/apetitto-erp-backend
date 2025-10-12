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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Процессы: Перемещения", description = "API для управления перемещениями товаров между складами")
@RequestMapping("/api/v1/transfers")
public interface TransferApi {

    @Operation(summary = "Создание нового заказа на перемещение")
    @PostMapping
    ResponseEntity<TransferOrderDto> createTransfer(@RequestBody TransferOrderRequestDto requestDto);

    @Operation(summary = "Получение списка перемещений с фильтрацией и пагинацией")
    @GetMapping
    ResponseEntity<Page<TransferOrderDto>> getTransfers(
            @Parameter(description = "Фильтр по статусу") @RequestParam(required = false) TransferStatus status,
            @Parameter(description = "Фильтр по ID склада-получателя") @RequestParam(required = false) Long destinationWarehouseId,
            Pageable pageable
    );

    @Operation(summary = "Получение заказа на перемещение по ID")
    @GetMapping("/{id}")
    ResponseEntity<TransferOrderDto> getTransferById(@PathVariable Long id);

    @Operation(summary = "Отправка перемещения", description = "Меняет статус на SHIPPED и списывает товары со склада-отправителя.")
    @PostMapping("/{id}/ship")
    ResponseEntity<TransferOrderDto> shipTransfer(@PathVariable Long id);

    @Operation(summary = "Приемка перемещения", description = "Меняет статус на RECEIVED и оприходует товары на склад-получатель.")
    @PostMapping("/{id}/receive")
    ResponseEntity<TransferOrderDto> receiveTransfer(@PathVariable Long id);
}