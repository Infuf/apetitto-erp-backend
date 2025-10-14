package com.apetitto.apetittoerpbackend.erp.warehouse.repository.specification;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockItem;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class StockItemSpecifications {

    public static Specification<StockItem> byWarehouse(Long warehouseId) {
        return (root, query, cb) ->
                cb.equal(root.get("warehouse").get("id"), warehouseId);
    }

    public static Specification<StockItem> byCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(root.join("product", JoinType.INNER).get("category").get("id"), categoryId);
        };
    }

    public static Specification<StockItem> bySearchQuery(String searchQuery) {
        return (root, query, cb) -> {
            if (searchQuery == null || searchQuery.isBlank()) return cb.conjunction();
            var productJoin = root.join("product", JoinType.INNER);
            String pattern = "%" + searchQuery.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(productJoin.get("name")), pattern),
                    cb.like(cb.lower(productJoin.get("productCode")), pattern),
                    cb.like(cb.lower(cb.coalesce(productJoin.get("barcode"), "")), pattern)
            );
        };
    }

    public static Specification<StockItem> excludeZeroQuantity(boolean showZeroQuantity) {
        return (root, query, cb) -> {
            if (showZeroQuantity) return cb.conjunction();
            return cb.greaterThan(root.get("quantity"), 0L);
        };
    }

    public static Specification<StockItem> fetchProduct() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("product", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}
