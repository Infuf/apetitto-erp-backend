package com.apetitto.apetittoerpbackend.erp.finance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancellationRequestDto {
    @NotBlank(message = "Clarify cancellation reason")
    private String reason;
}
