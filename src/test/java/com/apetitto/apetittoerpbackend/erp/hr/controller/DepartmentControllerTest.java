package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.hr.dto.DepartmentDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.Department;
import com.apetitto.apetittoerpbackend.erp.hr.repository.DepartmentRepository;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("API HR: Департаменты")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;

    private User testManager;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();

        testManager = userRepository.findById(2L).orElseThrow();
    }

    @Nested
    @DisplayName("POST / (Создание)")
    class CreateTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Успешное создание департамента с менеджером")
        void createDepartment_valid_shouldSucceed() throws Exception {
            DepartmentDto dto = new DepartmentDto();
            dto.setName("IT Отдел");
            dto.setDescription("Разработка и поддержка");
            dto.setManagerId(testManager.getId());

            mockMvc.perform(post("/api/v1/hr/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name", is("IT Отдел")))
                    .andExpect(jsonPath("$.managerName").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Ошибка: Дубликат имени")
        void createDepartment_duplicateName_shouldFail() throws Exception {
            createDepartmentInDb("Бухгалтерия");

            DepartmentDto dto = new DepartmentDto();
            dto.setName("Бухгалтерия");

            mockMvc.perform(post("/api/v1/hr/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("A department")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Ошибка: Несуществующий менеджер")
        void createDepartment_invalidManager_shouldFail() throws Exception {
            DepartmentDto dto = new DepartmentDto();
            dto.setName("Новый Отдел");
            dto.setManagerId(9999L);

            mockMvc.perform(post("/api/v1/hr/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(containsString("User (manager) not found")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Ошибка доступа: Обычный юзер не может создавать")
        void createDepartment_forbiddenUser_shouldFail() throws Exception {
            DepartmentDto dto = new DepartmentDto();
            dto.setName("Hacker Dept");

            mockMvc.perform(post("/api/v1/hr/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /{id} (Обновление)")
    class UpdateTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Успешное обновление названия")
        void updateDepartment_valid_shouldSucceed() throws Exception {
            Department dept = createDepartmentInDb("Старое Название");

            DepartmentDto updateDto = new DepartmentDto();
            updateDto.setName("Новое Название");
            updateDto.setDescription("Обновленное описание");

            mockMvc.perform(put("/api/v1/hr/departments/" + dept.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Новое Название")))
                    .andExpect(jsonPath("$.managerId").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Ошибка: Конфликт имен при обновлении")
        void updateDepartment_duplicateName_shouldFail() throws Exception {
            createDepartmentInDb("Отдел А");
            Department deptB = createDepartmentInDb("Отдел Б");

            DepartmentDto updateDto = new DepartmentDto();
            updateDto.setName("Отдел А");

            mockMvc.perform(put("/api/v1/hr/departments/" + deptB.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString(" already exists.")));
        }
    }

    @Nested
    @DisplayName("GET / (Получение списка)")
    class GetAllTests {

        @Test
        @WithMockUser
        void getAllDepartments_shouldReturnList() throws Exception {
            createDepartmentInDb("HR");
            createDepartmentInDb("Sales");

            mockMvc.perform(get("/api/v1/hr/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].name", org.hamcrest.Matchers.hasItems("HR", "Sales")));
        }
    }

    @Nested
    @DisplayName("DELETE /{id} (Удаление)")
    class DeleteTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteDepartment_shouldSucceed() throws Exception {
            Department dept = createDepartmentInDb("Temp Dept");

            mockMvc.perform(delete("/api/v1/hr/departments/" + dept.getId()))
                    .andExpect(status().isNoContent());

            assertFalse(departmentRepository.existsById(dept.getId()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteDepartment_notFound_shouldFail() throws Exception {
            mockMvc.perform(delete("/api/v1/hr/departments/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    private Department createDepartmentInDb(String name) {
        Department dept = new Department();
        dept.setName(name);
        return departmentRepository.save(dept);
    }
}