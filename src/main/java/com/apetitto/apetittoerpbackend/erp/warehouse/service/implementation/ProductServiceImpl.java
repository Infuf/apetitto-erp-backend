package com.apetitto.apetittoerpbackend.erp.warehouse.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.ProductDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.mapper.ProductMapper;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.ProductRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.CategoryService;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        if (productDto.getId() == null) {
            throw new InvalidRequestException("Product ID must exists");
        }
        var existingProduct = productRepository.findById(productDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productDto.getId() + " not found"));

        productMapper.updateEntityFromDto(productDto, existingProduct);

        if (productDto.getCategoryId() != null &&
                !productDto.getCategoryId().equals(existingProduct.getCategory().getId())) {
            var category = categoryService.findCategoryEntityById(productDto.getCategoryId());
            existingProduct.setCategory(category);
        }

        var updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        if (productDto.getId() != null) {
            throw new InvalidRequestException("While creating product ID must be NULL");
        }
        if (productDto.getCategoryId() == null) {
            throw new InvalidRequestException("Category must not be NULL");
        }

        var category = categoryService.findCategoryEntityById(productDto.getCategoryId());

        var product = productMapper.toEntity(productDto);
        product.setCategory(category);

        var savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }


    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID not found " + id));
        return productMapper.toDto(product);
    }


    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product ID not found " + id);
        }
        productRepository.deleteById(id);
    }
}
