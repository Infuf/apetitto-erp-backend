package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "Справочник: Товары",
        description = "API для создания, редактирования, поиска и удаления товаров."
)
@RequestMapping("/api/v1/products")
public interface ProductApi {

    @Operation(summary = "Создание нового товара")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Товар успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto);

    @Operation(summary = "Получение списка всех товаров с пагинацией")
    @Parameters({
            @Parameter(name = "page", description = "Номер страницы (начиная с 0)", example = "0"),
            @Parameter(name = "size", description = "Количество элементов на странице", example = "10"),
            @Parameter(name = "sort", description = "Сортировка (например: name,asc или createdAt,desc)", example = "name,asc")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список товаров успешно получен",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<Page<ProductDto>> getAllProducts(
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    );

    @Operation(summary = "Получение товара по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<ProductDto> getProductById(@PathVariable Long id);

    @Operation(summary = "Обновление существующего товара",
            description = "Обновляет товар по его ID. ID должен быть указан в теле запроса.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    })
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto productDto);


    @Operation(summary = "Удаление товара по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Товар успешно удален", content = @Content),
            @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<Void> deleteProduct(@PathVariable Long id);


    @Operation(summary = "Поиск товаров по имени с пагинацией",
            description = "Поиск по частичному совпадению имени без учета регистра.")
    @Parameters({
            @Parameter(name = "name", description = "Часть имени товара для поиска", example = "Пицца"),
            @Parameter(name = "page", description = "Номер страницы (начиная с 0)", example = "0"),
            @Parameter(name = "size", description = "Количество элементов на странице", example = "10"),
            @Parameter(name = "sort", description = "Сортировка (например: name,asc)", example = "name,asc")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список найденных товаров",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<Page<ProductDto>> searchProductsByName(
            @RequestParam String name,
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    );
}