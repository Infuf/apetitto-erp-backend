package com.apetitto.apetittoerpbackend.erp.hr.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceTransactionService;
import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollAccrualDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollRequestDto;
import com.apetitto.apetittoerpbackend.erp.hr.mapper.PayrollMapper;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.PayrollAccrualRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.specification.PayrollSpecifications;
import com.apetitto.apetittoerpbackend.erp.hr.service.PayrollService;
import com.apetitto.apetittoerpbackend.erp.hr.service.strategy.SalaryCalculationStrategy;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.apetitto.apetittoerpbackend.erp.hr.model.enums.PayrollStatus.APPROVED;
import static com.apetitto.apetittoerpbackend.erp.hr.model.enums.PayrollStatus.CANCELLED;
import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollAccrualRepository payrollRepository;
    private final UserRepository userRepository;
    private final FinanceTransactionService financeService;
    private final List<SalaryCalculationStrategy> strategies;
    private final PayrollMapper payrollMapper;
    private final FinanceTransactionRepository financeTransactionRepository;

    @Override
    @Transactional
    public void calculateAndAccruePayroll(PayrollRequestDto request) {

        List<Employee> employees;

        if (request.getEmployeeId() != null) {
            employees = List.of(employeeRepository.findById(request.getEmployeeId()).orElseThrow());
        } else if (request.getDepartmentId() != null) {
            employees = employeeRepository.findAllByDepartmentIdAndIsActiveIsTrue(request.getDepartmentId());
        } else {
            employees = employeeRepository.findAllByIsActiveIsTrue();
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        for (Employee emp : employees) {

            if (payrollRepository.existsByEmployeeIdAndPeriodStartAndPeriodEndAndStatusNot(emp.getId(),
                    request.getPeriodStart(), request.getPeriodEnd(), CANCELLED)) {
                continue;
            }

            SalaryCalculationStrategy strategy = strategies.stream()
                    .filter(s -> s.getSupportedType() == emp.getSalaryType())
                    .findFirst()
                    .orElseThrow(() -> new InvalidRequestException("No strategy for: " + emp.getSalaryType()));

            PayrollAccrual accrual = new PayrollAccrual();
            accrual.setEmployee(emp);
            accrual.setPeriodStart(request.getPeriodStart());
            accrual.setPeriodEnd(request.getPeriodEnd());
            accrual.setCreatedBy(currentUser);
            accrual.setPaymentType(emp.getSalaryType());
            accrual.setStatus(APPROVED);

            accrual.setBaseSalary(emp.getSalaryBase());
            accrual.setBaseWorkHours(emp.getWorkHoursPerDay());
            accrual.setBaseDaysOff(emp.getDaysOffPerMonth() != null ? emp.getDaysOffPerMonth() : 0);

            List<AttendanceRecord> records = attendanceRepository.findAllByEmployeeIdsAndDateRange(
                    List.of(emp.getId()), request.getPeriodStart(), request.getPeriodEnd());

            strategy.calculate(accrual, emp, records);
            if (accrual.getFinalAmount().compareTo(ZERO) <= 0) {
                continue;
            }

            TransactionCreateRequestDto trxDto = new TransactionCreateRequestDto();
            trxDto.setAmount(accrual.getFinalAmount());
            trxDto.setOperationType(FinanceOperationType.SALARY_ACCRUAL);
            trxDto.setFromAccountId(emp.getFinanceAccount().getId());
            trxDto.setToAccountId(null);
            trxDto.setDescription("ЗП: " + emp.getUser().getFirstName());
            trxDto.setTransactionDate(java.time.Instant.now());

            var tranDto = financeService.createTransaction(trxDto);
            var transaction = financeTransactionRepository.getReferenceById(tranDto.getId());
            accrual.setFinanceTransaction(transaction);
            payrollRepository.save(accrual);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollAccrualDto> getPayrolls(LocalDate dateFrom, LocalDate dateTo, Long departmentId, Long employeeId, Pageable pageable) {

        Specification<PayrollAccrual> spec =
                PayrollSpecifications.periodBetween(dateFrom, dateTo)
                        .and(PayrollSpecifications.hasDepartment(departmentId))
                        .and(PayrollSpecifications.hasEmployee(employeeId));

        return payrollRepository.findAll(spec, pageable).map(payrollMapper::toDto);
    }

    @Override
    @Transactional
    public void cancelPayroll(Long id) {
        PayrollAccrual accrual = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));

        if (CANCELLED.equals(accrual.getStatus())) {
            throw new InvalidRequestException("Already cancelled");
        }

        if (accrual.getFinanceTransaction() != null) {
            financeService.cancelTransaction(
                    accrual.getFinanceTransaction().getId(),
                    "Отмена начисления зарплаты #" + id
            );
        }

        accrual.setStatus(CANCELLED);
        payrollRepository.save(accrual);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollAccrualDto getPayrollById(Long id) {
        PayrollAccrual accrual = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));
        return payrollMapper.toDto(accrual);
    }
}