package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.hr.dto.AttendanceUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.DepartmentRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("API HR: Управление табелем (Attendance)")
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private FinanceAccountRepository financeAccountRepository;

    private Department itDepartment;
    private Department salesDepartment;

    private User managerUser;
    private User regularUser;
    private User otherUser;

    private Employee managerEmployee;
    private Employee regularEmployee;
    private Employee otherEmployee;

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        financeAccountRepository.deleteAll();
        userRepository.deleteAll();

        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_USER");

        managerUser = createUser("it_manager", Set.of("ROLE_USER"));
        itDepartment = createDepartment("IT Dept", managerUser);
        managerEmployee = createEmployee(managerUser, itDepartment, LocalTime.of(9, 0), LocalTime.of(18, 0));

        regularUser = createUser("dev_john", Set.of("ROLE_USER"));
        regularEmployee = createEmployee(regularUser, itDepartment, LocalTime.of(9, 0), LocalTime.of(18, 0));

        salesDepartment = createDepartment("Sales Dept", null);
        otherUser = createUser("sales_man", Set.of("ROLE_USER"));
        otherEmployee = createEmployee(otherUser, salesDepartment, LocalTime.of(9, 0), LocalTime.of(18, 0));
    }

    @Nested
    @DisplayName("Логика доступа (Security)")
    class SecurityTests {

        @Test
        @WithMockUser(username = "it_manager", roles = "USER")
        @DisplayName("Менеджер может редактировать сотрудника СВОЕГО отдела")
        void updateAttendance_managerOwnDept_success() throws Exception {
            AttendanceUpdateDto dto = new AttendanceUpdateDto();
            dto.setEmployeeId(regularEmployee.getId());
            dto.setDate(LocalDate.now());
            dto.setCheckIn(LocalTime.of(9, 0));

            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            assertTrue(attendanceRepository.findByEmployeeIdAndDate(regularEmployee.getId(), LocalDate.now()).isPresent());
        }

        @Test
        @WithMockUser(username = "it_manager", roles = "USER")
        @DisplayName("Менеджер НЕ может редактировать сотрудника ЧУЖОГО отдела")
        void updateAttendance_managerOtherDept_forbidden() throws Exception {
            AttendanceUpdateDto dto = new AttendanceUpdateDto();
            dto.setEmployeeId(otherEmployee.getId());
            dto.setDate(LocalDate.now());
            dto.setCheckIn(LocalTime.of(9, 0));

            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Админ может редактировать ЛЮБОГО сотрудника")
        void updateAttendance_admin_success() throws Exception {
            AttendanceUpdateDto dto = new AttendanceUpdateDto();
            dto.setEmployeeId(otherEmployee.getId());
            dto.setDate(LocalDate.now());
            dto.setCheckIn(LocalTime.of(8, 0));
            userRepository.save(createUser("user", Set.of("ROLE_ADMIN")));


            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "dev_john", roles = "USER")
        @DisplayName("Обычный сотрудник НЕ может редактировать даже себя (если он не менеджер)")
        void updateAttendance_regularUser_forbidden() throws Exception {
            AttendanceUpdateDto dto = new AttendanceUpdateDto();
            dto.setEmployeeId(regularEmployee.getId());
            dto.setDate(LocalDate.now());

            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Бизнес-логика и Валидация")
    class BusinessLogicTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Опоздание считается автоматически (График 09:00, Приход 09:30)")
        void calculations_lateArrival_shouldSetLateMinutes() throws Exception {
            AttendanceUpdateDto dto = new AttendanceUpdateDto();
            dto.setEmployeeId(regularEmployee.getId());
            dto.setDate(LocalDate.now());
            dto.setCheckIn(LocalTime.of(9, 30));
            dto.setCheckOut(LocalTime.of(18, 0));
            userRepository.save(createUser("user", Set.of("ROLE_ADMIN")));


            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(regularEmployee.getId(), LocalDate.now()).orElseThrow();
            assertEquals(30, record.getLateMinutes(), "Должно быть 30 минут опоздания");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Переработка считается автоматически (Уход позже графика)")
        void calculations_overtime_shouldSetOvertimeMinutes() throws Exception {
            AttendanceUpdateDto dto = new AttendanceUpdateDto();
            dto.setEmployeeId(regularEmployee.getId());
            dto.setDate(LocalDate.now());
            dto.setCheckIn(LocalTime.of(9, 0));
            dto.setCheckOut(LocalTime.of(20, 0));
            userRepository.save(createUser("user", Set.of("ROLE_ADMIN")));


            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(regularEmployee.getId(), LocalDate.now()).orElseThrow();

            assertEquals(120, record.getOvertimeMinutes());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Ошибка: Нельзя ставить дату в будущем")
        void validation_futureDate_shouldFail() throws Exception {
            AttendanceUpdateDto dto = new AttendanceUpdateDto();
            dto.setEmployeeId(regularEmployee.getId());
            dto.setDate(LocalDate.now().plusDays(1));
            userRepository.save(createUser("user", Set.of("ROLE_ADMIN")));


            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("Validation failed for argument")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Идемпотентность: Повторная отправка обновляет запись, а не создает новую")
        void update_shouldUpdateExistingRecord() throws Exception {
            AttendanceUpdateDto dto1 = new AttendanceUpdateDto();
            dto1.setEmployeeId(regularEmployee.getId());
            dto1.setDate(LocalDate.now());
            dto1.setCheckIn(LocalTime.of(9, 0));
            userRepository.save(createUser("user", Set.of("ROLE_ADMIN")));

            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto1)))
                    .andExpect(status().isOk());

            AttendanceUpdateDto dto2 = new AttendanceUpdateDto();
            dto2.setEmployeeId(regularEmployee.getId());
            dto2.setDate(LocalDate.now());
            dto2.setCheckOut(LocalTime.of(18, 0));

            mockMvc.perform(post("/api/v1/hr/attendance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto2)))
                    .andExpect(status().isOk());

            long count = attendanceRepository.count();
            assertEquals(1, count);

            AttendanceRecord record = attendanceRepository.findAll().get(0);
            assertNotNull(record.getCheckIn());
            assertNotNull(record.getCheckOut());
        }
    }


    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }

    private User createUser(String username, Set<String> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pass123456");
        user.setFirstName("User");
        user.setLastName("Test");
        var setOfRoles =
                roles.stream().map(role -> roleRepository.findByName(role).orElseThrow()).collect(Collectors.toSet());
        user.setRoles(setOfRoles);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Department createDepartment(String name, User manager) {
        Department dept = new Department();
        dept.setName(name);
        dept.setManager(manager);
        return departmentRepository.save(dept);
    }

    private Employee createEmployee(User user, Department dept, LocalTime start, LocalTime end) {
        FinanceAccount account = new FinanceAccount();
        account.setName("Account: " + user.getUsername());
        account.setType(FinanceAccountType.EMPLOYEE);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setIsActive(true);
        financeAccountRepository.save(account);

        Employee emp = new Employee();
        emp.setUser(user);
        emp.setDepartment(dept);
        emp.setFinanceAccount(account);
        emp.setSalaryType(SalaryType.HOURLY);
        emp.setSalaryBase(BigDecimal.TEN);
        emp.setShiftStartTime(start);
        emp.setShiftEndTime(end);
        emp.setHiredAt(Instant.now());
        emp.setIsActive(true);
        emp.setWorkHoursPerDay(new BigDecimal("9"));
        return employeeRepository.save(emp);
    }

    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("Expected condition to be true");
    }
}