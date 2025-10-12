package com.apetitto.apetittoerpbackend.erp.warehouse.mapper;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.unit", target = "unit")
    @Mapping(target = "quantity", ignore = true)
    StockItemDto toDto(StockItem stockItem);

    List<StockItemDto> toDtoList(List<StockItem> stockItems);


    @AfterMapping
    default void convertQuantityToUserFriendlyFormat(StockItem stockItem, @MappingTarget StockItemDto dto) {
        if (stockItem.getProduct() != null && stockItem.getProduct().getUnit() != null) {
            dto.setQuantity(stockItem
                    .getProduct()
                    .getUnit()
                    .fromBaseUnit(stockItem
                            .getQuantity()));
        }
    }
}
