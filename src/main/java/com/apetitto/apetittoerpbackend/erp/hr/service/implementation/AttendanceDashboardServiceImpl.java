package com.apetitto.apetittoerpbackend.erp.hr.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.AccessDeniedException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.AttendanceGridResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.EmployeeExtendedDetailDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.SingleAttendanceRecordDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.service.AttendanceDashboardService;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class AttendanceDashboardServiceImpl implements AttendanceDashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final FinanceTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Tashkent");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public AttendanceGridResponseDto getAttendanceGrid(Long departmentId, LocalDate dateFrom, LocalDate dateTo) {

        Long targetDeptId = resolveDepartmentId(departmentId);

        List<Employee> employees = employeeRepository.findAllByDepartmentIdAndIsActiveIsTrue(targetDeptId);
        if (employees.isEmpty()) {
            return AttendanceGridResponseDto.builder()
                    .fromDate(dateFrom).toDate(dateTo).rows(Collections.emptyList()).build();
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).toList();

        List<AttendanceRecord> records = attendanceRepository.findAllByEmployeeIdsAndDateRange(employeeIds, dateFrom, dateTo);

        Map<Long, Map<LocalDate, AttendanceRecord>> recordsMap = records.stream()
                .collect(groupingBy(
                        r -> r.getEmployee().getId(),
                        toMap(AttendanceRecord::getDate, Function.identity())
                ));

        List<AttendanceGridResponseDto.EmployeeGridRowDto> rows = new ArrayList<>();
        LocalDate today = LocalDate.now(ZONE_ID);

        for (Employee emp : employees) {
            Map<LocalDate, AttendanceRecord> empRecords = recordsMap.getOrDefault(emp.getId(), Collections.emptyMap());
            Map<String, AttendanceGridResponseDto.GridDayDto> daysDtoMap = new LinkedHashMap<>();

            int totalWorkedHours = 0;
            int totalShortcoming = 0;
            int totalOvertime = 0;

            for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
                AttendanceRecord record = empRecords.get(date);
                AttendanceGridResponseDto.GridDayDto dayDto = buildDayCell(date, record, today);

                if (record != null) {
                    totalWorkedHours += record.getDurationMinutes();
                    totalShortcoming += (record.getLateMinutes() + record.getEarlyLeaveMinutes());
                    totalOvertime += record.getOvertimeMinutes();
                }

                daysDtoMap.put(date.toString(), dayDto);
            }

            rows.add(AttendanceGridResponseDto.EmployeeGridRowDto.builder()
                    .employeeId(emp.getId())
                    .fullName(emp.getUser().getFirstName() + " " + emp.getUser().getLastName())
                    .positionTitle(emp.getPositionTitle())
                    .totalWorkedHours(totalWorkedHours / 60)
                    .totalShortcomingMinutes(totalShortcoming)
                    .totalOvertimeMinutes(totalOvertime)
                    .standardStartTime(formatTime(emp.getShiftStartTime()))
                    .standardEndTime(formatTime(emp.getShiftEndTime()))
                    .days(daysDtoMap)
                    .build());
        }

        return AttendanceGridResponseDto.builder()
                .fromDate(dateFrom)
                .toDate(dateTo)
                .totalWorkingDays(calculateWorkingDays(dateFrom, dateTo))
                .rows(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeExtendedDetailDto getEmployeeDetails(Long employeeId, LocalDate dateFrom, LocalDate dateTo) {
        Employee employee = validateEmployeeAccess(employeeId);

        List<AttendanceRecord> records = attendanceRepository.findAllByEmployeeIdsAndDateRange(
                List.of(employeeId), dateFrom, dateTo);

        Map<LocalDate, AttendanceRecord> recordMap = records.stream()
                .collect(toMap(AttendanceRecord::getDate, Function.identity()));

        Instant startInstant = dateFrom.atStartOfDay(ZONE_ID).toInstant();
        Instant endInstant = dateTo.atTime(LocalTime.MAX).atZone(ZONE_ID).toInstant();

        List<FinanceTransaction> transactions = transactionRepository.findEmployeeHistory(
                employee.getFinanceAccount().getId(), startInstant, endInstant);

        List<EmployeeExtendedDetailDto.DailyAttendanceDetailDto> days = new ArrayList<>();
        int totalLate = 0, totalEarly = 0, totalOver = 0, totalWorked = 0;

        for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
            AttendanceRecord record = recordMap.get(date);
            var dayDto = buildDetailDay(date, record);

            if (record != null) {
                totalLate += record.getLateMinutes();
                totalEarly += record.getEarlyLeaveMinutes();
                totalOver += record.getOvertimeMinutes();
                totalWorked += record.getDurationMinutes();
            }
            days.add(dayDto);
        }

        BigDecimal totalTaken = BigDecimal.ZERO;
        List<EmployeeExtendedDetailDto.EmployeeFinanceTransactionDto> financeDtos = new ArrayList<>();

        for (FinanceTransaction trx : transactions) {
            financeDtos.add(EmployeeExtendedDetailDto.EmployeeFinanceTransactionDto.builder()
                    .id(trx.getId())
                    .transactionDate(trx.getTransactionDate())
                    .amount(trx.getAmount())
                    .type(trx.getOperationType().name())
                    .description(trx.getDescription())
                    .build());

            if (trx.getOperationType() == FinanceOperationType.SALARY_PAYOUT) {
                totalTaken = totalTaken.add(trx.getAmount());
            }
        }

        return EmployeeExtendedDetailDto.builder()
                .fromDate(dateFrom).toDate(dateTo)
                .employeeId(employee.getId())
                .fullName(employee.getUser().getFirstName() + " " + employee.getUser().getLastName())
                .position(employee.getPositionTitle())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : "")
                .baseSalary(employee.getSalaryBase())
                .salaryType(employee.getSalaryType().name())
                .totalLateMinutes(totalLate)
                .totalEarlyLeaveMinutes(totalEarly)
                .totalShortcomingMinutes(totalLate + totalEarly)
                .totalOvertimeMinutes(totalOver)
                .totalWorkedMinutes(totalWorked)
                .days(days)
                .finance(EmployeeExtendedDetailDto.FinanceStatsDto.builder()
                        .currentBalance(employee.getFinanceAccount().getBalance())
                        .totalTakenInPeriod(totalTaken)
                        .transactions(financeDtos)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SingleAttendanceRecordDto getSingleRecord(Long employeeId, LocalDate date) {
        Employee employee = validateEmployeeAccess(employeeId);

        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(employeeId, date)
                .orElse(null);

        return SingleAttendanceRecordDto.builder()
                .id(record != null ? record.getId() : null)
                .employeeId(employeeId)
                .employeeName(employee.getUser().getFirstName() + " " + employee.getUser().getLastName())
                .date(date)
                .status(record != null ? record.getStatus().name() : AttendanceStatus.ABSENT.name())
                .actualCheckIn(record != null && record.getCheckIn() != null
                        ? record.getCheckIn().atZone(ZONE_ID).toLocalTime() : null)
                .actualCheckOut(record != null && record.getCheckOut() != null
                        ? record.getCheckOut().atZone(ZONE_ID).toLocalTime() : null)
                .expectedStartTime(employee.getShiftStartTime())
                .expectedEndTime(employee.getShiftEndTime())
                .durationMinutes(record != null ? record.getDurationMinutes() : 0)
                .lateMinutes(record != null ? record.getLateMinutes() : 0)
                .earlyLeaveMinutes(record != null ? record.getEarlyLeaveMinutes() : 0)
                .overtimeMinutes(record != null ? record.getOvertimeMinutes() : 0)
                .totalShortcomingMinutes(record != null ? record.getLateMinutes() + record.getEarlyLeaveMinutes() : 0)
                .build();
    }


    private AttendanceGridResponseDto.GridDayDto buildDayCell(LocalDate date, AttendanceRecord record, LocalDate today) {
        var cell = new AttendanceGridResponseDto.GridDayDto();

        if (record != null) {
            cell.setRecordId(record.getId());
            cell.setStatus(record.getStatus().name());
            cell.setShortcomingMinutes(record.getLateMinutes() + record.getEarlyLeaveMinutes());
            cell.setOvertimeMinutes(record.getOvertimeMinutes());

            if (record.getCheckIn() != null)
                cell.setCheckIn(record.getCheckIn().atZone(ZONE_ID).format(TIME_FMT));
            if (record.getCheckOut() != null)
                cell.setCheckOut(record.getCheckOut().atZone(ZONE_ID).format(TIME_FMT));
        } else {
            cell.setStatus(date.isAfter(LocalDate.now(ZONE_ID)) ? "FUTURE" : "ABSENT");
            cell.setShortcomingMinutes(0);
            cell.setOvertimeMinutes(0);
        }
        return cell;
    }

    private EmployeeExtendedDetailDto.DailyAttendanceDetailDto buildDetailDay(LocalDate date, AttendanceRecord record) {
        var dto = new EmployeeExtendedDetailDto.DailyAttendanceDetailDto();
        dto.setDate(date);

        if (record != null) {
            dto.setStatus(record.getStatus().name());
            if (record.getCheckIn() != null)
                dto.setCheckIn(record.getCheckIn().atZone(ZONE_ID).format(TIME_FMT));
            if (record.getCheckOut() != null)
                dto.setCheckOut(record.getCheckOut().atZone(ZONE_ID).format(TIME_FMT));

            dto.setLateMinutes(record.getLateMinutes());
            dto.setEarlyLeaveMinutes(record.getEarlyLeaveMinutes());
            dto.setOvertimeMinutes(record.getOvertimeMinutes());
            dto.setWorkingMinutes(record.getDurationMinutes());
            dto.setShortcomingMinutes(record.getLateMinutes() + record.getEarlyLeaveMinutes());
        } else {
            dto.setStatus(date.isAfter(LocalDate.now(ZONE_ID)) ? "FUTURE" : "ABSENT");
        }
        return dto;
    }

    private Long resolveDepartmentId(Long requestedId) {
        User currentUser = getCurrentUser();

        if (isAdmin(currentUser)) {
            if (requestedId == null) {
                throw new InvalidRequestException("Department ID is required for Admin");
            }
            return requestedId;
        }

        Employee currentEmployee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Not an employee"));

        Department department = currentEmployee.getDepartment();

        if (department == null) {
            throw new AccessDeniedException("You are not assigned to any department");
        }

        User deptManager = department.getManager();
        boolean isManager = deptManager != null && deptManager.getId().equals(currentUser.getId());

        if (!isManager) {
            throw new AccessDeniedException("You are not the manager of this department");
        }

        if (requestedId != null && !department.getId().equals(requestedId)) {
            throw new AccessDeniedException("You can only request data for your own department (" + department.getName() + ")");
        }

        return department.getId();
    }

    private Employee validateEmployeeAccess(Long targetEmployeeId) {
        User currentUser = getCurrentUser();

        if (isAdmin(currentUser)) {
            return employeeRepository.findById(targetEmployeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        }

        Employee currentEmployee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("You do not have an employee profile. Access is denied."));

        Employee targetEmployee = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        boolean isMe = currentEmployee.getId().equals(targetEmployee.getId());

        boolean isHisManager = false;

        if (targetEmployee.getDepartment() != null && targetEmployee.getDepartment().getManager() != null) {
            Long deptManagerUserId = targetEmployee.getDepartment().getManager().getId();
            isHisManager = deptManagerUserId.equals(currentUser.getId());
        }

        if (isMe || isHisManager) {
            return targetEmployee;
        }

        throw new AccessDeniedException("Access denied. You are not this employee's manager.");
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_HR"));
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FMT) : "";
    }

    private int calculateWorkingDays(LocalDate from, LocalDate to) {
        return (int) ChronoUnit.DAYS.between(from, to) + 1;
    }
}