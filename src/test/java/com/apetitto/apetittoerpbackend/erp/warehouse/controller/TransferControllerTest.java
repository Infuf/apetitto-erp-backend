package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.common.annotation.IntegrationTest;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@DisplayName("Интеграционные тесты для TransferController")
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final long SOURCE_WAREHOUSE_ID = 101L;
    private static final long DEST_WAREHOUSE_ID = 102L;
    private static final long PRODUCT_ID_RICE = 302L;
    private static final long PRODUCT_ID_MILK = 303L;

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Полный жизненный цикл перемещения ('Happy Path')")
    void fullTransferLifecycle_shouldWorkCorrectly() throws Exception {

        TransferOrderRequestDto.Item riceItem = new TransferOrderRequestDto.Item();
        riceItem.setProductId(PRODUCT_ID_RICE);
        riceItem.setQuantity(new BigDecimal("100.5"));

        TransferOrderRequestDto.Item milkItem = new TransferOrderRequestDto.Item();
        milkItem.setProductId(PRODUCT_ID_MILK);
        milkItem.setQuantity(new BigDecimal("50"));

        TransferOrderRequestDto createRequest = new TransferOrderRequestDto();
        createRequest.setSourceWarehouseId(SOURCE_WAREHOUSE_ID);
        createRequest.setDestinationWarehouseId(DEST_WAREHOUSE_ID);
        createRequest.setItems(List.of(riceItem, milkItem));

        MvcResult createResult = mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.sourceWarehouseId", is((int) SOURCE_WAREHOUSE_ID)))
                .andExpect(jsonPath("$.destinationWarehouseId", is((int) DEST_WAREHOUSE_ID)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Integer transferId = objectMapper.readTree(responseBody).get("id").asInt();

        mockMvc.perform(get("/api/v1/warehouse/stock?warehouseId=" + SOURCE_WAREHOUSE_ID + "&searchQuery=Рис"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity", is(1000.0)));


        mockMvc.perform(post("/api/v1/transfers/{id}/ship", transferId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")));

        mockMvc.perform(get("/api/v1/warehouse/stock?warehouseId=" + SOURCE_WAREHOUSE_ID + "&searchQuery=Рис"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity", is(899.5)));


        mockMvc.perform(post("/api/v1/transfers/{id}/receive", transferId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RECEIVED")));

        mockMvc.perform(get("/api/v1/warehouse/stock?warehouseId=" + DEST_WAREHOUSE_ID + "&searchQuery=Рис"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity", is(100.5)))
                .andExpect(jsonPath("$.content[0].averageCost", is(18.0000)));
        mockMvc.perform(get("/api/v1/warehouse/stock?warehouseId=" + DEST_WAREHOUSE_ID + "&searchQuery=Молоко"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity", is(50.0)))
                .andExpect(jsonPath("$.content[0].averageCost", is(7000.0000)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Попытка отправить заказ с недостаточным количеством товара")
    void shipTransfer_whenNotEnoughStock_shouldReturnBadRequest() throws Exception {
        TransferOrderRequestDto.Item riceItem = new TransferOrderRequestDto.Item();
        riceItem.setProductId(PRODUCT_ID_RICE);
        riceItem.setQuantity(new BigDecimal("9999"));

        TransferOrderRequestDto createRequest = new TransferOrderRequestDto();
        createRequest.setSourceWarehouseId(SOURCE_WAREHOUSE_ID);
        createRequest.setDestinationWarehouseId(DEST_WAREHOUSE_ID);
        createRequest.setItems(List.of(riceItem));

        MvcResult createResult = mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer transferId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(post("/api/v1/transfers/{id}/ship", transferId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Not enough goods")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Попытка выполнить действия в неправильном порядке")
    void transferActions_whenWrongStatus_shouldReturnBadRequest() throws Exception {
        TransferOrderRequestDto.Item milkItem = new TransferOrderRequestDto.Item();
        milkItem.setProductId(PRODUCT_ID_MILK);
        milkItem.setQuantity(new BigDecimal("10"));
        TransferOrderRequestDto createRequest = new TransferOrderRequestDto();
        createRequest.setSourceWarehouseId(SOURCE_WAREHOUSE_ID);
        createRequest.setDestinationWarehouseId(DEST_WAREHOUSE_ID);
        createRequest.setItems(List.of(milkItem));
        MvcResult createResult = mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer transferId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(post("/api/v1/transfers/{id}/receive", transferId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Only orders with the SHIPPED status can be accepted")));

        mockMvc.perform(post("/api/v1/transfers/{id}/ship", transferId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/transfers/{id}/ship", transferId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("You can only send orders with the PENDING status.")));
    }
}