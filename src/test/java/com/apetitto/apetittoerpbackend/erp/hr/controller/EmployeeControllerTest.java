package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeCreateDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalTime;

import static com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType.FIXED;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("API HR: Управление сотрудниками")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FinanceAccountRepository financeAccountRepository;
    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        financeAccountRepository.deleteAll();
        userRepository.deleteAll();

        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role role = new Role();
            role.setName("ROLE_USER");
            roleRepository.save(role);
        }
    }

    @Nested
    @DisplayName("POST / (Найм сотрудника)")
    class CreateEmployeeTests {

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Успешный найм: Должны создаться User, Account и Employee")
        void createEmployee_validRequest_shouldCreateAllEntities() throws Exception {
            EmployeeCreateDto dto = new EmployeeCreateDto();
            dto.setUsername("baker_ali");
            dto.setPassword("securePass123");
            dto.setFirstName("Али");
            dto.setLastName("Валиев");
            dto.setPositionTitle("Главный Пекарь");
            dto.setSalaryType(SalaryType.HOURLY);
            dto.setSalaryBase(new BigDecimal("50000.00"));
            dto.setShiftStartTime(LocalTime.of(8, 0));
            dto.setShiftEndTime(LocalTime.of(17, 0));
            dto.setTerminalId(1001L);

            MvcResult result = mockMvc.perform(post("/api/v1/hr/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username", is("baker_ali")))
                    .andExpect(jsonPath("$.fullName", is("Али Валиев")))
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.financeAccountId").exists())
                    .andReturn();


            Long employeeId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
            Employee savedEmployee = employeeRepository.findById(employeeId).orElseThrow();

            assertEquals("Главный Пекарь", savedEmployee.getPositionTitle());
            assertEquals(0, new BigDecimal("9.00").compareTo(savedEmployee.getWorkHoursPerDay()));
            assertEquals(1001L, savedEmployee.getTerminalId());
            assertNotNull(savedEmployee.getHiredAt(), "Дата найма должна проставиться автоматически");

            User savedUser = userRepository.findByUsername("baker_ali").orElseThrow();
            assertEquals("Али", savedUser.getFirstName());
            assertTrue(savedUser.isEnabled(), "Пользователь должен быть активен");
            assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER")), "Роль по умолчанию должна быть USER");

            FinanceAccount savedAccount = financeAccountRepository.findById(savedEmployee.getFinanceAccount().getId()).orElseThrow();
            assertEquals(FinanceAccountType.EMPLOYEE, savedAccount.getType());
            assertEquals("Сотрудник: Али Валиев", savedAccount.getName(), "Имя счета должно генерироваться автоматически");
            assertEquals(0, BigDecimal.ZERO.compareTo(savedAccount.getBalance()), "Начальный баланс должен быть 0");
        }

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Ошибка валидации: Дубликат логина")
        void createEmployee_duplicateUsername_shouldFail() throws Exception {
            User existingUser = new User();
            existingUser.setUsername("existing_user");
            existingUser.setPassword("pass123456");
            userRepository.save(existingUser);

            EmployeeCreateDto dto = new EmployeeCreateDto();
            dto.setUsername("existing_user");
            dto.setPassword("pass123456");
            dto.setFirstName("Test");
            dto.setLastName("Test");
            dto.setPositionTitle("Test");
            dto.setSalaryType(FIXED);
            dto.setSalaryBase(BigDecimal.TEN);

            mockMvc.perform(post("/api/v1/hr/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser(roles = "USER")
        void createEmployee_forbiddenUser_shouldFail() throws Exception {
            EmployeeCreateDto dto = new EmployeeCreateDto();
            dto.setUsername("hacker");
            dto.setPassword("123456789");
            dto.setFirstName("first");
            dto.setLastName("last");
            dto.setPositionTitle("somthing");
            dto.setSalaryType(FIXED);
            dto.setSalaryBase(new BigDecimal("0.00"));

            mockMvc.perform(post("/api/v1/hr/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /{id} (Обновление сотрудника)")
    class UpdateEmployeeTests {

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Изменение имени сотрудника должно обновлять User и название счета")
        void updateEmployee_nameChange_shouldSyncAllModules() throws Exception {
            Employee original = createHelperEmployee("old_name", "Иван", "Иванов");

            EmployeeUpdateDto updateDto = new EmployeeUpdateDto();
            updateDto.setFirstName("Петр");
            updateDto.setLastName("Петров");
            updateDto.setShiftStartTime(LocalTime.of(10, 0));
            updateDto.setShiftEndTime(LocalTime.of(14, 0));

            mockMvc.perform(put("/api/v1/hr/employees/" + original.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName", is("Петр Петров")));

            Employee updatedEmp = employeeRepository.findById(original.getId()).orElseThrow();
            assertEquals(0, new BigDecimal("4.00").compareTo(updatedEmp.getWorkHoursPerDay()));

            User updatedUser = userRepository.findById(updatedEmp.getUser().getId()).orElseThrow();
            assertEquals("Петр", updatedUser.getFirstName());
            assertEquals("Петров", updatedUser.getLastName());

            FinanceAccount updatedAccount = financeAccountRepository.findById(updatedEmp.getFinanceAccount().getId()).orElseThrow();
            assertEquals("Сотрудник: Петр Петров", updatedAccount.getName());
        }
    }

    @Nested
    @DisplayName("DELETE /{id} (Увольнение)")
    class DismissTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Увольнение должно деактивировать все связанные сущности")
        void dismissEmployee_shouldSoftDeleteEverything() throws Exception {
            Employee employee = createHelperEmployee("fired_guy", "Джон", "Доу");

            mockMvc.perform(delete("/api/v1/hr/employees/" + employee.getId()))
                    .andExpect(status().isOk());

            Employee dismissedEmp = employeeRepository.findById(employee.getId()).orElseThrow();
            assertFalse(dismissedEmp.getIsActive(), "Сотрудник должен быть неактивен");

            User blockedUser = userRepository.findById(dismissedEmp.getUser().getId()).orElseThrow();
            assertFalse(blockedUser.isEnabled(), "Вход в систему должен быть заблокирован");

            FinanceAccount frozenAccount = financeAccountRepository.findById(dismissedEmp.getFinanceAccount().getId()).orElseThrow();
            assertFalse(frozenAccount.getIsActive(), "Счет должен быть заморожен");
        }
    }

    private Employee createHelperEmployee(String username, String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pass123456");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        Role roleUser = roleRepository.findByName("ROLE_USER").orElseThrow();
        user.getRoles().add(roleUser);
        user = userRepository.save(user);

        FinanceAccount account = new FinanceAccount();
        account.setName("Сотрудник: " + firstName);
        account.setType(FinanceAccountType.EMPLOYEE);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setIsActive(true);
        account = financeAccountRepository.save(account);

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFinanceAccount(account);
        employee.setSalaryBase(BigDecimal.TEN);
        employee.setSalaryType(SalaryType.HOURLY);
        employee.setIsActive(true);
        employee.setShiftStartTime(LocalTime.of(9, 0));
        employee.setShiftEndTime(LocalTime.of(18, 0));
        employee.setWorkHoursPerDay(new BigDecimal("9.00"));

        return employeeRepository.save(employee);
    }
}