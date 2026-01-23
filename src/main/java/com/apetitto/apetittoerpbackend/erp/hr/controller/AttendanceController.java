package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.hr.controller.api.AttendanceApi;
import com.apetitto.apetittoerpbackend.erp.hr.dto.AttendanceUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttendanceController implements AttendanceApi {

    private final AttendanceService attendanceService;

    @Override
    public ResponseEntity<Void> updateAttendance(AttendanceUpdateDto dto) {
        attendanceService.updateAttendance(dto);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/admin/recalculate-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> recalculateAll() {
        attendanceService.recalculateAllHistory();
        return ResponseEntity.ok().build();
    }
}
