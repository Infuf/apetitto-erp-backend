package com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerProductFlatDto {
    private Long partnerId;
    private String partnerName;

    private String productName;
    private String productUnit;

    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
}