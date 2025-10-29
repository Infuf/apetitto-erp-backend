package com.apetitto.apetittoerpbackend.erp.warehouse.model;


import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type")
    private MovementType movementType;

    @CreationTimestamp
    @Column(name = "movement_time")
    private Instant movementTime;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "comment")
    private String comment;

    @OneToMany(mappedBy = "movement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockMovementItem> items = new ArrayList<>();
}