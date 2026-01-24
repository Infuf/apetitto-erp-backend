package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollRequestDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.PayrollStatus;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.PayrollAccrualRepository;
import com.apetitto.apetittoerpbackend.erp.user.model.Role;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.RoleRepository;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("API HR: Расчет Зарплаты (Payroll)")
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private PayrollAccrualRepository payrollRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private FinanceAccountRepository accountRepository;
    @Autowired
    private FinanceTransactionRepository transactionRepository;

    private Employee fixedEmployee;
    private final LocalDate PERIOD_START = LocalDate.of(2026, 10, 1);
    private final LocalDate PERIOD_END = LocalDate.of(2026, 10, 30); 
    private final BigDecimal SALARY_BASE = new BigDecimal("2800000.00");

    @BeforeEach
    void setUp() {
        payrollRepository.deleteAll();
        attendanceRepository.deleteAll();
        transactionRepository.deleteAll();
        employeeRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        createRole("ROLE_HR");
        createRole("ROLE_USER");

        createUser("hr_manager", "ROLE_HR");

        fixedEmployee = createEmployee("worker_ali", SalaryType.FIXED, SALARY_BASE);
    }

    @Nested
    @DisplayName("POST /calculate (Расчет)")
    class CalculateTests {

        @Test
        @WithMockUser(username = "hr_manager", roles = "HR")
        @DisplayName("FIXED: Полный месяц без опозданий -> Полный оклад")
        void calculate_fullMonth_shouldPayFullSalary() throws Exception {
            createFullAttendance(fixedEmployee, 28, 0);

            PayrollRequestDto request = new PayrollRequestDto();
            request.setPeriodStart(PERIOD_START);
            request.setPeriodEnd(PERIOD_END);
            request.setEmployeeId(fixedEmployee.getId());

            mockMvc.perform(post("/api/v1/hr/payroll/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            List<PayrollAccrual> accruals = payrollRepository.findAll();
            assertEquals(1, accruals.size());
            PayrollAccrual accrual = accruals.get(0);

            assertEquals(0, SALARY_BASE.compareTo(accrual.getFinalAmount()), "Должен получить полный оклад");
            assertEquals(28, accrual.getDaysWorked());
            assertEquals(0, BigDecimal.ZERO.compareTo(accrual.getPenaltyAmount()));

            FinanceAccount account = accountRepository.findById(fixedEmployee.getFinanceAccount().getId()).orElseThrow();
            assertEquals(0, SALARY_BASE.negate().compareTo(account.getBalance()), "Баланс сотрудника должен уйти в минус на сумму ЗП");
        }

        @Test
        @WithMockUser(username = "hr_manager", roles = "HR")
        @DisplayName("FIXED: С опозданиями -> Оклад минус штраф")
        void calculate_withLateness_shouldDeductPenalty() throws Exception {
            createFullAttendance(fixedEmployee, 27, 0);
            createOneDayAttendance(fixedEmployee, PERIOD_START.plusDays(29), 540);


            PayrollRequestDto request = new PayrollRequestDto();
            request.setPeriodStart(PERIOD_START);
            request.setPeriodEnd(PERIOD_END);
            request.setEmployeeId(fixedEmployee.getId());

            mockMvc.perform(post("/api/v1/hr/payroll/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            PayrollAccrual accrual = payrollRepository.findAll().get(0);

            assertTrue(accrual.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0, "Штраф должен быть начислен");

            BigDecimal expected = new BigDecimal("2700000.00");
            BigDecimal actual = accrual.getFinalAmount();
            assertTrue(expected.subtract(actual).abs().compareTo(BigDecimal.ONE) < 0,
                    "Ожидаем " + expected + ", получили " + actual);
        }

        @Test
        @WithMockUser(username = "hr_manager", roles = "HR")
        @DisplayName("Идемпотентность: Повторный запуск не создает дублей")
        void calculate_twice_shouldSkipSecond() throws Exception {
            createFullAttendance(fixedEmployee, 28, 0);
            PayrollRequestDto request = new PayrollRequestDto();
            request.setPeriodStart(PERIOD_START);
            request.setPeriodEnd(PERIOD_END);
            request.setEmployeeId(fixedEmployee.getId());

            mockMvc.perform(post("/api/v1/hr/payroll/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/hr/payroll/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            assertEquals(1, payrollRepository.count());
            assertEquals(1, transactionRepository.count());
        }
    }

    @Nested
    @DisplayName("POST /{id}/cancel (Отмена)")
    class CancelTests {

        @Test
        @WithMockUser(username = "hr_manager", roles = "HR")
        @DisplayName("Отмена начисления должна откатить финансовый баланс")
        void cancelPayroll_shouldRollbackFinance() throws Exception {
            createFullAttendance(fixedEmployee, 28, 0);
            PayrollRequestDto request = new PayrollRequestDto();
            request.setPeriodStart(PERIOD_START);
            request.setPeriodEnd(PERIOD_END);
            request.setEmployeeId(fixedEmployee.getId());

            mockMvc.perform(post("/api/v1/hr/payroll/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            PayrollAccrual accrual = payrollRepository.findAll().get(0);

            FinanceAccount accBefore = accountRepository.findById(fixedEmployee.getFinanceAccount().getId()).orElseThrow();
            assertEquals(0, SALARY_BASE.negate().compareTo(accBefore.getBalance()));

            mockMvc.perform(post("/api/v1/hr/payroll/" + accrual.getId() + "/cancel"))
                    .andExpect(status().isOk());

            PayrollAccrual cancelledAccrual = payrollRepository.findById(accrual.getId()).orElseThrow();
            assertEquals(PayrollStatus.CANCELLED, cancelledAccrual.getStatus());

            FinanceAccount accAfter = accountRepository.findById(fixedEmployee.getFinanceAccount().getId()).orElseThrow();
            assertEquals(0, BigDecimal.ZERO.compareTo(accAfter.getBalance()), "Баланс должен восстановиться");
        }
    }


    private void createRole(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role r = new Role();
            r.setName(name);
            roleRepository.save(r);
        }
    }

    private User createUser(String username, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pass");
        user.setFirstName("User");
        user.setLastName("Test");
        user.setEnabled(true);
        Role role = roleRepository.findByName(roleName).orElseThrow();
        user.setRoles(Collections.singleton(role));
        return userRepository.save(user);
    }

    private Employee createEmployee(String username, SalaryType type, BigDecimal salary) {
        User user = createUser(username, "ROLE_USER");

        FinanceAccount acc = new FinanceAccount();
        acc.setName("Acc: " + username);
        acc.setType(FinanceAccountType.EMPLOYEE);
        acc.setUser(user);
        acc.setBalance(BigDecimal.ZERO);
        acc.setIsActive(true);
        accountRepository.save(acc);

        Employee e = new Employee();
        e.setUser(user);
        e.setFinanceAccount(acc);
        e.setSalaryType(type);
        e.setSalaryBase(salary);
        e.setDaysOffPerMonth(2);
        e.setWorkHoursPerDay(new BigDecimal("9.00"));
        e.setShiftStartTime(LocalTime.of(9, 0));
        e.setShiftEndTime(LocalTime.of(18, 0));
        e.setIsActive(true);
        return employeeRepository.save(e);
    }

    private void createFullAttendance(Employee emp, int days, int lateMinutesPerDay) {
        for (int i = 0; i < days; i++) {
            createOneDayAttendance(emp, PERIOD_START.plusDays(i), lateMinutesPerDay);
        }
    }

    private void createOneDayAttendance(Employee emp, LocalDate date, int lateMinutes) {
        AttendanceRecord r = new AttendanceRecord();
        r.setEmployee(emp);
        r.setDate(date);
        r.setStatus(AttendanceStatus.PRESENT);
        r.setLateMinutes(lateMinutes);
        r.setEarlyLeaveMinutes(0);
        r.setOvertimeMinutes(0);
        r.setEarlyComeMinutes(0);
        r.setDurationMinutes(540);
        attendanceRepository.save(r);
    }
}