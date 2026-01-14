package com.apetitto.apetittoerpbackend.erp.hr.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.List;

@Getter
public class NewAttendanceLogEvent extends ApplicationEvent {
    private final List<Long> logIds;

    public NewAttendanceLogEvent(Object source, List<Long> logIds) {
        super(source);
        this.logIds = logIds;
    }
}