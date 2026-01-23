package com.apetitto.apetittoerpbackend.erp.hr.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.AccessDeniedException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.hr.dto.AttendanceUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.service.AttendanceService;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus.ABSENT;
import static com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus.PRESENT;
import static java.time.LocalDateTime.ofInstant;
import static java.time.temporal.ChronoUnit.MINUTES;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Tashkent");

    @Override
    @Transactional
    public void updateAttendance(AttendanceUpdateDto dto) {
        Employee targetEmployee = findEmployee(dto.getEmployeeId());
        validateUserAccess(targetEmployee);
        processAttendanceUpdate(targetEmployee, dto);
    }

    @Override
    @Transactional
    public void updateAttendanceSystem(AttendanceUpdateDto dto) {
        Employee targetEmployee = findEmployee(dto.getEmployeeId());
        processAttendanceUpdate(targetEmployee, dto);
    }

    private void processAttendanceUpdate(Employee employee, AttendanceUpdateDto dto) {
        validateDate(dto.getDate());

        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), dto.getDate())
                .orElse(new AttendanceRecord());

        if (record.getId() == null) {
            record.setEmployee(employee);
            record.setDate(dto.getDate());
            record.setStatus(PRESENT);
        }


        if (dto.getCheckIn() != null) {
            record.setCheckIn(dto.getCheckIn().atDate(dto.getDate()).atZone(ZONE_ID).toInstant());
            record.setStatus(PRESENT);
        }

        if (dto.getCheckOut() != null) {
            record.setCheckOut(dto.getCheckOut().atDate(dto.getDate()).atZone(ZONE_ID).toInstant());
            record.setStatus(PRESENT);
        }

        if (dto.getCheckIn() == null && dto.getCheckOut() == null) {
            record.setCheckIn(null);
            record.setCheckOut(null);
            record.setStatus(ABSENT);
        }

        recalculateMetrics(record, employee);
        attendanceRepository.save(record);
    }

    @Override
    @Transactional
    public void recalculateAllHistory() {

        var allRecords = attendanceRepository.findAll();
        log.info("Starting recalculation for {} records...", allRecords);
        for (var record : allRecords) {
            recalculateMetrics(record, record.getEmployee());
        }
        attendanceRepository.saveAll(allRecords);
        log.info("Recalculation completed.");

    }
    private void recalculateMetrics(AttendanceRecord record, Employee employee) {
        record.setDurationMinutes(0);
        record.setLateMinutes(0);
        record.setEarlyLeaveMinutes(0);
        record.setOvertimeMinutes(0);
        record.setEarlyComeMinutes(0);
        record.setLateOutMinutes(0);

        if (record.getCheckIn() == null && record.getCheckOut() == null) {
            return;
        }

        var actualIn = record.getCheckIn() != null ? ofInstant(record.getCheckIn(), ZONE_ID) : null;
        var actualOut = record.getCheckOut() != null ? ofInstant(record.getCheckOut(), ZONE_ID) : null;

        if (actualIn != null && actualOut != null && actualOut.isBefore(actualIn)) {
            actualOut = actualOut.plusDays(1);
        }

        var shiftStart = employee.getShiftStartTime();
        var shiftEnd = employee.getShiftEndTime();

        if (shiftStart == null || shiftEnd == null) {
            if (actualIn != null && actualOut != null) {
                long duration = MINUTES.between(actualIn, actualOut);
                record.setDurationMinutes((int) duration);
            }
            return;
        }

        var recordDate = record.getDate();
        var planIn = LocalDateTime.of(recordDate, shiftStart);
        var planOut = LocalDateTime.of(recordDate, shiftEnd);

        if (planOut.isBefore(planIn)) {
            planOut = planOut.plusDays(1);
        }

        if (actualIn != null) {

            if (actualIn.isBefore(planIn)) {
                record.setEarlyComeMinutes((int) MINUTES.between(actualIn, planIn));
            }

            if (actualIn.isAfter(planIn)) {
                record.setLateMinutes((int) MINUTES.between(planIn, actualIn));
            }
        }

        if (actualOut != null) {

            if (actualOut.isBefore(planOut)) {
                record.setEarlyLeaveMinutes((int) MINUTES.between(actualOut, planOut));
            }

            if (actualOut.isAfter(planOut)) {
                record.setLateOutMinutes((int) MINUTES.between(planOut, actualOut));
            }
        }

        if (actualIn != null && actualOut != null) {
            long duration = MINUTES.between(actualIn, actualOut);
            record.setDurationMinutes((int) Math.max(0, duration));
        }

        record.setOvertimeMinutes(record.getEarlyComeMinutes() + record.getLateOutMinutes());
        record.setTotalLessMinutes(record.getEarlyLeaveMinutes() + record.getLateMinutes());
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
    }

    private void validateUserAccess(Employee targetEmployee) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        boolean isGlobalAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_HR"));

        if (isGlobalAdmin) return;

        if (targetEmployee.getDepartment() == null) {
            throw new AccessDeniedException("The employee does not have a department, editing is restricted.");
        }

        User departmentManager = targetEmployee.getDepartment().getManager();
        if (departmentManager == null || !departmentManager.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the manager of the "
                    + targetEmployee.getDepartment().getName() + " department.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date.isAfter(LocalDate.now(ZONE_ID))) {
            throw new InvalidRequestException("Cannot set attendance in the future.");
        }
    }
}