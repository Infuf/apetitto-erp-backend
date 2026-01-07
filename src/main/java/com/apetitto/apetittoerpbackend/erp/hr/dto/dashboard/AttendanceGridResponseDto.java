package com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceGridResponseDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer totalWorkingDays;

    private List<EmployeeGridRowDto> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeGridRowDto {
        private Long employeeId;
        private String fullName;
        private String positionTitle;

        private Integer totalWorkedHours;
        private Integer totalShortcomingMinutes;
        private Integer totalOvertimeMinutes;

        private String standardStartTime;
        private String standardEndTime;

        private Map<String, GridDayDto> days;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GridDayDto {
        private String status;

        private String checkIn;
        private String checkOut;

        private Integer shortcomingMinutes;
        private Integer overtimeMinutes;

        private Long recordId;
    }
}