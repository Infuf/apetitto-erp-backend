package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.hr.controller.api.AttendanceDashboardApi;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.AttendanceGridResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.EmployeeExtendedDetailDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.SingleAttendanceRecordDto;
import com.apetitto.apetittoerpbackend.erp.hr.service.AttendanceDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class AttendanceDashboardController implements AttendanceDashboardApi {

    private final AttendanceDashboardService attendanceDashboardService;

    @Override
    public ResponseEntity<AttendanceGridResponseDto> getAttendanceGrid(Long departmentId, LocalDate dateFrom, LocalDate dateTo) {
        return ResponseEntity.ok(attendanceDashboardService.getAttendanceGrid(departmentId, dateFrom, dateTo));
    }

    @Override
    public ResponseEntity<EmployeeExtendedDetailDto> getEmployeeDetails(Long employeeId, LocalDate dateFrom, LocalDate dateTo) {
        return ResponseEntity.ok(attendanceDashboardService.getEmployeeDetails(employeeId, dateFrom, dateTo));
    }

    @Override
    public ResponseEntity<SingleAttendanceRecordDto> getSingleRecord(Long employeeId, LocalDate date) {
        return ResponseEntity.ok(attendanceDashboardService.getSingleRecord(employeeId, date));
    }
}
