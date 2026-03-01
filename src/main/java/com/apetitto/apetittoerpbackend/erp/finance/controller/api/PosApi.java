package com.apetitto.apetittoerpbackend.erp.finance.controller.api;

import com.apetitto.apetittoerpbackend.erp.finance.dto.PosSaleRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(
        name = "Продажи POS",
        description = "Операции обработки продаж, поступающих из кассовой системы (POS)"
)
@RequestMapping("/api/pos")
public interface PosApi {

    @Operation(
            summary = "Обработка продажи из POS",
            description = """
                    Обрабатывает продажу, поступившую из кассовой системы.

                    В рамках одной транзакции выполняется:
                    • Списание товара со склада
                    • Создание финансовой операции типа ДОХОД
                    • Обновление баланса счета

                    Если происходит ошибка (недостаточно товара, неверный счет и т.д.),
                    операция полностью откатывается.
                    """
    )
    @PostMapping("/sale")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'OWNER')")
    ResponseEntity<Void> processSale(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные продажи из POS",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PosSaleRequestDto.class))
            )
            @RequestBody PosSaleRequestDto request
    );
}