package com.apetitto.apetittoerpbackend.erp.hr.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.AccessDeniedException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.hr.dto.AttendanceUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.service.AttendanceService;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

import static com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus.ABSENT;
import static com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus.PRESENT;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Tashkent");

    @Override
    @Transactional
    public void updateAttendance(AttendanceUpdateDto dto) {
        Employee targetEmployee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));

        validateUserAccess(targetEmployee);

        validateDate(dto.getDate());

        // TODO: Проверка на закрытый период (Финансы).
        // Если зарплата за этот месяц (или этот день) уже начислена (есть транзакция SALARY_ACCRUAL или статус PAID),
        // то редактирование должно быть запрещено.
        // Пример: if (payrollService.isPeriodClosed(targetEmployee.getId(), dto.getDate())) throw ...

        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(targetEmployee.getId(), dto.getDate())
                .orElse(new AttendanceRecord());

        if (record.getId() == null) {
            record.setEmployee(targetEmployee);
            record.setDate(dto.getDate());
            record.setStatus(AttendanceStatus.PRESENT);
        }

        if (dto.getCheckIn() != null) {
            Instant checkInInstant = dto.getCheckIn().atDate(dto.getDate()).atZone(ZONE_ID).toInstant();
            record.setCheckIn(checkInInstant);
            record.setStatus(PRESENT);
        }

        if (dto.getCheckOut() != null) {
            Instant checkOutInstant = dto.getCheckOut().atDate(dto.getDate()).atZone(ZONE_ID).toInstant();
            record.setCheckOut(checkOutInstant);
            record.setStatus(PRESENT);
        }

        if (dto.getCheckIn() == null && null == dto.getCheckOut()) {
            record.setCheckIn(null);
            record.setCheckOut(null);
            record.setStatus(ABSENT);
        }

        recalculateMetrics(record, targetEmployee);

        attendanceRepository.save(record);
    }

    @Override
    @Transactional
    public void updateAttendanceSystem(AttendanceUpdateDto dto) {
        Employee targetEmployee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));

        validateDate(dto.getDate());

        // TODO: Проверка на закрытый период (Финансы).
        // Если зарплата за этот месяц (или этот день) уже начислена (есть транзакция SALARY_ACCRUAL или статус PAID),
        // то редактирование должно быть запрещено.
        // Пример: if (payrollService.isPeriodClosed(targetEmployee.getId(), dto.getDate())) throw ...

        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(targetEmployee.getId(), dto.getDate())
                .orElse(new AttendanceRecord());

        if (record.getId() == null) {
            record.setEmployee(targetEmployee);
            record.setDate(dto.getDate());
            record.setStatus(AttendanceStatus.PRESENT);
        }

        if (dto.getCheckIn() != null) {
            Instant checkInInstant = dto.getCheckIn().atDate(dto.getDate()).atZone(ZONE_ID).toInstant();
            record.setCheckIn(checkInInstant);
            record.setStatus(PRESENT);
        }

        if (dto.getCheckOut() != null) {
            Instant checkOutInstant = dto.getCheckOut().atDate(dto.getDate()).atZone(ZONE_ID).toInstant();
            record.setCheckOut(checkOutInstant);
            record.setStatus(PRESENT);
        }

        if (dto.getCheckIn() == null && null == dto.getCheckOut()) {
            record.setCheckIn(null);
            record.setCheckOut(null);
            record.setStatus(ABSENT);
        }

        recalculateMetrics(record, targetEmployee);

        attendanceRepository.save(record);
    }

    private void validateUserAccess(Employee targetEmployee) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        boolean isGlobalAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_HR"));

        if (isGlobalAdmin) {
            return;
        }

        if (targetEmployee.getDepartment() == null) {
            throw new AccessDeniedException("The employee does not have a department, editing is restricted.");
        }

        User departmentManager = targetEmployee.getDepartment().getManager();
        if (departmentManager == null || !departmentManager.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the manager of the "
                    + targetEmployee.getDepartment().getName() + " department. Access denied.");
        }
    }

    private void validateDate(LocalDate date) {
        LocalDate today = LocalDate.now(ZONE_ID);
        if (date.isAfter(today)) {
            throw new InvalidRequestException("You cannot make marks for future dates.(" + date + ").");
        }
    }

    private void recalculateMetrics(AttendanceRecord record, Employee employee) {
        record.setLateMinutes(0);
        record.setEarlyLeaveMinutes(0);
        record.setOvertimeMinutes(0);
        record.setDurationMinutes(0);

        if (record.getCheckIn() == null || record.getCheckOut() == null) {
            return;
        }

        LocalTime actualCheckIn =
                record.getCheckIn().atZone(ZONE_ID).toLocalTime();
        LocalTime actualCheckOut =
                record.getCheckOut().atZone(ZONE_ID).toLocalTime();

        LocalTime shiftStart = employee.getShiftStartTime();
        LocalTime shiftEnd = employee.getShiftEndTime();

        if (shiftStart == null || shiftEnd == null) {
            return;
        }

        boolean nightShift = shiftEnd.isBefore(shiftStart);


        long durationMinutes;
        if (!nightShift) {
            durationMinutes = Duration.between(actualCheckIn, actualCheckOut).toMinutes();
        } else {
            durationMinutes = Duration.between(
                    actualCheckIn,
                    actualCheckOut.isAfter(actualCheckIn)
                            ? actualCheckOut
                            : actualCheckOut.plusHours(24)
            ).toMinutes();
        }

        record.setDurationMinutes((int) Math.max(durationMinutes, 0));

        if (!nightShift) {
            if (actualCheckIn.isAfter(shiftStart)) {
                record.setLateMinutes(
                        (int) Duration.between(shiftStart, actualCheckIn).toMinutes()
                );
            }
        } else {
            if (actualCheckIn.isAfter(shiftStart)) {
                record.setLateMinutes(
                        (int) Duration.between(shiftStart, actualCheckIn).toMinutes()
                );
            }
        }

        if (!nightShift) {
            if (actualCheckOut.isBefore(shiftEnd)) {
                record.setEarlyLeaveMinutes(
                        (int) Duration.between(actualCheckOut, shiftEnd).toMinutes()
                );
            }
        } else {
            // ночная смена
            if (actualCheckOut.isBefore(shiftEnd)) {
                record.setEarlyLeaveMinutes(
                        (int) Duration.between(actualCheckOut, shiftEnd).toMinutes()
                );
            }
        }

        long overtime = 0;

        if (actualCheckIn.isBefore(shiftStart)) {
            overtime += Duration.between(actualCheckIn, shiftStart).toMinutes();
        }

        if (actualCheckOut.isAfter(shiftEnd)) {
            overtime += Duration.between(shiftEnd, actualCheckOut).toMinutes();
        }

        record.setOvertimeMinutes((int) Math.max(overtime, 0));
    }
}