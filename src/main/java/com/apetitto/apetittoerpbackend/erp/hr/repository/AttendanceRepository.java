package com.apetitto.apetittoerpbackend.erp.hr.repository;

import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    @Query("SELECT ar FROM AttendanceRecord ar " +
            "WHERE ar.employee.id IN :employeeIds " +
            "AND ar.date BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findAllByEmployeeIdsAndDateRange(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
