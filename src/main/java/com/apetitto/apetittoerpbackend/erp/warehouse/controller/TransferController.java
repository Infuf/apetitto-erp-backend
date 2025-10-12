package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.warehouse.controller.api.TransferApi;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransferController implements TransferApi {

    private final TransferService transferService;

    @Override
    public ResponseEntity<TransferOrderDto> createTransfer(TransferOrderRequestDto requestDto) {
        TransferOrderDto createdTransfer = transferService.createTransfer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransfer);
    }

    @Override
    public ResponseEntity<Page<TransferOrderDto>> getTransfers(TransferStatus status, Long destinationWarehouseId, Pageable pageable) {
        Page<TransferOrderDto> transfers = transferService.getTransfers(status, destinationWarehouseId, pageable);
        return ResponseEntity.ok(transfers);
    }

    @Override
    public ResponseEntity<TransferOrderDto> getTransferById(Long id) {
        TransferOrderDto transfer = transferService.getTransferById(id);
        return ResponseEntity.ok(transfer);
    }

    @Override
    public ResponseEntity<TransferOrderDto> shipTransfer(Long id) {
        TransferOrderDto shippedTransfer = transferService.shipTransfer(id);
        return ResponseEntity.ok(shippedTransfer);
    }

    @Override
    public ResponseEntity<TransferOrderDto> receiveTransfer(Long id) {
        TransferOrderDto receivedTransfer = transferService.receiveTransfer(id);
        return ResponseEntity.ok(receivedTransfer);
    }
}