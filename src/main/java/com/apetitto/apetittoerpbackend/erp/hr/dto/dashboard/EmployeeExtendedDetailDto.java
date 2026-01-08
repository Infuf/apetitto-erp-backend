package com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeExtendedDetailDto {
    private LocalDate fromDate;
    private LocalDate toDate;

    private Long employeeId;
    private String fullName;
    private String position;
    private String departmentName;
    private BigDecimal baseSalary;
    private String salaryType;

    private Integer totalLateMinutes;
    private Integer totalEarlyLeaveMinutes;
    private Integer totalShortcomingMinutes;
    private Integer totalOvertimeMinutes;
    private Integer totalWorkedMinutes;

    private List<DailyAttendanceDetailDto> days;

    private FinanceStatsDto finance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendanceDetailDto {
        private LocalDate date;
        private String status;
        private String checkIn;
        private String checkOut;

        private Integer lateMinutes;
        private Integer earlyLeaveMinutes;
        private Integer shortcomingMinutes;
        private Integer overtimeMinutes;
        private Integer workingMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinanceStatsDto {
        private BigDecimal currentBalance;
        private BigDecimal totalTakenInPeriod;
        private List<EmployeeFinanceTransactionDto> transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeFinanceTransactionDto {
        private Long id;
        private Instant transactionDate;
        private BigDecimal amount;
        private String type;
        private String description;
    }
}