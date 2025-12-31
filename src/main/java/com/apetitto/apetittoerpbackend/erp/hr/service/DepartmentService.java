package com.apetitto.apetittoerpbackend.erp.hr.service;

import com.apetitto.apetittoerpbackend.erp.hr.dto.DepartmentDto;

import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto department);

    DepartmentDto updateDepartment(Long id, DepartmentDto department);

    DepartmentDto getDepartmentById(Long id);

    List<DepartmentDto> getAllDepartments();

    void deleteDepartment(Long id);
}
