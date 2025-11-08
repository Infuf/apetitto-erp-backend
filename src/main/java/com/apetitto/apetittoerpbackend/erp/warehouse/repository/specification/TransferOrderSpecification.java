package com.apetitto.apetittoerpbackend.erp.warehouse.repository.specification;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrder;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class TransferOrderSpecification {

    public static Specification<TransferOrder> hasStatus(TransferStatus status) {
        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<TransferOrder> hasDestinationWarehouseId(Long warehouseId) {
        return (root, query, cb) -> {

            if (warehouseId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("destinationWarehouse").get("id"), warehouseId);
        };
    }

    public static Specification<TransferOrder> createdBetween(Instant dateFrom, Instant dateTo) {

        return (root, query, cb) -> {

            if (dateFrom == null || dateTo == null) {
                return cb.conjunction();
            }
            return cb.between(root.get("createdAt"), dateFrom, dateTo);
        };
    }
}