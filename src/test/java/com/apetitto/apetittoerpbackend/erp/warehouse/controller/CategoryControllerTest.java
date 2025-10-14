package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.CategoryDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@DisplayName("Интеграционные тесты для CategoryController")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Nested
    @DisplayName("Тесты для GET /api/v1/categories")
    class GetAllCategoriesTests {

        @Test
        @WithMockUser
        void getAllCategories_whenAuthenticated_shouldReturnCategoryList() throws Exception {
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].name", is("Молочные продукты")))
                    .andExpect(jsonPath("$[1].name", is("Хлебобулочные изделия")));
        }

        @Test
        void getAllCategories_whenAnonymous_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/categories/{id}")
    class GetCategoryByIdTests {

        @Test
        @WithMockUser
        void getCategoryById_whenCategoryExists_shouldReturnCategory() throws Exception {
            mockMvc.perform(get("/api/v1/categories/203"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(203)))
                    .andExpect(jsonPath("$.name", is("Бакалея")));
        }

        @Test
        @WithMockUser
        void getCategoryById_whenCategoryNotExists_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/categories/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Тесты для POST /api/v1/categories")
    class CreateCategoryTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void createCategory_whenValidRequest_shouldReturnCreatedCategory() throws Exception {
            CategoryDto newCategory = new CategoryDto();
            newCategory.setName("Новая категория");
            newCategory.setDescription("Описание новой категории");

            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newCategory)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name", is("Новая категория")));
        }

        @Test
        @WithMockUser
        void createCategory_whenDtoHasId_shouldReturnBadRequest() throws Exception {
            CategoryDto newCategory = new CategoryDto();
            newCategory.setId(123L);
            newCategory.setName("Категория с ID");

            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newCategory)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Тесты для PUT /api/v1/categories")
    class UpdateCategoryTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        void updateCategory_whenCategoryExists_shouldReturnUpdatedCategory() throws Exception {
            CategoryDto updatedCategoryDto = new CategoryDto();
            updatedCategoryDto.setId(201L);
            updatedCategoryDto.setName("Обновленное имя");

            mockMvc.perform(put("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedCategoryDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(201)))
                    .andExpect(jsonPath("$.name", is("Обновленное имя")));
        }
    }

    @Nested
    @DisplayName("Тесты для DELETE /api/v1/categories/{id}")
    class DeleteCategoryTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteCategory_whenCategoryExists_shouldReturnNoContent() throws Exception {
            mockMvc.perform(delete("/api/v1/categories/205"))
                    .andExpect(status().isNoContent());

            assertFalse(categoryRepository.existsById(205L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteCategory_whenCategoryNotExists_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/categories/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/v1/categories/search")
    class SearchCategoriesTests {

        @Test
        @WithMockUser
        void searchCategoriesByName_whenMatchExists_shouldReturnFilteredList() throws Exception {
            mockMvc.perform(get("/api/v1/categories/search").param("name", "Продукты"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Молочные продукты")));
        }

        @Test
        @WithMockUser
        void searchCategoriesByName_whenNoMatch_shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/v1/categories/search").param("name", "несуществующееимя"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}