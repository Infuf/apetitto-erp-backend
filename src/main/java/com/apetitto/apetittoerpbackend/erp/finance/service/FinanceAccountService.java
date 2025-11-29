package com.apetitto.apetittoerpbackend.erp.finance.service;

import com.apetitto.apetittoerpbackend.erp.finance.dto.AssignOwnerDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceAccountDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import java.util.List;

public interface FinanceAccountService {

    FinanceAccountDto createAccount(FinanceAccountDto dto);

    FinanceAccountDto updateAccount(Long id, FinanceAccountDto dto);

    void deleteAccount(Long id);

    FinanceAccountDto getAccountById(Long id);

    List<FinanceAccountDto> getAccounts(FinanceAccountType type);

    FinanceAccountDto assignUserToAccount(Long accountId, Long userId);
}