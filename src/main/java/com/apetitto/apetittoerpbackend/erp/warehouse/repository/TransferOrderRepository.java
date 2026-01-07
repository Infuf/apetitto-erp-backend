package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrder;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long>, JpaSpecificationExecutor<TransferOrder> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    @NonNull
    Optional<TransferOrder> findById(@NonNull Long id);

}
