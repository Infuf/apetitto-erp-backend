package com.apetitto.apetittoerpbackend.erp.finance.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import com.apetitto.apetittoerpbackend.erp.finance.mapper.FinanceTransactionMapper;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransactionItem;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceSubCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceTransactionService;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;

import static com.apetitto.apetittoerpbackend.erp.finance.repository.specification.FinanceTransactionSpecifications.dateBetween;
import static com.apetitto.apetittoerpbackend.erp.finance.repository.specification.FinanceTransactionSpecifications.hasAccount;

@Service
@RequiredArgsConstructor
public class FinanceTransactionServiceImpl implements FinanceTransactionService {

    private final FinanceTransactionRepository transactionRepository;
    private final FinanceAccountRepository accountRepository;
    private final FinanceCategoryRepository categoryRepository;
    private final FinanceSubCategoryRepository subCategoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FinanceTransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionResponseDto createTransaction(TransactionCreateRequestDto request) {
        var trx = transactionMapper.toEntity(request);

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        trx.setCreatedBy(user);

        if (request.getCategoryId() != null) {
            trx.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId())));
        }
        if (request.getSubcategoryId() != null) {
            trx.setSubCategory(subCategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with ID: " + request.getSubcategoryId())));
        }

        handleAccountsAndBalances(trx, request.getFromAccountId(), request.getToAccountId());

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            trx.setItems(new ArrayList<>());
            for (var itemDto : request.getItems()) {
                FinanceTransactionItem item = transactionMapper.toItemEntity(itemDto);

                item.setProduct(productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDto.getProductId())));

                item.setTotalAmount(item.getQuantity().multiply(item.getPriceSnapshot()));

                item.setTransaction(trx);
                trx.getItems().add(item);
            }
        }

        FinanceTransaction savedTrx = transactionRepository.save(trx);
        return transactionMapper.toDto(savedTrx);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getTransactions(Long accountId, Instant dateFrom, Instant dateTo, Pageable pageable) {
        var spec = hasAccount(accountId).and(dateBetween(dateFrom, dateTo));

        return transactionRepository.findAll(spec, pageable)
                .map(transactionMapper::toDto);
    }

    private void handleAccountsAndBalances(FinanceTransaction trx, Long fromId, Long toId) {
        FinanceAccount fromAccount = null;
        FinanceAccount toAccount = null;

        if (fromId != null) {
            fromAccount = accountRepository.findById(fromId)
                    .orElseThrow(() -> new ResourceNotFoundException("Source Account (From) not found with ID: " + fromId));

            fromAccount.setBalance(fromAccount.getBalance().subtract(trx.getAmount()));
            accountRepository.save(fromAccount);
            trx.setFromAccount(fromAccount);
        }

        if (toId != null) {
            toAccount = accountRepository.findById(toId)
                    .orElseThrow(() -> new ResourceNotFoundException("Destination Account (To) not found with ID: " + toId));

            toAccount.setBalance(toAccount.getBalance().add(trx.getAmount()));
            accountRepository.save(toAccount);
            trx.setToAccount(toAccount);
        }

        validateOperationType(trx, fromAccount, toAccount);
    }

    private void validateOperationType(FinanceTransaction trx, FinanceAccount from, FinanceAccount to) {
        switch (trx.getOperationType()) {
            case TRANSFER:
            case PAYMENT_TO_SUPP:
            case PAYMENT_FROM_DLR:
                if (from == null || to == null) {
                    throw new InvalidRequestException("Both Source and Destination accounts are required for this operation type.");
                }
                break;
            case INCOME:
            case DEALER_INVOICE:
                if (to == null) {
                    throw new InvalidRequestException("Destination account is required for INCOME/INVOICE operations.");
                }
                if (from != null) {
                    throw new InvalidRequestException("Source account must be empty for INCOME operations.");
                }
                break;
            case EXPENSE:
            case SUPPLIER_INVOICE:
            case SALARY_PAYOUT:
                if (from == null) {
                    throw new InvalidRequestException("Source account is required for EXPENSE/PAYOUT operations.");
                }
                if (to != null) {
                    throw new InvalidRequestException("Destination account must be empty for EXPENSE operations.");
                }
                break;
            default:
                break;
        }
    }
}