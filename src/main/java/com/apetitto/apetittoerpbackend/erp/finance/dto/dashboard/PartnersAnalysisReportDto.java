package com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PartnersAnalysisReportDto {

    private BigDecimal grandTotalAmount;
    private BigDecimal grandTotalQuantity;

    private List<PartnerDto> partners;

    @Data
    @Builder
    public static class PartnerDto {
        private Long partnerId;
        private String partnerName;

        private BigDecimal totalAmount;
        private BigDecimal totalQuantity;
        private BigDecimal shareInGrandTotal;

        private List<ProductDto> products;
    }

    @Data
    @Builder
    public static class ProductDto {
        private String productName;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal amount;
        private BigDecimal averagePrice;
    }
}