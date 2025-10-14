package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferOrderItemRepository extends JpaRepository<TransferOrderItem, Long> {
}
