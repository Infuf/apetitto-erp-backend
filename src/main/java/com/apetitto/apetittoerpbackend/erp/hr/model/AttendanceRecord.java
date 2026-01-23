package com.apetitto.apetittoerpbackend.erp.hr.model;

import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_records")
@Getter
@Setter
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate date;

    private Instant checkIn;
    private Instant checkOut;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private Integer durationMinutes = 0;

    private Integer lateMinutes = 0;
    private Integer earlyLeaveMinutes = 0;
    private Integer totalLessMinutes = 0;

    private Integer earlyComeMinutes = 0;
    private Integer lateOutMinutes = 0;
    private Integer overtimeMinutes = 0;

    @CreationTimestamp
    private Instant created_at;
}