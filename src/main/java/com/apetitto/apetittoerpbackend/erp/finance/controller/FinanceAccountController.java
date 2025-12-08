package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.finance.controller.api.FinanceAccountApi;
import com.apetitto.apetittoerpbackend.erp.finance.dto.AssignOwnerDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceAccountDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FinanceAccountController implements FinanceAccountApi {

    private final FinanceAccountService accountService;

    @Override
    public ResponseEntity<List<FinanceAccountDto>> getAccounts(FinanceAccountType type) {
        return ResponseEntity.ok(accountService.getAccounts(type));
    }

    @Override
    public ResponseEntity<FinanceAccountDto> getAccountById(Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @Override
    public ResponseEntity<FinanceAccountDto> createAccount(FinanceAccountDto dto) {
        return ResponseEntity.ok(accountService.createAccount(dto));
    }

    @Override
    public ResponseEntity<FinanceAccountDto> updateAccount(Long id, FinanceAccountDto dto) {
        return ResponseEntity.ok(accountService.updateAccount(id, dto));
    }

    @Override
    public ResponseEntity<Void> deleteAccount(Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<FinanceAccountDto> assignUser(Long id, AssignOwnerDto dto) {
        return ResponseEntity.ok(accountService.assignUserToAccount(id, dto.getUserId()));
    }
}