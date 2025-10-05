package com.apetitto.apetittoerpbackend.erp.warehouse.repository;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
