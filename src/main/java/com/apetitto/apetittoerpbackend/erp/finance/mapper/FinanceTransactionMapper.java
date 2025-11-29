package com.apetitto.apetittoerpbackend.erp.finance.mapper;

import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionDetailDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransactionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
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

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.productCode", target = "productCode")
    TransactionDetailDto.ItemDetailDto toItemDto(FinanceTransactionItem item);
}
