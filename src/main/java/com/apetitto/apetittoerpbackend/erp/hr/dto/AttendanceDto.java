package com.apetitto.apetittoerpbackend.erp.hr.dto;

import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceDto {

    private Long id;

    @NotNull(message = "Employee is required")
    private Long employeeId;
    private String employeeName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private LocalTime checkIn;
    private LocalTime checkOut;

    @NotNull
    private AttendanceStatus status;

    private Integer durationMinutes;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private Integer overtimeMinutes;

    private LocalTime expectedStartTime;
    private LocalTime expectedEndTime;
}