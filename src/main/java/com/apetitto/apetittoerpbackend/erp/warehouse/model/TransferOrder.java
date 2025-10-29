package com.apetitto.apetittoerpbackend.erp.warehouse.model;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class TransferOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_warehouse_id", nullable = false)
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @Column
    private Instant shippedAt;

    @Column
    private Instant receivedAt;

    @OneToMany(mappedBy = "transferOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferOrderItem> items = new ArrayList<>();
}
