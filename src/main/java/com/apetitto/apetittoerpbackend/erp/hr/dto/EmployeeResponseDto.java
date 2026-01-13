package com.apetitto.apetittoerpbackend.erp.hr.dto;

import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

@Data
public class EmployeeResponseDto {
    private Long id;

    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Set<String> roles;
    private Boolean isUserEnabled;

    private Long departmentId;
    private String departmentName;
    private String positionTitle;
    private Boolean isActive;

    private Long financeAccountId;
    private BigDecimal currentBalance;

    private SalaryType salaryType;
    private BigDecimal salaryBase;
    private Integer daysOffPerMonth;
    private Long terminalId;

    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private Instant hiredAt;
}
