package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.DepartmentRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.user.model.Role;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.RoleRepository;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.TransactionStatus.COMPLETED;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("API HR: Дашборд и Аналитика")
class AttendanceDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private FinanceAccountRepository accountRepository;
    @Autowired
    private FinanceTransactionRepository transactionRepository;

    private User managerUser;
    private User workerUser;
    private User otherUser;
    private User adminUser;

    private Department itDept;
    private Department salesDept;

    private Employee managerEmp;
    private Employee workerEmp;
    private Employee otherEmp;

    private FinanceAccount cashbox;

    private final LocalDate TODAY = LocalDate.now();
    private final ZoneId ZONE_ID = ZoneId.of("Asia/Tashkent");

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        transactionRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_USER");
        createRoleIfNotFound("ROLE_HR");

        cashbox = createFinanceAccount("Main Cashbox", FinanceAccountType.CASHBOX, null);


        managerUser = createUser("ali_manager", Set.of("ROLE_USER"));
        adminUser = createUser("admin1", Set.of("ROLE_ADMIN"));


        itDept = new Department();
        itDept.setName("IT Department");
        itDept.setManager(managerUser);
        itDept = departmentRepository.save(itDept);

        managerEmp = createEmployee(managerUser, itDept);

        workerUser = createUser("bob_worker", Set.of("ROLE_USER"));
        workerEmp = createEmployee(workerUser, itDept);


        otherUser = createUser("sales_guy", Set.of("ROLE_USER"));
        salesDept = new Department();
        salesDept.setName("Sales");
        salesDept = departmentRepository.save(salesDept);
        otherEmp = createEmployee(otherUser, salesDept);

    }

    @Nested
    @DisplayName("GET /grid (Табель-сетка)")
    class GridTests {

        @Test
        @WithMockUser(username = "ali_manager")
        @DisplayName("Менеджер видит сетку СВОЕГО департамента")
        void getGrid_managerOwnDept_shouldSucceed() throws Exception {
            createAttendance(workerEmp, TODAY, "09:30", "18:00");

            mockMvc.perform(get("/api/v1/hr/dashboard/grid")
                            .param("departmentId", itDept.getId().toString())
                            .param("dateFrom", TODAY.minusDays(1).toString())
                            .param("dateTo", TODAY.plusDays(1).toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rows", hasSize(2)))
                    .andExpect(jsonPath("$.rows[?(@.employeeId == " + workerEmp.getId() + ")].fullName").value("bob_worker Lastname"))
                    .andExpect(jsonPath("$.rows[?(@.employeeId == " + workerEmp.getId() + ")].days['" + TODAY + "'].status").value("PRESENT"));
        }

        @Test
        @WithMockUser(username = "ali_manager")
        @DisplayName("Менеджер НЕ видит сетку ЧУЖОГО департамента")
        void getGrid_managerOtherDept_shouldFail() throws Exception {
            mockMvc.perform(get("/api/v1/hr/dashboard/grid")
                            .param("departmentId", salesDept.getId().toString())
                            .param("dateFrom", TODAY.toString())
                            .param("dateTo", TODAY.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "bob_worker")
        @DisplayName("Обычный сотрудник (не менеджер) не имеет доступа к сетке")
        void getGrid_regularUser_shouldFail() throws Exception {
            mockMvc.perform(get("/api/v1/hr/dashboard/grid")
                            .param("departmentId", itDept.getId().toString())
                            .param("dateFrom", TODAY.toString())
                            .param("dateTo", TODAY.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin1", roles = "ADMIN")
        @DisplayName("Админ видит всё")
        void getGrid_admin_shouldSucceed() throws Exception {
            mockMvc.perform(get("/api/v1/hr/dashboard/grid")
                            .param("departmentId", salesDept.getId().toString())
                            .param("dateFrom", TODAY.toString())
                            .param("dateTo", TODAY.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rows", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /employee/{id}/details (Карточка)")
    class DetailsTests {

        @Test
        @WithMockUser(username = "ali_manager")
        @DisplayName("Менеджер видит карточку своего ПОДЧИНЕННОГО + Финансы")
        void getDetails_managerViewSubordinate_success() throws Exception {
            createAttendance(workerEmp, TODAY, "09:00", "18:00");
            createAdvancePayment(workerEmp, new BigDecimal("500000"));

            mockMvc.perform(get("/api/v1/hr/dashboard/employee/" + workerEmp.getId() + "/details")
                            .param("dateFrom", TODAY.minusDays(5).toString())
                            .param("dateTo", TODAY.plusDays(5).toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName", is("bob_worker Lastname")))
                    .andExpect(jsonPath("$.finance.currentBalance").value(500000.0))
                    .andExpect(jsonPath("$.finance.transactions[0].amount").value(500000.0));
        }

        @Test
        @WithMockUser(username = "ali_manager")
        @DisplayName("Менеджер НЕ видит карточку сотрудника из ЧУЖОГО отдела")
        void getDetails_managerViewOther_forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/hr/dashboard/employee/" + otherEmp.getId() + "/details")
                            .param("dateFrom", TODAY.toString())
                            .param("dateTo", TODAY.toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "bob_worker")
        @DisplayName("Сотрудник видит СВОЮ карточку")
        void getDetails_ownCard_success() throws Exception {
            mockMvc.perform(get("/api/v1/hr/dashboard/employee/" + workerEmp.getId() + "/details")
                            .param("dateFrom", TODAY.toString())
                            .param("dateTo", TODAY.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employeeId", is(workerEmp.getId().intValue())));
        }

        @Test
        @WithMockUser(username = "bob_worker")
        @DisplayName("Сотрудник НЕ видит чужую карточку (даже коллеги)")
        void getDetails_colleagueCard_forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/hr/dashboard/employee/" + managerEmp.getId() + "/details")
                            .param("dateFrom", TODAY.toString())
                            .param("dateTo", TODAY.toString()))
                    .andExpect(status().isForbidden());
        }
    }


    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role r = new Role();
            r.setName(name);
            roleRepository.save(r);
        }
    }

    private User createUser(String username, Set<String> rolesNames) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pass123456");
        user.setFirstName(username);
        user.setLastName("Lastname");
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        for (String roleName : rolesNames) {
            roles.add(roleRepository.findByName(roleName).orElseThrow());
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private FinanceAccount createFinanceAccount(String name, FinanceAccountType type, User user) {
        FinanceAccount acc = new FinanceAccount();
        acc.setName(name);
        acc.setType(type);
        acc.setUser(user);
        acc.setBalance(BigDecimal.ZERO);
        acc.setIsActive(true);
        return accountRepository.save(acc);
    }

    private Employee createEmployee(User user, Department dept) {
        FinanceAccount acc = createFinanceAccount("Acc: " + user.getUsername(), FinanceAccountType.EMPLOYEE, user);
        Employee e = new Employee();
        e.setUser(user);
        e.setDepartment(dept);
        e.setFinanceAccount(acc);
        e.setSalaryType(SalaryType.HOURLY);
        e.setSalaryBase(BigDecimal.TEN);
        e.setShiftStartTime(LocalTime.of(9, 0));
        e.setShiftEndTime(LocalTime.of(18, 0));
        e.setWorkHoursPerDay(new BigDecimal("9.00"));
        e.setHiredAt(Instant.now());
        e.setIsActive(true);
        return employeeRepository.save(e);
    }

    private void createAttendance(Employee emp, LocalDate date, String in, String out) {
        AttendanceRecord r = new AttendanceRecord();
        r.setEmployee(emp);
        r.setDate(date);
        r.setStatus(AttendanceStatus.PRESENT);
        if (in != null) r.setCheckIn(LocalTime.parse(in).atDate(date).atZone(ZONE_ID).toInstant());
        if (out != null) r.setCheckOut(LocalTime.parse(out).atDate(date).atZone(ZONE_ID).toInstant());
        r.setDurationMinutes(540);
        if (in.equals("09:30")) r.setLateMinutes(30);
        attendanceRepository.save(r);
    }

    private void createAdvancePayment(Employee emp, BigDecimal amount) {
        FinanceTransaction t = new FinanceTransaction();
        t.setAmount(amount);
        t.setOperationType(FinanceOperationType.SALARY_PAYOUT);
        t.setTransactionDate(java.time.Instant.now());
        t.setFromAccount(cashbox);
        t.setToAccount(emp.getFinanceAccount());
        t.setStatus(COMPLETED);
        emp.getFinanceAccount().setBalance(emp.getFinanceAccount().getBalance().add(amount));
        accountRepository.save(emp.getFinanceAccount());
        transactionRepository.save(t);
    }
}