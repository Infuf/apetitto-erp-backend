package com.apetitto.apetittoerpbackend.erp.finance.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.AccessDeniedException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceAccountDto;
import com.apetitto.apetittoerpbackend.erp.finance.mapper.FinanceAccountMapper;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceAccountService;
import com.apetitto.apetittoerpbackend.erp.user.model.User;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apetitto.apetittoerpbackend.erp.finance.repository.specification.FinanceAccountSpecifications.*;

@Service
@RequiredArgsConstructor
public class FinanceAccountServiceImpl implements FinanceAccountService {

    private final FinanceAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final FinanceAccountMapper accountMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public FinanceAccountDto createAccount(FinanceAccountDto dto) {
        FinanceAccount account = accountMapper.toEntity(dto);
        account.setIsActive(true);

        if (dto.getUserId() != null) {
            account.setUser(userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        }

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public FinanceAccountDto updateAccount(Long id, FinanceAccountDto dto) {
        FinanceAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account NOT found"));

        accountMapper.updateEntityFromDto(dto, account);
        return accountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        FinanceAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account NOT found"));
        account.setIsActive(false);
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceAccountDto> getAccounts(FinanceAccountType type) {

        var spec = isActive();

        if (type != null) {
            spec = spec.and(hasType(type));
        }
        return accountRepository.findAll(spec).stream()
                .map(accountMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceAccountDto getAccountById(Long id) {
        FinanceAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account NOT found"));

        validateAccess(account);

        return accountMapper.toDto(account);
    }

    @Override
    @Transactional
    public FinanceAccountDto assignUserToAccount(Long accountId, Long userId) {
        FinanceAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account NOT found"));

        if (userId == null) {
            account.setUser(null);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User NOT found"));
            account.setUser(user);
        }
        return accountMapper.toDto(accountRepository.save(account));
    }

    private void validateAccess(FinanceAccount account) {
        User user = getCurrentUser();
        boolean isGlobalViewer = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN") ||
                        r.getName().equals("ROLE_OWNER") ||
                        r.getName().equals("ROLE_FINANCE_OFFICER"));

        if (isGlobalViewer) return;
        if (account.getUser() == null) {
            throw new AccessDeniedException("Access Denied: This account belongs to another user.");
        } else if (!account.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access Denied: This account belongs to another user.");
        }
    }
}