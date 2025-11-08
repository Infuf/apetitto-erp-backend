package com.apetitto.apetittoerpbackend.erp.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class SignupRequestDto {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Set<String> roles;
}