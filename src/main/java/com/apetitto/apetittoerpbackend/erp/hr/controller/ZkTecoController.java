package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.hr.event.NewAttendanceLogEvent;
import com.apetitto.apetittoerpbackend.erp.hr.model.HrDeviceLog;
import com.apetitto.apetittoerpbackend.erp.hr.repository.HrDeviceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/iclock")
@RequiredArgsConstructor
@Slf4j
public class ZkTecoController {

    private final HrDeviceLogRepository deviceLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final DateTimeFormatter ZK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/cdata")
    public ResponseEntity<String> handshake(
            @RequestParam(name = "SN", required = false) String sn
    ) {
        return ResponseEntity.ok("GET OPTION FROM: " + sn + "\nOK");
    }

    @GetMapping("/getrequest")
    public ResponseEntity<String> getRequest(
            @RequestParam(name = "SN", required = false) String sn
    ) {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/cdata")
    public ResponseEntity<String> receiveData(
            @RequestParam(name = "SN") String sn,
            @RequestParam(name = "table") String table,
            @RequestBody String body
    ) {
        if ("ATTLOG".equals(table)) {
            try {
                List<HrDeviceLog> logs = parseAndSaveLogs(sn, body);

                if (!logs.isEmpty()) {
                    List<Long> ids = logs.stream().map(HrDeviceLog::getId).toList();
                    eventPublisher.publishEvent(new NewAttendanceLogEvent(this, ids));
                }

                return ResponseEntity.ok("OK");
            } catch (Exception e) {
                log.error("Failed to parse logs from {}: {}", sn, e.getMessage());
                return ResponseEntity.ok("OK");
            }
        }
        return ResponseEntity.ok("OK");
    }

    private List<HrDeviceLog> parseAndSaveLogs(String sn, String body) {
        List<HrDeviceLog> result = new ArrayList<>();
        String[] lines = body.split("\\r?\\n");

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] parts = line.split("\\s+");

            if (parts.length >= 2) {
                try {
                    HrDeviceLog logItem = new HrDeviceLog();
                    logItem.setDeviceSn(sn);
                    logItem.setUserPin(Long.parseLong(parts[0]));

                    String dateStr = parts[1] + " " + parts[2];
                    logItem.setEventTime(LocalDateTime.parse(dateStr, ZK_FORMATTER));

                    logItem.setEventType(Integer.parseInt(parts[3]));
                    logItem.setRawData(line);
                    logItem.setIsProcessed(false);

                    result.add(logItem);
                } catch (Exception e) {
                    log.warn("Skipping bad line from {}: {}", sn, line);
                }
            }
        }
        return deviceLogRepository.saveAll(result);
    }
}