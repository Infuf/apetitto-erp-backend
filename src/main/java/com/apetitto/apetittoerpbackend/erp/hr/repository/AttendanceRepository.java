package com.apetitto.apetittoerpbackend.erp.hr.repository;

import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

}
