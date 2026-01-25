package com.apetitto.apetittoerpbackend.erp.hr.repository.specification;

import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class PayrollSpecifications {

    public static Specification<PayrollAccrual> periodBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null || to == null) return cb.conjunction();
            return cb.between(root.get("periodStart"), from, to);
        };
    }

    public static Specification<PayrollAccrual> hasEmployee(Long employeeId) {
        return (root, query, cb) ->
                employeeId == null ? cb.conjunction() : cb.equal(root.get("employee").get("id"), employeeId);
    }

    public static Specification<PayrollAccrual> hasDepartment(Long departmentId) {
        return (root, query, cb) ->
                departmentId == null ? cb.conjunction() : cb.equal(root.get("employee").get("department").get("id"), departmentId);
    }
}