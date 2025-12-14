package com.apetitto.apetittoerpbackend.erp.finance.model;

import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_account")
@Getter
@Setter
public class FinanceAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanceAccountType type;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}