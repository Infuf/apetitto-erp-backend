package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.WarehouseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@DisplayName("Интеграционные тесты для WarehouseController")
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Nested
    @DisplayName("Тесты для GET /api/v1/warehouses")
    class GetAllWarehousesTests {

        @Test
        @WithMockUser
        void getAllWarehouses_whenAuthenticated_shouldReturnWarehouseList() throws Exception {
            mockMvc.perform(get("/api/v1/warehouses"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].name", is("Тестовый Основной склад")));
        }

        @Test
        void getAllWarehouses_whenAnonymous_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/warehouses"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/warehouses/{id}")
    class GetWarehouseByIdTests {

        @Test
        @WithMockUser
        void getWarehouseById_whenWarehouseExists_shouldReturnWarehouse() throws Exception {
            mockMvc.perform(get("/api/v1/warehouses/101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(101)))
                    .andExpect(jsonPath("$.name", is("Тестовый Основной склад")));
        }

        @Test
        @WithMockUser
        void getWarehouseById_whenWarehouseNotExists_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/warehouses/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Warehouse with ID 999 not found")));
        }
    }

    @Nested
    @DisplayName("Тесты для POST /api/v1/warehouses")
    class CreateWarehouseTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void createWarehouse_whenValidRequest_shouldReturnCreatedWarehouse() throws Exception {
            WarehouseDto newWarehouse = new WarehouseDto();
            newWarehouse.setName("Новый склад");
            newWarehouse.setLocation("г. Фергана");

            mockMvc.perform(post("/api/v1/warehouses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newWarehouse)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name", is("Новый склад")));
        }
    }

    @Nested
    @DisplayName("Тесты для PUT /api/v1/warehouses")
    class UpdateWarehouseTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateWarehouse_whenWarehouseExists_shouldReturnUpdatedWarehouse() throws Exception {
            WarehouseDto updatedWarehouse = new WarehouseDto();
            updatedWarehouse.setId(102L);
            updatedWarehouse.setName("Магазин 'Центральный' (обновленный)");
            updatedWarehouse.setLocation("Новый адрес");

            mockMvc.perform(put("/api/v1/warehouses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedWarehouse)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(102)))
                    .andExpect(jsonPath("$.name", is("Магазин 'Центральный' (обновленный)")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateWarehouse_whenWarehouseNotExists_shouldReturnNotFound() throws Exception {
            WarehouseDto updatedWarehouse = new WarehouseDto();
            updatedWarehouse.setId(999L);
            updatedWarehouse.setName("Несуществующий склад");

            mockMvc.perform(put("/api/v1/warehouses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedWarehouse)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Тесты для DELETE /api/v1/warehouses/{id}")
    class DeleteWarehouseTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteWarehouse_whenWarehouseExistsAndNotUsed_shouldReturnNoContent() throws Exception {
            mockMvc.perform(delete("/api/v1/warehouses/103"))
                    .andExpect(status().isNoContent());

            assertFalse(warehouseRepository.existsById(103L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void deleteWarehouse_whenWarehouseIsInUse_shouldReturnConflict() throws Exception {
            mockMvc.perform(delete("/api/v1/warehouses/101"))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteWarehouse_whenWarehouseNotExists_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/warehouses/999"))
                    .andExpect(status().isNotFound());
        }
    }
}