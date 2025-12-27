package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockMovement;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<StockMovement> findByWarehouseIdAndMovementType(Long warehouseId, MovementType movementType, Pageable pageable);

    Page<StockMovement> findByWarehouseIdAndMovementTimeBetween(Long warehouseId, Instant dateFrom, Instant dateTo,
                                                                Pageable pageable);

    @Query("""
                SELECT sm
                FROM StockMovement sm
                JOIN FETCH sm.items i
                JOIN FETCH i.product p
                JOIN FETCH sm.warehouse w
                WHERE sm.movementType = 'TRANSFER_IN'
                AND (:warehouseIds IS NULL OR w.id IN :warehouseIds)
                AND sm.movementTime BETWEEN :dateFrom AND :dateTo
            """)
    List<StockMovement> findIncomingMovements(
            @Param("warehouseIds") List<Long> warehouseIds,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo
    );
}
