package com.apetitto.apetittoerpbackend.erp.hr.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_device_log")
@Getter
@Setter
public class HrDeviceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceSn;

    @Column(nullable = false)
    private Long userPin;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    private Integer eventType;

    private String rawData;

    @Column(nullable = false)
    private Boolean isProcessed = false;

    private String errorMessage;

    @CreationTimestamp
    private Instant createdAt;
}