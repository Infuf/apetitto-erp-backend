package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.ProductDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.UnitType;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.ProductRepository;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("Интеграционные тесты для ProductController")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Nested
    @DisplayName("Тесты для GET /api/v1/products")
    class GetAllProductsTests {

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void getAllProducts_shouldReturnPagedProducts() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("page", "0")
                            .param("size", "3")
                            .param("sort", "id,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements", is(5)))
                    .andExpect(jsonPath("$.totalPages", is(2)))
                    .andExpect(jsonPath("$.content[0].name", is("Нон (Лепешка)")));
        }

        @Test
        void getAllProducts_whenAnonymous_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isUnauthorized());
        }
    }


    @Nested
    @DisplayName("Тесты для GET /api/v1/products/{id}")
    class GetProductByIdTests {

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void getProductById_whenProductExists_shouldReturnProduct() throws Exception {
            mockMvc.perform(get("/api/v1/products/302")) // Рис "Лазер"
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(302)))
                    .andExpect(jsonPath("$.name", is("Рис \"Лазер\"")))
                    .andExpect(jsonPath("$.categoryId", is(203)));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void getProductById_whenProductNotExists_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/products/999"))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    @DisplayName("Тесты для POST /api/v1/products")
    class CreateProductTests {

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void createProduct_whenValidRequest_shouldReturnCreatedProduct() throws Exception {
            ProductDto newProduct = new ProductDto();
            newProduct.setName("Новый Тестовый Продукт");
            newProduct.setProductCode("NTP-001");
            newProduct.setUnit(UnitType.PIECE);
            newProduct.setSellingPrice(new BigDecimal("999.99"));
            newProduct.setCategoryId(201L);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newProduct)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name", is("Новый Тестовый Продукт")))
                    .andExpect(jsonPath("$.categoryName", is("Молочные продукты")));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void createProduct_whenCategoryNotExists_shouldReturnNotFound() throws Exception {
            ProductDto newProduct = new ProductDto();
            newProduct.setName("Продукт с несуществующей категорией");
            newProduct.setProductCode("NTP-002");
            newProduct.setUnit(UnitType.PIECE);
            newProduct.setCategoryId(999L);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newProduct)))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    @DisplayName("Тесты для PUT /api/v1/products")
    class UpdateProductTests {

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void updateProduct_whenProductExists_shouldReturnUpdatedProduct() throws Exception {
            ProductDto updatedProduct = new ProductDto();
            updatedProduct.setId(301L);
            updatedProduct.setName("Лепешка (обновленная)");
            updatedProduct.setSellingPrice(new BigDecimal("3500.00"));
            updatedProduct.setCategoryId(202L);

            mockMvc.perform(put("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(301)))
                    .andExpect(jsonPath("$.name", is("Лепешка (обновленная)")))
                    .andExpect(jsonPath("$.sellingPrice", is(3500.00)));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void updateProduct_whenChangingCategory_shouldUpdateCategory() throws Exception {
            ProductDto updatedProduct = new ProductDto();
            updatedProduct.setId(301L);
            updatedProduct.setName("Лепешка в новой категории");
            updatedProduct.setCategoryId(204L);

            mockMvc.perform(put("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(301)))
                    .andExpect(jsonPath("$.categoryId", is(204)))
                    .andExpect(jsonPath("$.categoryName", is("Напитки")));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void updateProduct_whenProductNotExists_shouldReturnNotFound() throws Exception {
            ProductDto updatedProduct = new ProductDto();
            updatedProduct.setId(999L);
            updatedProduct.setName("Несуществующий продукт");

            mockMvc.perform(put("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedProduct)))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    @DisplayName("Тесты для DELETE /api/v1/products/{id}")
    class DeleteProductTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteProduct_whenProductExistsAndNotUsed_shouldReturnNoContent() throws Exception {

            mockMvc.perform(delete("/api/v1/products/305"))
                    .andExpect(status().isNoContent());

            assertFalse(productRepository.existsById(305L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void deleteProduct_whenProductIsInUse_shouldFail() throws Exception {
            mockMvc.perform(delete("/api/v1/products/301"))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteProduct_whenProductNotExists_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/products/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/products/search")
    class SearchProductsTests {

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void searchProductsByName_whenMatchExists_shouldReturnPagedResult() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("name", "Лепешка"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Нон (Лепешка)")));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void searchProductsByName_whenMultipleMatches_shouldReturnAll() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("name", "1л"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", containsString("Молоко")));
        }
    }
}