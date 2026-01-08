package com.apetitto.apetittoerpbackend.erp.hr.mapper;

import com.apetitto.apetittoerpbackend.erp.hr.dto.DepartmentDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "manager.id", target = "managerId")
    @Mapping(target = "managerName", expression = "java(department.getManager() != null ? " +
            "department.getManager().getFirstName() + \" \" + department.getManager().getLastName() : null)")
    DepartmentDto toDto(Department department);

    List<DepartmentDto> toDtoList(List<Department> departments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    Department toEntity(DepartmentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    void updateEntity(DepartmentDto dto, @MappingTarget Department entity);
}