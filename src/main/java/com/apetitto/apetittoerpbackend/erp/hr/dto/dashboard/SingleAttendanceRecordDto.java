package com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleAttendanceRecordDto {
    private Long id;

    private Long employeeId;
    private String employeeName;
    private LocalDate date;

    private LocalTime actualCheckIn;
    private LocalTime actualCheckOut;
    private String status;

    private LocalTime expectedStartTime;
    private LocalTime expectedEndTime;

    private Integer durationMinutes;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private Integer totalShortcomingMinutes;
    private Integer overtimeMinutes;
}