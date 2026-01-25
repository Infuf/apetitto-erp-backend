package com.apetitto.apetittoerpbackend.erp.hr.dto.payroll;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PayrollAccrualDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String departmentName;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;

    private BigDecimal finalAmount;
    private BigDecimal baseAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal bonusAmount;

    private Integer daysWorked;
    private BigDecimal totalWorkedHours;
    private BigDecimal totalUndertimeHours;
    private BigDecimal totalOvertimeHours;

    private BigDecimal calculatedDayRate;
    private BigDecimal calculatedHourRate;
}