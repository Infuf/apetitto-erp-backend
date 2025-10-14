package com.apetitto.apetittoerpbackend.erp.user.service;

import com.apetitto.apetittoerpbackend.erp.user.dto.JwtResponseDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.LoginRequestDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.SignupRequestDto;

public interface AuthService {
    JwtResponseDto login(LoginRequestDto loginRequest);

    String register(SignupRequestDto signUpRequest);
}
