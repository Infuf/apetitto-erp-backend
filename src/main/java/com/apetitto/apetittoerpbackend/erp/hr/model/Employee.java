package com.apetitto.apetittoerpbackend.erp.hr.model;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_account_id", nullable = false)
    private FinanceAccount financeAccount;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(nullable = false)
    private BigDecimal salaryBase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalaryType salaryType;

    private BigDecimal workHoursPerDay;
    private Integer daysOffPerMonth = 2;

    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;

    @CreationTimestamp
    private Instant hiredAt;

    @UpdateTimestamp
    private Instant updatedAt;
}