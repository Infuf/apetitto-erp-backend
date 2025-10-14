package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long>, JpaSpecificationExecutor<StockItem> {

    Optional<StockItem> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    Page<StockItem> findAllByWarehouseId(Long warehouseId, Pageable pageable);

}