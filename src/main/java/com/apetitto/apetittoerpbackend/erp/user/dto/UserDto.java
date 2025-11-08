package com.apetitto.apetittoerpbackend.erp.user.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private Long warehouseId;
    private String warehouseName;
    private Set<String> roles;
}