package com.apetitto.apetittoerpbackend.erp.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private Instant timestamp;
    private int status;
    private String error;
    private String path;
}
