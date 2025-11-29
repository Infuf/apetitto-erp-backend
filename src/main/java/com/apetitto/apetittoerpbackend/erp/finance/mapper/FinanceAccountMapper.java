package com.apetitto.apetittoerpbackend.erp.finance.mapper;

import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceAccountDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinanceAccountMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    FinanceAccountDto toDto(FinanceAccount account);

    List<FinanceAccountDto> toDtoList(List<FinanceAccount> accounts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(FinanceAccountDto dto, @MappingTarget FinanceAccount entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "user", ignore = true)
    FinanceAccount toEntity(FinanceAccountDto dto);
}