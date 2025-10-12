package com.apetitto.apetittoerpbackend.erp.warehouse.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.mapper.WarehouseMapper;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Warehouse;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.WarehouseRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

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
}