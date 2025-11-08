package com.apetitto.apetittoerpbackend.erp.user.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.user.dto.UserDto;
import com.apetitto.apetittoerpbackend.erp.user.mapper.UserMapper;
import com.apetitto.apetittoerpbackend.erp.user.model.Role;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.RoleRepository;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import com.apetitto.apetittoerpbackend.erp.user.service.UserService;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Warehouse;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userDto.getId() + " not found"));

        userMapper.updateEntityFromDto(userDto, user);

        if (userDto.getRoles() != null) {
            Set<Role> newRoles = userDto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new InvalidRequestException("ROLE '" + roleName + "' not found")))
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }

        if (userDto.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(userDto.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse with ID " + userDto.getWarehouseId() + " not found"));
            user.setWarehouse(warehouse);
        } else {
            user.setWarehouse(null);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidRequestException("New password can't be blank");
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }
}