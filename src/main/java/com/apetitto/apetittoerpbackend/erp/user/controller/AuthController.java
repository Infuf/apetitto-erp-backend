package com.apetitto.apetittoerpbackend.erp.user.controller;

import com.apetitto.apetittoerpbackend.erp.user.controller.api.AuthApi;
import com.apetitto.apetittoerpbackend.erp.user.dto.JwtResponseDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.LoginRequestDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.SignupRequestDto;
import com.apetitto.apetittoerpbackend.erp.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<JwtResponseDto> authenticateUser(LoginRequestDto loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<?> registerUser(SignupRequestDto signUpRequest) {
        String message = authService.register(signUpRequest);
        return ResponseEntity.ok(message);
    }
}