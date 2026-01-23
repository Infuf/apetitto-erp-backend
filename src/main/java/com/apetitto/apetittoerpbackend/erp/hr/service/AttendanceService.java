package com.apetitto.apetittoerpbackend.erp.hr.service;

import com.apetitto.apetittoerpbackend.erp.hr.dto.AttendanceUpdateDto;

public interface AttendanceService {
    void updateAttendance(AttendanceUpdateDto dto);

    void updateAttendanceSystem(AttendanceUpdateDto dto);

}
