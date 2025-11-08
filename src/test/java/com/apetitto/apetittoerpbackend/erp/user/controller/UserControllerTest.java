package com.apetitto.apetittoerpbackend.erp.user.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.user.dto.PasswordResetRequestDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.UserDto;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@DisplayName("API Администрирования: Пользователи (/api/v1/users)")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final String ADMIN_USERNAME = "admin";
    private static final Long MANAGER_ID = 2L;
    private static final String MANAGER_USERNAME = "manager";
    private static final Long WAREHOUSE_ID_SHOP_SOUTH = 103L;

    @Nested
    @DisplayName("GET / (Получение списка)")
    class GetUsersTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUsers_whenAdmin_shouldReturnUserPage() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)))
                    .andExpect(jsonPath("$.content[0].username", is(ADMIN_USERNAME)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void getUsers_whenNotAdmin_shouldReturnForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /{id} (Получение по ID)")
    class GetUserByIdTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        void getUserById_whenUserExists_shouldReturnUser() throws Exception {
            mockMvc.perform(get("/api/v1/users/" + MANAGER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(MANAGER_ID.intValue())))
                    .andExpect(jsonPath("$.username", is(MANAGER_USERNAME)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUserById_whenUserNotExists_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/users/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /{id} (Обновление)")
    class UpdateUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void updateUser_whenValidRequest_shouldUpdateAllFields() throws Exception {
            UserDto updateDto = new UserDto();
            updateDto.setId(MANAGER_ID);
            updateDto.setFirstName("Мария");
            updateDto.setLastName("Иванова");
            updateDto.setEmail("new_manager@apetitto.com");
            updateDto.setEnabled(false);
            updateDto.setWarehouseId(WAREHOUSE_ID_SHOP_SOUTH);
            updateDto.setRoles(Set.of("ROLE_USER", "ROLE_STORE_MANAGER"));

            mockMvc.perform(put("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lastName", is("Иванова")))
                    .andExpect(jsonPath("$.enabled", is(false)))
                    .andExpect(jsonPath("$.warehouseId", is(WAREHOUSE_ID_SHOP_SOUTH.intValue())))
                    .andExpect(jsonPath("$.roles", hasSize(2)));

            User updatedUser = userRepository.findById(MANAGER_ID).orElseThrow();
            assertFalse(updatedUser.isEnabled());
            assertEquals(WAREHOUSE_ID_SHOP_SOUTH, updatedUser.getWarehouse().getId());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateUser_withInvalidRole_shouldReturnBadRequest() throws Exception {
            UserDto updateDto = new UserDto();
            updateDto.setId(MANAGER_ID);
            updateDto.setRoles(Set.of("ROLE_INVALID"));

            mockMvc.perform(put("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /{id}/reset-password (Сброс пароля)")
    class ResetPasswordTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void resetPassword_whenAdmin_shouldChangePassword() throws Exception {
            PasswordResetRequestDto dto = new PasswordResetRequestDto();
            dto.setNewPassword("newStrongPassword123");

            mockMvc.perform(post("/api/v1/users/{id}/reset-password", MANAGER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            User updatedUser = userRepository.findById(MANAGER_ID).orElseThrow();
            assertTrue(passwordEncoder.matches("newStrongPassword123", updatedUser.getPassword()));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void resetPassword_whenNotAdmin_shouldReturnForbidden() throws Exception {
            PasswordResetRequestDto dto = new PasswordResetRequestDto();
            dto.setNewPassword("somePassword");

            mockMvc.perform(post("/api/v1/users/{id}/reset-password", MANAGER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /{id} (Удаление)")
    class DeleteUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
            // Создадим нового пользователя специально для теста на удаление
            User userToDelete = new User();
            userToDelete.setUsername("to_delete");
            userToDelete.setPassword("password");
            userToDelete.setEmail("to_delete@test.com");
            userRepository.save(userToDelete);

            mockMvc.perform(delete("/api/v1/users/" + userToDelete.getId()))
                    .andExpect(status().isNoContent());

            assertFalse(userRepository.existsById(userToDelete.getId()));
        }

    }
}