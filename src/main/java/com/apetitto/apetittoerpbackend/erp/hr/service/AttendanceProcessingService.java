package com.apetitto.apetittoerpbackend.erp.hr.service;

import com.apetitto.apetittoerpbackend.erp.hr.dto.AttendanceUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.HrDeviceLog;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.HrDeviceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceProcessingService {

    private final HrDeviceLogRepository deviceLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Tashkent");

    @Transactional
    public void processLogs(List<Long> logIds) {
        List<HrDeviceLog> logs = deviceLogRepository.findAllById(logIds);

        for (HrDeviceLog logItem : logs) {
            try {
                processSingleLog(logItem);
                logItem.setIsProcessed(true);
            } catch (Exception e) {
                log.error("Logic error processing log {}: {}", logItem.getId(), e.getMessage());
                logItem.setErrorMessage(e.getMessage());
                logItem.setIsProcessed(true);
            }
        }
        deviceLogRepository.saveAll(logs);
    }

    private void processSingleLog(HrDeviceLog logItem) {
        Employee employee = employeeRepository.findByTerminalId(logItem.getUserPin())
                .orElseThrow(() -> new RuntimeException("Сотрудник с PIN " + logItem.getUserPin() + " не найден"));

        LocalDateTime eventDateTime = logItem.getEventTime();
        LocalDate shiftDate = determineShiftDate(eventDateTime, employee);

        AttendanceRecord existingRecord = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), shiftDate)
                .orElse(null);

        AttendanceUpdateDto updateDto = new AttendanceUpdateDto();
        updateDto.setEmployeeId(employee.getId());
        updateDto.setDate(shiftDate);

        boolean needUpdate = false;
        LocalTime eventTime = eventDateTime.toLocalTime();


        LocalTime currentIn = (existingRecord != null && existingRecord.getCheckIn() != null)
                ? existingRecord.getCheckIn().atZone(ZONE_ID).toLocalTime() : null;

        LocalTime currentOut = (existingRecord != null && existingRecord.getCheckOut() != null)
                ? existingRecord.getCheckOut().atZone(ZONE_ID).toLocalTime() : null;

        if (currentIn == null || eventTime.isBefore(currentIn)) {
            updateDto.setCheckIn(eventTime);
            needUpdate = true;
        }

        if (currentOut == null || eventTime.isAfter(currentOut)) {
            updateDto.setCheckOut(eventTime);
            needUpdate = true;
        }

        if (needUpdate) {
            attendanceService.updateAttendance(updateDto);
        }
    }

    private LocalDate determineShiftDate(LocalDateTime eventDateTime, Employee employee) {
        LocalTime time = eventDateTime.toLocalTime();
        LocalDate date = eventDateTime.toLocalDate();

        if (employee.getShiftStartTime() != null) {
            LocalTime cutoff = employee.getShiftStartTime().minusHours(4);
            if (time.isBefore(cutoff)) {
                return date.minusDays(1);
            }
        }
        return date;
    }
}