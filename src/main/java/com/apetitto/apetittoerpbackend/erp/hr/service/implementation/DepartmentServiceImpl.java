package com.apetitto.apetittoerpbackend.erp.hr.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.hr.dto.DepartmentDto;
import com.apetitto.apetittoerpbackend.erp.hr.mapper.DepartmentMapper;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import com.apetitto.apetittoerpbackend.erp.hr.repository.DepartmentRepository;
import com.apetitto.apetittoerpbackend.erp.hr.service.DepartmentService;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new InvalidRequestException("A department with the name " + dto.getName() + " already exists.");
        }

        Department department = departmentMapper.toEntity(dto);

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User (manager) not found with ID: " + dto.getManagerId()));
            department.setManager(manager);
        }

        return departmentMapper.toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new InvalidRequestException("A department with the name '" + dto.getName() + "' already exists.");
        }

        departmentMapper.updateEntity(dto, department);

        if (dto.getManagerId() != null) {
            if (department.getManager() == null || !department.getManager().getId().equals(dto.getManagerId())) {
                User manager = userRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new ResourceNotFoundException("User (manager) not found"));
                department.setManager(manager);
            }
        } else {
            department.setManager(null);
        }

        return departmentMapper.toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentMapper.toDtoList(departmentRepository.findAll());
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with ID: " + id);
        }
        departmentRepository.deleteById(id);
    }
}