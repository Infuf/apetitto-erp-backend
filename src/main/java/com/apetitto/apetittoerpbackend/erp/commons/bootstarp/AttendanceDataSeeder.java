package com.apetitto.apetittoerpbackend.erp.commons.bootstarp;

import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeCreateDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import com.apetitto.apetittoerpbackend.erp.hr.repository.AttendanceRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.DepartmentRepository;
import com.apetitto.apetittoerpbackend.erp.hr.repository.EmployeeRepository;
import com.apetitto.apetittoerpbackend.erp.hr.service.EmployeeService;
import com.apetitto.apetittoerpbackend.erp.user.model.Role;
import com.apetitto.apetittoerpbackend.erp.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceDataSeeder implements CommandLineRunner {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    private static final ZoneId ZONE = ZoneId.of("Asia/Tashkent");

    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() > 0) {
            log.info("База данных сотрудников не пуста. Генерация пропущена.");
            return;
        }

        log.info("Начало генерации тестовых данных HR...");

        // 1. Подготовка ролей (Сервис требует наличие ROLE_USER)
        createRoleIfNotFound("ROLE_USER");
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_HR");

        // 2. Создание департаментов
        Department kitchen = createDepartment("Кухня (Kitchen)", "Повара и заготовки");
        Department hall = createDepartment("Зал (Service)", "Официанты и хостес");

        // 3. Создание сотрудников через Service
        List<Employee> createdEmployees = new ArrayList<>();

        // --- КУХНЯ ---
        createdEmployees.add(createEmployeeViaService("alisher", "Alisher", "Navoiy", kitchen.getId(), "Шеф-повар", 10000000));
        createdEmployees.add(createEmployeeViaService("sardor", "Sardor", "Rahimov", kitchen.getId(), "Су-шеф", 7000000));
        createdEmployees.add(createEmployeeViaService("bobur", "Bobur", "Aliyev", kitchen.getId(), "Повар ГЦ", 5000000));
        createdEmployees.add(createEmployeeViaService("dilshod", "Dilshod", "Karimov", kitchen.getId(), "Повар ХЦ", 4500000));
        createdEmployees.add(createEmployeeViaService("aziza", "Aziza", "Umarova", kitchen.getId(), "Кондитер", 5500000));

        // --- ЗАЛ ---
        createdEmployees.add(createEmployeeViaService("malika", "Malika", "Tursunova", hall.getId(), "Админ", 6000000));
        createdEmployees.add(createEmployeeViaService("jasur", "Jasur", "Bek", hall.getId(), "Официант", 3000000));
        createdEmployees.add(createEmployeeViaService("laylo", "Laylo", "Saidova", hall.getId(), "Официант", 3000000));
        createdEmployees.add(createEmployeeViaService("farrukh", "Farrukh", "Zokirov", hall.getId(), "Бармен", 4000000));
        createdEmployees.add(createEmployeeViaService("nodira", "Nodira", "Yuldasheva", hall.getId(), "Хостес", 3500000));

        // 4. Генерация посещаемости (Attendance)
        // Логика посещаемости не входит в EmployeeService, поэтому генерируем её здесь
        generateAttendanceHistory(createdEmployees);

        log.info("Генерация данных завершена успешно!");
    }

    private Employee createEmployeeViaService(String username, String firstName, String lastName, Long deptId, String position, double salary) {
        // Создаем DTO
        EmployeeCreateDto dto = new EmployeeCreateDto();
        dto.setUsername(username);
        dto.setPassword("12345"); // Пароль
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(username + "@apetitto.com");
        dto.setDepartmentId(deptId);
        dto.setPositionTitle(position);
        dto.setSalaryBase(BigDecimal.valueOf(salary));
        dto.setSalaryType(SalaryType.FIXED);

        // График работы
        dto.setShiftStartTime(LocalTime.of(9, 0));
        dto.setShiftEndTime(LocalTime.of(18, 0));

        // Вызываем сервис (Он создаст User, FinanceAccount, посчитает часы и т.д.)
        EmployeeResponseDto responseDto = employeeService.createEmployee(dto);

        // Достаем Entity из базы, так как AttendanceRecord требует объект Employee, а не DTO
        return employeeRepository.findById(responseDto.getId())
                .orElseThrow(() -> new RuntimeException("Ошибка при создании сотрудника"));
    }

    @Transactional
    public void generateAttendanceHistory(List<Employee> employees) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate startDate = today.minusDays(14); // 15 дней (включая сегодня)
        Random random = new Random();

        for (Employee emp : employees) {
            for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {

                // 20% вероятность выходного/прогула (записи нет)
                if (random.nextInt(10) < 2) continue;

                generateDailyRecord(emp, date, random);
            }
        }
    }

    private void generateDailyRecord(Employee emp, LocalDate date, Random random) {
        LocalTime shiftStart = emp.getShiftStartTime();
        LocalTime shiftEnd = emp.getShiftEndTime();

        int scenario = random.nextInt(100);
        LocalTime actualIn, actualOut;
        AttendanceStatus status;

        if (scenario < 60) {
            // 60% - Идеально (приход за 0-15 мин до, уход +0-15 мин)
            actualIn = shiftStart.minusMinutes(random.nextInt(15));
            actualOut = shiftEnd.plusMinutes(random.nextInt(15));
            status = AttendanceStatus.PRESENT;
        } else if (scenario < 80) {
            // 20% - Опоздание (10 - 90 мин)
            actualIn = shiftStart.plusMinutes(10 + random.nextInt(80));
            actualOut = shiftEnd;
            status = AttendanceStatus.PRESENT;
        } else if (scenario < 90) {
            // 10% - Ранний уход (30 - 120 мин)
            actualIn = shiftStart.minusMinutes(5);
            actualOut = shiftEnd.minusMinutes(30 + random.nextInt(90));
            status = AttendanceStatus.PRESENT;
        } else {
            // 10% - Овертайм (задержался на 1-3 часа)
            actualIn = shiftStart;
            actualOut = shiftEnd.plusHours(1 + random.nextInt(2));
            status = AttendanceStatus.PRESENT;
        }

        // Корректировка для "сегодня": если время ухода еще не наступило
        if (date.equals(LocalDate.now(ZONE)) && actualOut.isAfter(LocalTime.now(ZONE))) {
            actualOut = null; // Еще на смене
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(emp);
        record.setDate(date);
        record.setStatus(status);

        if (actualIn != null) record.setCheckIn(date.atTime(actualIn).atZone(ZONE).toInstant());
        if (actualOut != null) record.setCheckOut(date.atTime(actualOut).atZone(ZONE).toInstant());

        // Расчеты минут (дублируем логику сервиса, чтобы в базе были готовые цифры для Dashboard)
        calculateStats(record, shiftStart, shiftEnd, actualIn, actualOut);

        attendanceRepository.save(record);
    }

    private void calculateStats(AttendanceRecord record, LocalTime expectedIn, LocalTime expectedOut, LocalTime actualIn, LocalTime actualOut) {
        if (actualIn == null) return;

        // Опоздание
        if (actualIn.isAfter(expectedIn)) {
            record.setLateMinutes((int) ChronoUnit.MINUTES.between(expectedIn, actualIn));
        }

        if (actualOut != null) {
            record.setDurationMinutes((int) ChronoUnit.MINUTES.between(actualIn, actualOut));

            // Ранний уход
            if (actualOut.isBefore(expectedOut)) {
                record.setEarlyLeaveMinutes((int) ChronoUnit.MINUTES.between(actualOut, expectedOut));
            }

            // Переработка (если длительность больше плана)
            long planMinutes = ChronoUnit.MINUTES.between(expectedIn, expectedOut);
            if (record.getDurationMinutes() > planMinutes) {
                record.setOvertimeMinutes((int) (record.getDurationMinutes() - planMinutes));
            }
        }
    }

    private Department createDepartment(String name, String desc) {
        return departmentRepository.save(generateDepartment(name, desc));
    }

    private Department generateDepartment(String name, String desc) {
        Department dep = new Department();
        dep.setName(name);
        dep.setDescription(desc);
        return dep;
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
}