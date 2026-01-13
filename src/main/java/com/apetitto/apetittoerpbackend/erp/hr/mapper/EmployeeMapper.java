package com.apetitto.apetittoerpbackend.erp.hr.mapper;

import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeCreateDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.user.model.Role;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.enabled", target = "isUserEnabled")
    @Mapping(target = "fullName", expression = "java(employee.getUser().getFirstName() + \" \" + employee.getUser().getLastName())")
    @Mapping(source = "user.roles", target = "roles", qualifiedByName = "rolesToString")

    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.name", target = "departmentName")

    @Mapping(source = "financeAccount.id", target = "financeAccountId")
    @Mapping(source = "financeAccount.balance", target = "currentBalance")
    EmployeeResponseDto toDto(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    User toUserEntity(EmployeeCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "financeAccount", ignore = true)
    @Mapping(target = "workHoursPerDay", ignore = true)
    @Mapping(target = "hiredAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "updatedAt", ignore = true)
    Employee toEntity(EmployeeCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "financeAccount", ignore = true)
    @Mapping(target = "workHoursPerDay", ignore = true)
    @Mapping(target = "hiredAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(EmployeeUpdateDto dto, @MappingTarget Employee entity);

    @Named("rolesToString")
    default Set<String> rolesToString(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream().map(Role::getName).collect(toSet());
    }
}