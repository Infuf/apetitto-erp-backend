package com.apetitto.apetittoerpbackend.erp.hr.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeCreateDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.mapper.EmployeeMapper;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.repository.DepartmentRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.service.EmployeeService;
import com.apetitto.apetittoerpbackend.erp.user.model.Role;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.RoleRepository;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final FinanceAccountRepository financeAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateDto dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new InvalidRequestException("User with username " + dto.getUsername() + " already exists");
        }

        User user = employeeMapper.toUserEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_USER not found in database"));
        user.setRoles(Collections.singleton(userRole));

        user = userRepository.save(user);

        FinanceAccount salaryAccount = new FinanceAccount();
        salaryAccount.setName("Сотрудник: " + user.getFirstName() + " " + user.getLastName());
        salaryAccount.setType(FinanceAccountType.EMPLOYEE);
        salaryAccount.setBalance(BigDecimal.ZERO);
        salaryAccount.setIsActive(true);
        salaryAccount.setUser(user);
        salaryAccount = financeAccountRepository.save(salaryAccount);

        Employee employee = employeeMapper.toEntity(dto);
        employee.setUser(user);
        employee.setFinanceAccount(salaryAccount);

        employee.setWorkHoursPerDay(calculateWorkHours(dto.getShiftStartTime(), dto.getShiftEndTime()));

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(dept);
        }

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        employeeMapper.updateEntity(dto, employee);

        if (dto.getShiftStartTime() != null || dto.getShiftEndTime() != null) {
            LocalTime start = dto.getShiftStartTime() != null ? dto.getShiftStartTime() : employee.getShiftStartTime();
            LocalTime end = dto.getShiftEndTime() != null ? dto.getShiftEndTime() : employee.getShiftEndTime();

            employee.setWorkHoursPerDay(calculateWorkHours(start, end));
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(dept);
        }

        User user = employee.getUser();
        boolean userChanged = false;
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
            userChanged = true;
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
            userChanged = true;
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
            userChanged = true;
        }

        if (userChanged) {
            userRepository.save(user);
            FinanceAccount account = employee.getFinanceAccount();
            account.setName("Сотрудник: " + user.getFirstName() + " " + user.getLastName());
            financeAccountRepository.save(account);
        }

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(employeeMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Override
    @Transactional
    public void dismissEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setIsActive(false);
        employee.getUser().setEnabled(false);
        employee.getFinanceAccount().setIsActive(false);

        employeeRepository.save(employee);
    }

    private BigDecimal calculateWorkHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return new BigDecimal("0.00");
        }

        long minutes;
        if (end.isBefore(start)) {
            minutes = Duration.between(start, LocalTime.MAX).toMinutes() +
                    Duration.between(LocalTime.MIN, end).toMinutes() + 1;
        } else {
            minutes = Duration.between(start, end).toMinutes();
        }

        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}