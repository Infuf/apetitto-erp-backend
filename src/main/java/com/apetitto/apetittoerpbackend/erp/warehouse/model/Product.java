package com.apetitto.apetittoerpbackend.erp.warehouse.model;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.UnitType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String productCode;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private UnitType unit;

    @Column(unique = true)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(precision = 20, scale = 2)
    private BigDecimal sellingPrice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
