package com.apetitto.apetittoerpbackend.erp.finance.repository.specification;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceTransaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class FinanceTransactionSpecifications {


    public static Specification<FinanceTransaction> hasAccount(Long accountId) {
        return (root, query, cb) -> {
            if (accountId == null) return cb.conjunction();

            return cb.or(
                    cb.equal(root.get("fromAccount").get("id"), accountId),
                    cb.equal(root.get("toAccount").get("id"), accountId)
            );
        };
    }

    public static Specification<FinanceTransaction> dateBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();

            if (from != null && to != null) {
                return cb.between(root.get("transactionDate"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("transactionDate"), from);
            } else {
                return cb.lessThanOrEqualTo(root.get("transactionDate"), to);
            }
        };
    }
}