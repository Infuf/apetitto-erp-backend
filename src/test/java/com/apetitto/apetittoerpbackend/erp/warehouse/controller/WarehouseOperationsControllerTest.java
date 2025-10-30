package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("Интеграционные тесты для WarehouseOperationsController")
class WarehouseOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final long WAREHOUSE_ID = 101L;
    private static final long PRODUCT_ID_RICE = 302L;
    private static final long PRODUCT_ID_MILK = 303L;


    @Nested
    @DisplayName("Тесты для GET /stock (Получение остатков)")
    class GetStockTests {

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void getStock_withNoFilters_shouldReturnPagedStock() throws Exception {
            mockMvc.perform(get("/api/v1/warehouse/stock")
                            .param("warehouseId", String.valueOf(WAREHOUSE_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(3)));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void getStock_withSearchQuery_shouldReturnFilteredStock() throws Exception {
            mockMvc.perform(get("/api/v1/warehouse/stock")
                            .param("warehouseId", String.valueOf(WAREHOUSE_ID))
                            .param("searchQuery", "Рис"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].productId", is((int) PRODUCT_ID_RICE)));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void getStock_withCategoryFilter_shouldReturnFilteredStock() throws Exception {
            mockMvc.perform(get("/api/v1/warehouse/stock")
                            .param("warehouseId", String.valueOf(WAREHOUSE_ID))
                            .param("categoryId", "202"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].productName", is("Нон (Лепешка)")));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        void getStock_withShowZeroQuantity_shouldNotShowZero() throws Exception {
            StockMovementRequestDto outboundRequest = new StockMovementRequestDto();
            outboundRequest.setWarehouseId(WAREHOUSE_ID);
            outboundRequest.setMovementType(MovementType.OUTBOUND);
            StockMovementRequestDto.Item milkOut = new StockMovementRequestDto.Item();
            milkOut.setProductId(PRODUCT_ID_MILK);
            milkOut.setQuantity(new BigDecimal("200"));
            outboundRequest.setItems(List.of(milkOut));
            mockMvc.perform(post("/api/v1/warehouse/movements")
                    .with(request -> {
                        request.setMethod("POST");
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(outboundRequest)));

            mockMvc.perform(get("/api/v1/warehouse/stock")
                            .param("warehouseId", String.valueOf(WAREHOUSE_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(2)));
        }
    }


    @Nested
    @DisplayName("Тесты для POST /movements (Движения)")
    class ProcessMovementTests {

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        @DisplayName("INBOUND: Корректный расчет WAC при поступлении")
        void processInbound_shouldUpdateQuantityAndAverageCost() throws Exception {
            StockMovementRequestDto.Item riceIn = new StockMovementRequestDto.Item();
            riceIn.setProductId(PRODUCT_ID_RICE);
            riceIn.setQuantity(new BigDecimal("500"));
            riceIn.setCostPrice(new BigDecimal("20000"));

            StockMovementRequestDto request = new StockMovementRequestDto();
            request.setWarehouseId(WAREHOUSE_ID);
            request.setMovementType(MovementType.INBOUND);
            request.setItems(List.of(riceIn));

            mockMvc.perform(post("/api/v1/warehouse/movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/warehouse/stock?warehouseId=" + WAREHOUSE_ID + "&searchQuery=Рис"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].quantity", is(1500.0)))
                    .andExpect(jsonPath("$.content[0].averageCost", closeTo(18666.6667, 0.0001)));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        @DisplayName("OUTBOUND: Корректное списание")
        void processOutbound_shouldDecreaseQuantity() throws Exception {

            StockMovementRequestDto.Item milkOut = new StockMovementRequestDto.Item();
            milkOut.setProductId(PRODUCT_ID_MILK);
            milkOut.setQuantity(new BigDecimal("50"));

            StockMovementRequestDto request = new StockMovementRequestDto();
            request.setWarehouseId(WAREHOUSE_ID);
            request.setMovementType(MovementType.OUTBOUND);
            request.setItems(List.of(milkOut));

            mockMvc.perform(post("/api/v1/warehouse/movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());


            mockMvc.perform(get("/api/v1/warehouse/stock?warehouseId=" + WAREHOUSE_ID + "&searchQuery=Молоко"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].quantity", is(150.0)))
                    .andExpect(jsonPath("$.content[0].averageCost", is(7000.0000)));
        }

        @Test
        @WithMockUser(roles = "WAREHOUSE_MANAGER")
        @DisplayName("ADJUSTMENT: Корректировка остатков (недостача)")
        void processAdjustment_withNegativeDelta_shouldDecreaseQuantity() throws Exception {

            StockMovementRequestDto.Item milkAdj = new StockMovementRequestDto.Item();
            milkAdj.setProductId(PRODUCT_ID_MILK);
            milkAdj.setQuantity(new BigDecimal("-10"));

            StockMovementRequestDto request = new StockMovementRequestDto();
            request.setWarehouseId(WAREHOUSE_ID);
            request.setMovementType(MovementType.ADJUSTMENT);
            request.setComment("Инвентаризация: недостача");
            request.setItems(List.of(milkAdj));

            mockMvc.perform(post("/api/v1/warehouse/movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());


            mockMvc.perform(get("/api/v1/warehouse/stock?warehouseId=" + WAREHOUSE_ID + "&searchQuery=Молоко"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].quantity", is(190.0)));
        }

        @Test
        void processMovement_whenAnonymous_shouldReturnUnauthorized() throws Exception {
            StockMovementRequestDto request = new StockMovementRequestDto();
            request.setWarehouseId(WAREHOUSE_ID);
            request.setMovementType(MovementType.INBOUND);
            request.setItems(List.of());

            mockMvc.perform(post("/api/v1/warehouse/movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}