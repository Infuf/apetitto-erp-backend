package com.apetitto.apetittoerpbackend.erp.user.controller.api;

import com.apetitto.apetittoerpbackend.erp.user.dto.JwtResponseDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.LoginRequestDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.SignupRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Аутентификация", description = "API для регистрации и входа в систему")
@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @Operation(summary = "Вход в систему")
    @PostMapping("/login")
    ResponseEntity<JwtResponseDto> authenticateUser(@RequestBody LoginRequestDto loginRequest);

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    ResponseEntity<?> registerUser(@RequestBody SignupRequestDto signUpRequest);
}