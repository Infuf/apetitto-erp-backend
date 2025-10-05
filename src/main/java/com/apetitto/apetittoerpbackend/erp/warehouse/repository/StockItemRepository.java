package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {


    Optional<StockItem> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

}