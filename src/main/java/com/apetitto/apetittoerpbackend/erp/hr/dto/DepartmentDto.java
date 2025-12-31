package com.apetitto.apetittoerpbackend.erp.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentDto {
    private Long id;

    @NotBlank(message = "Название департамента обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    private String description;

    private Long managerId;

    private String managerName;
}