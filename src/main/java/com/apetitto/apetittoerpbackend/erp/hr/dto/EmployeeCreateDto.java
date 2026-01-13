package com.apetitto.apetittoerpbackend.erp.hr.dto;

import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class EmployeeCreateDto {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password required")
    @Size(min = 6, message = "Password is too short")
    private String password;

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String email;


    private Long departmentId;

    @NotBlank(message = "The position is mandatory")
    private String positionTitle;

    @NotNull
    private SalaryType salaryType;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal salaryBase;

    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private Integer daysOffPerMonth;
    private Long terminalId;
}