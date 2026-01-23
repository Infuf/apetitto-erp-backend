package com.apetitto.apetittoerpbackend.erp.hr.listener;

import com.apetitto.apetittoerpbackend.erp.hr.event.NewAttendanceLogEvent;
import com.apetitto.apetittoerpbackend.erp.hr.service.AttendanceProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceEventListener {

    private final AttendanceProcessingService processingService;

    @Async
    @EventListener
    public void handleNewLogs(NewAttendanceLogEvent event) {
        log.debug("Processing {} new logs in background", event.getLogIds().size());
        processingService.processLogs();
    }
}