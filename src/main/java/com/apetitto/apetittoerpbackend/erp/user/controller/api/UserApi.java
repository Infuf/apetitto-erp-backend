package com.apetitto.apetittoerpbackend.erp.user.controller.api;

import com.apetitto.apetittoerpbackend.erp.user.dto.PasswordResetRequestDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Администрирование: Пользователи", description = "API для управления пользователями системы")
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public interface UserApi {

    @Operation(summary = "Получение списка всех пользователей")
    @GetMapping
    ResponseEntity<Page<UserDto>> getUsers(Pageable pageable);

    @Operation(summary = "Получение пользователя по ID")
    @GetMapping("/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable Long id);

    @Operation(summary = "Обновление данных пользователя")
    @PutMapping
    ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto);

    @Operation(summary = "Удаление пользователя")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);

    @Operation(summary = "Сброс пароля пользователя администратором")
    @PostMapping("/{id}/reset-password")
    ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordResetRequestDto dto);
}