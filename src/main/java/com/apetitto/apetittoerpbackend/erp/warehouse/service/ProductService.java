package com.apetitto.apetittoerpbackend.erp.warehouse.service;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.ProductDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductDto createProduct(ProductDto productDto);


    Page<ProductDto> getAllProducts(Pageable pageable);

    ProductDto getProductById(Long id);

    ProductDto updateProduct(ProductDto productDto);

    void deleteProduct(Long id);

    Page<ProductDto> searchProductsByName(String name, Pageable pageable);

    Product findProductEntityById(Long id);
}
