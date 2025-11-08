package com.apetitto.apetittoerpbackend.erp.user.controller;

import com.apetitto.apetittoerpbackend.erp.user.controller.api.UserApi;
import com.apetitto.apetittoerpbackend.erp.user.dto.PasswordResetRequestDto;
import com.apetitto.apetittoerpbackend.erp.user.dto.UserDto;
import com.apetitto.apetittoerpbackend.erp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<Page<UserDto>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @Override
    public ResponseEntity<UserDto> getUserById(Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<UserDto> updateUser(UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(userDto));
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> resetPassword(Long id, PasswordResetRequestDto dto) {
        userService.resetPassword(id, dto.getNewPassword());
        return ResponseEntity.ok().build();
    }
}