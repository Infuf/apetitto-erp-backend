package com.apetitto.apetittoerpbackend.erp.finance.repository.specification;

import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceAccount;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import org.springframework.data.jpa.domain.Specification;

public class FinanceAccountSpecifications {

    public static Specification<FinanceAccount> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<FinanceAccount> hasType(FinanceAccountType type) {
        return (root, query, cb) -> type
                == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<FinanceAccount> accessibleByUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }
}