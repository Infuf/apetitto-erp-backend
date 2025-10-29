package com.apetitto.apetittoerpbackend.erp.warehouse.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.mapper.TransferOrderMapper;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrder;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrderItem;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.StockItemRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.TransferOrderRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.ProductService;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.TransferService;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferOrderRepository transferOrderRepository;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final StockItemRepository stockItemRepository;
    private final TransferOrderMapper transferOrderMapper;

    @Override
    @Transactional
    public TransferOrderDto createTransfer(TransferOrderRequestDto requestDto) {
        if (requestDto.getSourceWarehouseId().equals(requestDto.getDestinationWarehouseId())) {
            throw new InvalidRequestException("The sending warehouse and the receiving warehouse cannot be the same.");
        }

        var sourceWarehouse = warehouseService.findWarehouseEntityById(requestDto.getSourceWarehouseId());
        var destinationWarehouse = warehouseService.findWarehouseEntityById(requestDto.getDestinationWarehouseId());

        var transferOrder = new TransferOrder();
        transferOrder.setSourceWarehouse(sourceWarehouse);
        transferOrder.setDestinationWarehouse(destinationWarehouse);
        transferOrder.setStatus(TransferStatus.PENDING);
        transferOrder.setItems(new ArrayList<>());

        for (TransferOrderRequestDto.Item itemDto : requestDto.getItems()) {

            var product = productService.findProductEntityById(itemDto.getProductId());

            var orderItem = new TransferOrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setTransferOrder(transferOrder);
            orderItem.setCostAtTransfer(BigDecimal.ZERO);
            transferOrder.getItems().add(orderItem);
        }

        var savedOrder = transferOrderRepository.save(transferOrder);
        return transferOrderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferOrderDto> getTransfers(TransferStatus status, Long destinationWarehouseId, Pageable pageable) {
        Page<TransferOrder> page;
        if (status != null && destinationWarehouseId != null) {
            page = transferOrderRepository.findByDestinationWarehouseIdAndStatus(destinationWarehouseId, status, pageable);
        } else if (status != null) {
            page = transferOrderRepository.findByStatus(status, pageable);
        } else {
            page = transferOrderRepository.findAll(pageable);
        }
        return page.map(transferOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferOrderDto getTransferById(Long id) {
        return transferOrderRepository.findById(id)
                .map(transferOrderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("The move order with ID + id +  was not found."));
    }

    @Override
    @Transactional
    public TransferOrderDto shipTransfer(Long id) {
        TransferOrder order = findOrderById(id);

        if (order.getStatus() != TransferStatus.PENDING) {
            throw new InvalidRequestException("You can only send orders with the PENDING status. Current status:" + order.getStatus());
        }

        var movementRequest = new StockMovementRequestDto();
        movementRequest.setWarehouseId(order.getSourceWarehouse().getId());
        movementRequest.setMovementType(MovementType.TRANSFER_OUT);
        movementRequest.setComment("Dispatch for transfer order #" + order.getId());

        List<StockMovementRequestDto.Item> movementItems = new ArrayList<>();
        for (TransferOrderItem orderItem : order.getItems()) {
            BigDecimal costPerUserUnit = getCurrentAverageCost(orderItem, order.getSourceWarehouse().getId());
            orderItem.setCostAtTransfer(costPerUserUnit);

            StockMovementRequestDto.Item dtoItem = new StockMovementRequestDto.Item();
            dtoItem.setProductId(orderItem.getProduct().getId());
            dtoItem.setQuantity(orderItem.getQuantity());
            movementItems.add(dtoItem);
        }
        movementRequest.setItems(movementItems);

        warehouseService.processStockMovement(movementRequest);

        order.setStatus(TransferStatus.SHIPPED);
        order.setShippedAt(Instant.now());
        TransferOrder updatedOrder = transferOrderRepository.save(order);

        return transferOrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public TransferOrderDto receiveTransfer(Long id) {
        TransferOrder order = findOrderById(id);

        if (order.getStatus() != TransferStatus.SHIPPED) {
            throw new InvalidRequestException("Only orders with the SHIPPED status can be accepted. Current status:" + order.getStatus());
        }

        var movementRequest = new StockMovementRequestDto();
        movementRequest.setWarehouseId(order.getDestinationWarehouse().getId());
        movementRequest.setMovementType(MovementType.TRANSFER_IN);
        movementRequest.setComment("Acceptance for transfer order No." + order.getId());

        List<StockMovementRequestDto.Item> movementItems = order.getItems().stream().map(orderItem -> {
            StockMovementRequestDto.Item dtoItem = new StockMovementRequestDto.Item();
            dtoItem.setProductId(orderItem.getProduct().getId());
            dtoItem.setQuantity(orderItem.getQuantity());

            dtoItem.setCostPrice(orderItem.getCostAtTransfer());
            return dtoItem;
        }).collect(Collectors.toList());
        movementRequest.setItems(movementItems);

        warehouseService.processStockMovement(movementRequest);

        order.setStatus(TransferStatus.RECEIVED);
        order.setReceivedAt(Instant.now());
        TransferOrder updatedOrder = transferOrderRepository.save(order);

        return transferOrderMapper.toDto(updatedOrder);
    }


    private TransferOrder findOrderById(Long id) {
        return transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("The move order with ID + id + was not found."));
    }

    private BigDecimal getCurrentAverageCost(TransferOrderItem orderItem, Long sourceWarehouseId) {
        var stockItem = stockItemRepository.findByWarehouseIdAndProductId(sourceWarehouseId,
                        orderItem.getProduct().getId())
                .orElseThrow(() -> new InvalidRequestException("No balance found for item '" + orderItem.getProduct()
                        .getName() + "' at the sending warehouse."));

        if (stockItem.getQuantity().compareTo(orderItem.getQuantity()) < 0) {
            throw new InvalidRequestException("Not enough goods'" + orderItem.getProduct().getName() + "' for sending.");
        }

        return stockItem.getAverageCost();
    }
}