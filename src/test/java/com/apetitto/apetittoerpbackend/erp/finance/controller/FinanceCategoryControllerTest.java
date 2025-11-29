package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceCategoryDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceCategory;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@DisplayName("API Финансов: Категории")
class FinanceCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FinanceCategoryRepository categoryRepository;

    @Nested
    @DisplayName("Управление категориями (Admin/FinOfficer)")
    class ManagementTests {

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        void createCategory_shouldSucceed() throws Exception {
            FinanceCategoryDto dto = new FinanceCategoryDto();
            dto.setName("Новая статья расходов");
            dto.setType("EXPENSE");
            dto.setDescription("Тестовое описание");

            mockMvc.perform(post("/api/v1/finance/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name", is("Новая статья расходов")))
                    .andExpect(jsonPath("$.type", is("EXPENSE")));
        }

        @Test
        @WithMockUser(roles = "USER")
        void createCategory_whenUser_shouldReturnForbidden() throws Exception {
            FinanceCategoryDto dto = new FinanceCategoryDto();
            dto.setName("Hacker Category");

            mockMvc.perform(post("/api/v1/finance/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createSubCategory_shouldSucceed() throws Exception {
            FinanceCategory parent = new FinanceCategory();
            parent.setName("Родительская");
            parent.setType("INCOME");
            parent.setIsActive(true);
            parent = categoryRepository.save(parent);

            mockMvc.perform(post("/api/v1/finance/categories/" + parent.getId() + "/subcategories")
                            .param("name", "Дочерняя"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Дочерняя")))
                    .andExpect(jsonPath("$.categoryId", is(parent.getId().intValue())));
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        void deleteCategory_shouldPerformSoftDelete() throws Exception {
            FinanceCategory category = new FinanceCategory();
            category.setName("Удаляемая");
            category.setType("EXPENSE");
            category.setIsActive(true);
            category = categoryRepository.save(category);

            mockMvc.perform(delete("/api/v1/finance/categories/" + category.getId()))
                    .andExpect(status().isOk());

            FinanceCategory deleted = categoryRepository.findById(category.getId()).orElseThrow();
            assertFalse(deleted.getIsActive(), "Категория должна быть помечена как неактивная");
        }
    }

    @Nested
    @DisplayName("Просмотр категорий")
    class ReadTests {

        @Test
        @WithMockUser
        void getAllCategories_shouldReturnOnlyActive() throws Exception {
            FinanceCategory active = new FinanceCategory();
            active.setName("Active Cat");
            active.setType("INCOME");
            active.setIsActive(true);
            categoryRepository.save(active);

            FinanceCategory inactive = new FinanceCategory();
            inactive.setName("Inactive Cat");
            inactive.setType("INCOME");
            inactive.setIsActive(false);
            categoryRepository.save(inactive);

            mockMvc.perform(get("/api/v1/finance/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].name", hasItem("Active Cat")))
                    .andExpect(jsonPath("$[*].name", not(hasItem("Inactive Cat"))));
        }
    }
}