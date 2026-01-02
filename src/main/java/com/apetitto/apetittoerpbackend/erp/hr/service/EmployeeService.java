package com.apetitto.apetittoerpbackend.erp.hr.service;

import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeCreateDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponseDto createEmployee(EmployeeCreateDto dto);

    EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto);

    EmployeeResponseDto getEmployeeById(Long id);

    Page<EmployeeResponseDto> getAllEmployees(Pageable pageable);

    void dismissEmployee(Long id);
}