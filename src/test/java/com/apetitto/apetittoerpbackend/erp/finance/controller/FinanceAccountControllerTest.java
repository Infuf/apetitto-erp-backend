package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.finance.dto.AssignOwnerDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceAccountDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("API Финансов: Счета")
class FinanceAccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FinanceAccountRepository accountRepository;
    @Autowired private UserRepository userRepository;

    private User userOrdinary;
    private FinanceAccount accountPersonal;
    private FinanceAccount accountGeneral;

    @BeforeEach
    void setupData() {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        userOrdinary = new User();
        userOrdinary.setUsername("user_test");
        userOrdinary.setPassword("pass");
        userOrdinary = userRepository.save(userOrdinary);
        accountPersonal = new FinanceAccount();
        accountPersonal.setName("Личный сейф");
        accountPersonal.setType(FinanceAccountType.CASHBOX);
        accountPersonal.setBalance(new BigDecimal("1000.00"));
        accountPersonal.setUser(userOrdinary);
        accountPersonal.setIsActive(true);
        accountPersonal = accountRepository.save(accountPersonal);

        accountGeneral = new FinanceAccount();
        accountGeneral.setName("Общая касса");
        accountGeneral.setType(FinanceAccountType.CASHBOX);
        accountGeneral.setBalance(new BigDecimal("50000.00"));
        accountGeneral.setIsActive(true);
        accountGeneral = accountRepository.save(accountGeneral);
    }

    @Nested
    @DisplayName("Проверка видимости (Security)")
    class VisibilityTests {

        @Test
        @WithMockUser(username = "user_test", roles = "USER")
        void getAccounts_whenOrdinaryUser_shouldReturnedForbitten() throws Exception {
            mockMvc.perform(get("/api/v1/finance/accounts"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        void getAccounts_whenFinanceOfficer_shouldSeeAll() throws Exception {
            mockMvc.perform(get("/api/v1/finance/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)));
        }

        @Test
        @WithMockUser(username = "user_test", roles = "USER")
        void getAccountById_whenOwnAccount_shouldSucceed() throws Exception {
            mockMvc.perform(get("/api/v1/finance/accounts/" + accountPersonal.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance", is(1000.0)));
        }

        @Test
        @WithMockUser(username = "user_test", roles = "USER")
        void getAccountById_whenGeneralAccount_shouldReturnForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/finance/accounts/" + accountGeneral.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Операции изменения (CRUD)")
    class CrudTests {

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        void createAccount_shouldSucceed() throws Exception {
            FinanceAccountDto dto = new FinanceAccountDto();
            dto.setName("Новый Поставщик");
            dto.setType(FinanceAccountType.SUPPLIER);

            mockMvc.perform(post("/api/v1/finance/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.type", is("SUPPLIER")));
        }

        @Test
        @WithMockUser(roles = "USER")
        void createAccount_whenUser_shouldReturnForbidden() throws Exception {
            FinanceAccountDto dto = new FinanceAccountDto();
            dto.setName("Hacker Account");
            dto.setType(FinanceAccountType.CASHBOX);

            mockMvc.perform(post("/api/v1/finance/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void assignUser_shouldUpdateOwnership() throws Exception {
            AssignOwnerDto dto = new AssignOwnerDto();
            dto.setUserId(userOrdinary.getId());

            mockMvc.perform(put("/api/v1/finance/accounts/" + accountGeneral.getId() + "/assign-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is(userOrdinary.getId().intValue())));

            FinanceAccount updated = accountRepository.findById(accountGeneral.getId()).orElseThrow();
            assertEquals(userOrdinary.getId(), updated.getUser().getId());
        }

        @Test
        @WithMockUser(roles = "FINANCE_OFFICER")
        void assignUser_whenFinanceOfficer_shouldReturnForbidden() throws Exception {
            AssignOwnerDto dto = new AssignOwnerDto();
            dto.setUserId(userOrdinary.getId());

            mockMvc.perform(put("/api/v1/finance/accounts/" + accountGeneral.getId() + "/assign-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }
}