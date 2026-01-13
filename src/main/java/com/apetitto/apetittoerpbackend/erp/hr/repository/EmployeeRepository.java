package com.apetitto.apetittoerpbackend.erp.hr.repository;

import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByUserId(Long userId);

    List<Employee> findAllByDepartmentIdAndIsActiveIsTrue(Long departmentId);

    Optional<Employee> findByUserId(Long userId);
}
