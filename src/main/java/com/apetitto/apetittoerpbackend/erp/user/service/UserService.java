package com.apetitto.apetittoerpbackend.erp.user.service;

import com.apetitto.apetittoerpbackend.erp.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserDto> getUsers(Pageable pageable);

    UserDto getUserById(Long userId);

    UserDto updateUser(UserDto userDto);

    void deleteUser(Long userId);

    void resetPassword(Long userId, String newPassword);
}