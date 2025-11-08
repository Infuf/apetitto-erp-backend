package com.apetitto.apetittoerpbackend.erp.user.dto;

import lombok.Data;

@Data
public class PasswordResetRequestDto {
    private String newPassword;
}