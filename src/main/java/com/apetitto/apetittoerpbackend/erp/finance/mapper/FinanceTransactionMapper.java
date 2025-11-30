package com.apetitto.apetittoerpbackend.erp.finance.mapper;

import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionDetailDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransactionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class})
public interface FinanceTransactionMapper {

    @Mapping(source = "fromAccount.id", target = "fromAccountId")
    @Mapping(source = "fromAccount.name", target = "fromAccountName")
    @Mapping(source = "toAccount.id", target = "toAccountId")
    @Mapping(source = "toAccount.name", target = "toAccountName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "subCategory.id", target = "subcategoryId")
    @Mapping(source = "subCategory.name", target = "subcategoryName")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "createdBy.username", target = "createdByName")
    TransactionResponseDto toDto(FinanceTransaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", constant = "COMPLETED")

    @Mapping(target = "fromAccount", ignore = true)
    @Mapping(target = "toAccount", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "transactionDate", expression = "java(dto.getTransactionDate() != null ? dto.getTransactionDate()" +
            " : Instant.now())")
    FinanceTransaction toEntity(TransactionCreateRequestDto dto);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.productCode", target = "productCode")
    TransactionDetailDto.ItemDetailDto toItemDto(FinanceTransactionItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    FinanceTransactionItem toItemEntity(TransactionCreateRequestDto.TransactionItemDto dto);
}