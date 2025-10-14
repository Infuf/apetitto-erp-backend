package com.apetitto.apetittoerpbackend.erp.warehouse.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.mapper.StockItemMapper;
import com.apetitto.apetittoerpbackend.erp.warehouse.mapper.WarehouseMapper;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.*;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.StockItemRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.StockMovementRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.WarehouseRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.specification.StockItemSpecifications;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.ProductService;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.apetitto.apetittoerpbackend.erp.warehouse.repository.specification.StockItemSpecifications.*;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final StockItemRepository stockItemRepository;
    private final StockItemMapper stockItemMapper;
    private final ProductService productService;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public WarehouseDto createWarehouse(WarehouseDto warehouseDto) {
        if (warehouseDto.getId() != null) {
            throw new InvalidRequestException("While creating warehouse ID must be NULL");
        }
        var warehouse = warehouseMapper.toEntity(warehouseDto);
        var savedWarehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toDto(savedWarehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDto> getAllWarehouses() {
        return warehouseMapper.toDtoList(warehouseRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDto getWarehouseById(Long id) {
        var warehouse = findWarehouseEntityById(id);
        return warehouseMapper.toDto(warehouse);
    }

    @Override
    @Transactional
    public WarehouseDto updateWarehouse(WarehouseDto warehouseDto) {
        if (warehouseDto.getId() == null) {
            throw new InvalidRequestException("Whare house ID must exists");
        }
        var existingWarehouse = findWarehouseEntityById(warehouseDto.getId());
        warehouseMapper.updateEntityFromDto(warehouseDto, existingWarehouse);
        var updatedWarehouse = warehouseRepository.save(existingWarehouse);
        return warehouseMapper.toDto(updatedWarehouse);
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Warehouse with ID " + id + " not found");
        }
        warehouseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Warehouse findWarehouseEntityById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse with ID " + id + " not found"));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<StockItemDto> getStockByWarehouse(Long warehouseId, String searchQuery, Long categoryId,
                                                  boolean showZeroQuantity, Pageable pageable) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse with ID " + warehouseId + " not found");
        }
        var spec = byWarehouse(warehouseId)
                .and(byCategory(categoryId))
                .and(bySearchQuery(searchQuery))
                .and(excludeZeroQuantity(showZeroQuantity))
                .and(fetchProduct());

        Page<StockItem> itemsPage = stockItemRepository.findAll(spec, pageable);

        return itemsPage.map(stockItemMapper::toDto);
    }

    @Override
    @Transactional
    public void processStockMovement(StockMovementRequestDto requestDto) {
        var warehouse = findWarehouseEntityById(requestDto.getWarehouseId());
        var movementType = requestDto.getMovementType();

        var movement = createMovementHeader(requestDto, warehouse);

        switch (movementType) {
            case INBOUND, TRANSFER_IN -> processInboundOrTransfer(movement, requestDto.getItems());
            case OUTBOUND, TRANSFER_OUT -> processOutboundOrTransfer(movement, requestDto.getItems());
            case ADJUSTMENT -> processAdjustment(movement, requestDto.getItems());
            default -> throw new InvalidRequestException("Unsupported movement type: " + movementType);
        }

        stockMovementRepository.save(movement);
    }

    private void processOutboundOrTransfer(StockMovement movement, List<StockMovementRequestDto.Item> items) {

        List<StockItem> outboundItems = new ArrayList<>();
        for (var itemDto : items) {
            Product product = productService.findProductEntityById(itemDto.getProductId());
            long quantityInBase = product.getUnit().toBaseUnit(itemDto.getQuantity());

            StockItem stockItem = findOrCreateStockItem(movement.getWarehouse(), product);

            long newQuantity = stockItem.getQuantity() - quantityInBase;
            if (newQuantity < 0) {
                throw new InvalidRequestException("There are not enough items '" + product.getName() + "' in stock.");
            }
            stockItem.setQuantity(newQuantity);
            outboundItems.add(stockItem);

            movement.getItems().add(createMovementItem(movement, product, quantityInBase, null));
        }
        stockItemRepository.saveAll(outboundItems);
    }

    private void processInboundOrTransfer(StockMovement movement, List<StockMovementRequestDto.Item> items) {
        List<StockItem> inboundItems = new ArrayList<>();

        for (var itemDto : items) {
            if (itemDto.getCostPrice() == null || itemDto.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidRequestException("For admission, the purchase price (costPrice) is mandatory and cannot be negative.");
            }

            Product product = productService.findProductEntityById(itemDto.getProductId());
            long quantityInBase = product.getUnit().toBaseUnit(itemDto.getQuantity());

            StockItem stockItem = findOrCreateStockItem(movement.getWarehouse(), product);

            updateAverageCostOnInbound(stockItem, quantityInBase, itemDto.getCostPrice());

            stockItem.setQuantity(stockItem.getQuantity() + quantityInBase);
            inboundItems.add(stockItem);
            movement.getItems().add(createMovementItem(movement, product, quantityInBase, itemDto.getCostPrice()));
        }
        stockItemRepository.saveAll(inboundItems);
    }

    private void processAdjustment(StockMovement movement, List<StockMovementRequestDto.Item> items) {
        List<StockItem> adjustmentItems = new ArrayList<>();
        for (var itemDto : items) {
            Product product = productService.findProductEntityById(itemDto.getProductId());
            long quantityChangeInBase = product.getUnit().toBaseUnit(itemDto.getQuantity());

            StockItem stockItem = findOrCreateStockItem(movement.getWarehouse(), product);

            long newQuantity = stockItem.getQuantity() + quantityChangeInBase;
            if (newQuantity < 0) {
                throw new InvalidRequestException("The total quantity of product '" + product.getName() + "' cannot be negative.");
            }
            stockItem.setQuantity(newQuantity);

            adjustmentItems.add(stockItem);

            movement.getItems().add(createMovementItem(movement, product, quantityChangeInBase, null));
        }
        stockItemRepository.saveAll(adjustmentItems);
    }

    private StockMovement createMovementHeader(StockMovementRequestDto requestDto, Warehouse warehouse) {
        var movement = new StockMovement();
        movement.setWarehouse(warehouse);
        movement.setMovementType(requestDto.getMovementType());
        movement.setComment(requestDto.getComment());
        movement.setItems(new ArrayList<>());
        return movement;
    }

    private StockItem findOrCreateStockItem(Warehouse warehouse, Product product) {
        return stockItemRepository.findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElseGet(() -> {
                    var newItem = new StockItem();
                    newItem.setProduct(product);
                    newItem.setWarehouse(warehouse);
                    newItem.setQuantity(0L);
                    newItem.setAverageCost(BigDecimal.ZERO);
                    return newItem;
                });
    }

    private StockMovementItem createMovementItem(StockMovement movement, Product product,
                                                 long quantity, BigDecimal costPrice) {
        var item = new StockMovementItem();
        item.setMovement(movement);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setCostPrice(costPrice);
        return item;
    }

    private void updateAverageCostOnInbound(StockItem stockItem, long quantityChange, BigDecimal costPrice) {
        var product = stockItem.getProduct();
        long currentQuantity = stockItem.getQuantity();
        var currentAvgCost = stockItem.getAverageCost();

        var costPerBaseUnit = costPrice.divide(BigDecimal.valueOf(product.getUnit().getConversionFactor()),
                4, RoundingMode.HALF_UP);

        var currentTotalCost = currentAvgCost.multiply(BigDecimal.valueOf(currentQuantity));
        var inboundTotalCost = costPerBaseUnit.multiply(BigDecimal.valueOf(quantityChange));

        long newTotalQuantity = currentQuantity + quantityChange;
        if (newTotalQuantity == 0) {
            stockItem.setAverageCost(BigDecimal.ZERO);
        } else {
            BigDecimal newTotalCost = currentTotalCost.add(inboundTotalCost);
            BigDecimal newAverageCost = newTotalCost.divide(BigDecimal.valueOf(newTotalQuantity),
                    4, RoundingMode.HALF_UP);
            stockItem.setAverageCost(newAverageCost);
        }
    }
}