package com.apetitto.apetittoerpbackend.erp.finance.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionDetailDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import com.apetitto.apetittoerpbackend.erp.finance.mapper.FinanceTransactionMapper;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransactionItem;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceAccountRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceSubCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceTransactionRepository;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceTransactionService;
import com.apetitto.apetittoerpbackend.erp.user.repository.UserRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.TransactionStatus.CANCELLED;
import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.TransactionStatus.COMPLETED;
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

    @Value("${app.finance.cancellation-window-hours}")
    private int cancellationWindowHours;

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

    @Override
    @Transactional
    public void createDebtTransaction(
            Long accountId,
            List<StockMovementRequestDto.Item> items,
            MovementType movementType,
            String description
    ) {
        FinanceAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidRequestException("Account not found: " + accountId));

        boolean isInbound = (movementType == MovementType.INBOUND);
        FinanceOperationType operationType = isInbound ? FinanceOperationType.SUPPLIER_INVOICE :
                FinanceOperationType.DEALER_INVOICE;

        FinanceTransaction trx = new FinanceTransaction();
        trx.setTransactionDate(Instant.now());
        trx.setOperationType(operationType);
        trx.setStatus(COMPLETED);
        trx.setDescription("Авто-расчет: " + description);

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        trx.setCreatedBy(user);


        var totalTransactionAmount = BigDecimal.ZERO;
        List<FinanceTransactionItem> trxItems = new ArrayList<>();

        for (var itemDto : items) {
            var product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemDto.getProductId()));

            BigDecimal finalPrice;

            if (isInbound) {
                if (itemDto.getCostPrice() == null) {
                    throw new InvalidRequestException("No purchase price specified for delivery of " +
                            product.getName() + "!");
                }
                finalPrice = itemDto.getCostPrice();
            } else {
                var basePrice = product.getSellingPrice();

                if (account.getDiscountPercentage() != null &&
                        account.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    var discountFactor = account.getDiscountPercentage().divide(new BigDecimal("100"));
                    var discountAmount = basePrice.multiply(discountFactor);
                    finalPrice = basePrice.subtract(discountAmount);
                } else {
                    finalPrice = basePrice;
                }
            }

            var quantity = itemDto.getQuantity();
            var lineTotal = finalPrice.multiply(quantity);

            totalTransactionAmount = totalTransactionAmount.add(lineTotal);

            FinanceTransactionItem trxItem = new FinanceTransactionItem();
            trxItem.setTransaction(trx);
            trxItem.setProduct(product);
            trxItem.setQuantity(quantity);
            trxItem.setPriceSnapshot(finalPrice);
            trxItem.setTotalAmount(lineTotal);

            trxItems.add(trxItem);
        }

        trx.setAmount(totalTransactionAmount);
        trx.setItems(trxItems);

        if (isInbound) {
            trx.setFromAccount(account);
            account.setBalance(account.getBalance().subtract(totalTransactionAmount));
        } else {
            trx.setToAccount(account);
            account.setBalance(account.getBalance().add(totalTransactionAmount));
        }

        accountRepository.save(account);
        transactionRepository.save(trx);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailDto getTransactionById(Long id) {
        var transaction = transactionRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with ID " + id + " not found"));

        return transactionMapper.toDetailDto(transaction);
    }

    private void handleAccountsAndBalances(FinanceTransaction trx, Long fromId, Long toId) {
        FinanceAccount fromAccount = null;
        FinanceAccount toAccount = null;

        if (fromId != null) {
            fromAccount = accountRepository.findById(fromId)
                    .orElseThrow(() -> new ResourceNotFoundException("Source Account (From) not found with ID: " + fromId));

            var newBalance = fromAccount.getBalance().subtract(trx.getAmount());

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                if (fromAccount.getType() == FinanceAccountType.BANK
                        || fromAccount.getType() == FinanceAccountType.CASHBOX) {
                    throw new InvalidRequestException("Not money exception in account: " + fromAccount.getName());
                }
            }
            fromAccount.setBalance(newBalance);
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
                if (from == null) {
                    throw new InvalidRequestException("Source account is required for EXPENSE/PAYOUT operations.");
                }
                if (to != null) {
                    throw new InvalidRequestException("Destination account must be empty for EXPENSE operations.");
                }
                break;
            case SALARY_PAYOUT:
                if (from == null || (from.getType() != FinanceAccountType.CASHBOX && from.getType() != FinanceAccountType.BANK)) {
                    throw new InvalidRequestException("A real money account (Cashier/Bank) is required for payment.");
                }
                if (to == null || to.getType() != FinanceAccountType.EMPLOYEE) {
                    throw new InvalidRequestException("The recipient of the payment must be the employee's account (EMPLOYEE).");
                }
                break;
            case SALARY_ACCRUAL:
                if (from == null || from.getType() != FinanceAccountType.EMPLOYEE) {
                    throw new InvalidRequestException("To calculate payroll, you need an employee account (From) of type EMPLOYEE.");
                }
                if (to != null) {
                    throw new InvalidRequestException("When calculating payroll, the ‘Where’ field must be left blank.");
                }
                break;
            case OWNER_WITHDRAW:
                if (from == null) {
                    throw new InvalidRequestException("You must specify the cash register from which the money is taken.");
                }
                if (to == null) {
                    throw new InvalidRequestException("You must specify the owner's personal account (where).");
                }
                if (to.getType() != FinanceAccountType.OWNER) {
                    throw new InvalidRequestException("The recipient's account must be of type OWNER.");
                }
                break;
            default:
                break;
        }
    }

    @Override
    @Transactional
    public void cancelTransaction(Long id, String reason) {
        var trx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (trx.getStatus() == CANCELLED) {
            throw new InvalidRequestException("This transaction is already CANCELLED");
        }

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isSuperUser = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isSuperUser) {
            long hoursPassed = Duration.between(trx.getCreatedAt(), Instant.now()).toHours();
            if (hoursPassed > cancellationWindowHours) {
                throw new InvalidRequestException(
                        "Cancellation period has expired (" + cancellationWindowHours + " h). Contact the Administrator.");
            }
        }

        if (trx.getFromAccount() != null) {
            var from = trx.getFromAccount();
            from.setBalance(from.getBalance().add(trx.getAmount()));
            accountRepository.save(from);
        }

        if (trx.getToAccount() != null) {
            var to = trx.getToAccount();
            to.setBalance(to.getBalance().subtract(trx.getAmount()));
            accountRepository.save(to);
        }

        trx.setStatus(CANCELLED);
        trx.setCancelledBy(currentUser);
        trx.setCancellationReason(reason);

        transactionRepository.save(trx);
    }
}