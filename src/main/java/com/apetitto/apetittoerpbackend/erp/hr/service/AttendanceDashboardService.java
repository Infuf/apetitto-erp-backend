package com.apetitto.apetittoerpbackend.erp.hr.service;

import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.AttendanceGridResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.EmployeeExtendedDetailDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.SingleAttendanceRecordDto;

import java.time.LocalDate;

public interface AttendanceDashboardService {
    AttendanceGridResponseDto getAttendanceGrid(Long departmentId, LocalDate dateFrom, LocalDate dateTo);

    EmployeeExtendedDetailDto getEmployeeDetails(Long employeeId, LocalDate dateFrom, LocalDate dateTo);

    SingleAttendanceRecordDto getSingleRecord(Long employeeId, LocalDate date);
}
