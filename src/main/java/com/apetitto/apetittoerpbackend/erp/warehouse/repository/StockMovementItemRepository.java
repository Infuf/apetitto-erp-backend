package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockMovementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementItemRepository extends JpaRepository<StockMovementItem, Long> {

}
