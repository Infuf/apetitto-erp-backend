package com.apetitto.apetittoerpbackend.erp.warehouse.service;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface TransferService {

    TransferOrderDto createTransfer(TransferOrderRequestDto requestDto);

    Page<TransferOrderDto> getTransfers(TransferStatus status, Long destinationWarehouseId, Instant dateFrom, Instant dateTo, Pageable pageable);

    TransferOrderDto getTransferById(Long id);

    TransferOrderDto shipTransfer(Long id);

    TransferOrderDto receiveTransfer(Long id);
}