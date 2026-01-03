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
        }

        if (dto.getCheckOut() != null) {
            Instant checkOutInstant = dto.getCheckOut().atDate(dto.getDate()).atZone(ZONE_ID).toInstant();
            record.setCheckOut(checkOutInstant);
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

        LocalTime actualCheckIn = record.getCheckIn() != null ?
                record.getCheckIn().atZone(ZONE_ID).toLocalTime() : null;
        LocalTime actualCheckOut = record.getCheckOut() != null ?
                record.getCheckOut().atZone(ZONE_ID).toLocalTime() : null;

        LocalTime shiftStart = employee.getShiftStartTime();
        LocalTime shiftEnd = employee.getShiftEndTime();

        if (actualCheckIn != null && shiftStart != null) {
            if (actualCheckIn.isAfter(shiftStart)) {
                long late = Duration.between(shiftStart, actualCheckIn).toMinutes();
                record.setLateMinutes((int) late);
            }
        }

        if (actualCheckOut != null && shiftEnd != null) {
            if (actualCheckOut.isBefore(shiftEnd)) {
                long early = Duration.between(actualCheckOut, shiftEnd).toMinutes();
                record.setEarlyLeaveMinutes((int) early);
            }
        }

        if (actualCheckIn != null && actualCheckOut != null) {
            long duration = Duration.between(actualCheckIn, actualCheckOut).toMinutes();
            if (duration < 0) duration = 0;


            record.setDurationMinutes((int) duration);

            long normMinutes = 0;
            if (shiftStart != null && shiftEnd != null) {
                normMinutes = Duration.between(shiftStart, shiftEnd).toMinutes();
            }

            if (duration > normMinutes && normMinutes > 0) {
                record.setOvertimeMinutes((int) (duration - normMinutes));
            }
        }
    }
}