package com.apetitto.apetittoerpbackend.erp.hr.model;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.PayrollStatus;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_accruals")
@Getter
@Setter
public class PayrollAccrual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalaryType paymentType;

    @Column(nullable = false)
    private BigDecimal baseSalary;

    @Column(nullable = false)
    private BigDecimal baseWorkHours;

    private Integer baseDaysOff;

    @Column(nullable = false)
    private BigDecimal calculatedDayRate;

    @Column(nullable = false)
    private BigDecimal calculatedHourRate;

    private Integer daysWorked = 0;
    private BigDecimal totalWorkedHours = BigDecimal.ZERO;

    private Integer lateMinutes = 0;
    private Integer earlyLeaveMinutes = 0;
    private Integer totalUndertimeMinutes = 0;

    private Integer earlyComeMinutes = 0;
    private Integer lateOutMinutes = 0;
    private Integer totalOvertimeMinutes = 0;
    private Integer totalOvertimeEffectiveMinutes = 0;

    @Column(nullable = false)
    private BigDecimal grossSalaryAmount;

    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    private BigDecimal overtimeBonusAmount = BigDecimal.ZERO;
    private BigDecimal manualBonusAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal finalAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_transaction_id")
    private FinanceTransaction financeTransaction;

    @Enumerated(value = EnumType.STRING)
    private PayrollStatus status;

    private String calculationNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private Instant createdAt;
}