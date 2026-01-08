package com.apetitto.apetittoerpbackend.erp.hr.dto;

import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class EmployeeUpdateDto {
    private String firstName;
    private String lastName;
    private String email;

    private Long departmentId;
    private String positionTitle;
    private Boolean isActive;

    private SalaryType salaryType;
    private BigDecimal salaryBase;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private Integer daysOffPerMonth;

    private Long terminalId;
}