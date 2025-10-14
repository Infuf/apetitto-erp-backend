package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrder;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {

    Page<TransferOrder> findByStatus(TransferStatus status, Pageable pageable);

    Page<TransferOrder> findByDestinationWarehouseIdAndStatus(Long warehouseId, TransferStatus status, Pageable pageable);
}
