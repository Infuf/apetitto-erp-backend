package com.apetitto.apetittoerpbackend.erp.hr.dto.payroll;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PayrollRequestDto {
    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    private Long departmentId;
    private Long employeeId;
}