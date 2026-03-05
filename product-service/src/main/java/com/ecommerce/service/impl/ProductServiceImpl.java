package com.ecommerce.service.impl;

import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.dto.ProductFilterRequest;
import com.ecommerce.model.dto.ProductListResponse;
import com.ecommerce.model.dto.ProductRequest;
import com.ecommerce.model.dto.ProductResponse;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final Clock clock;


    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateCategoryExists(request.getCategoryId());
        Product product = productMapper.toEntity(request);

        LocalDateTime now = LocalDateTime.now(clock);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product savedProduct = productRepository.save(product);
        log.info("Продукт с id {} был создан", savedProduct.getId());
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductById(id);
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validateCategoryExists(request.getCategoryId());
        Product product = findProductById(id);

        log.info("Обновление продукта с id {}", id);
        productMapper.updateEntity(product, request);
        product.setUpdatedAt(LocalDateTime.now(clock));

        ProductResponse updateProduct = productMapper.toProductResponse(product);
        log.info("Продукт с id {} был обновлен", id);
        return updateProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductById(id);
        log.info("Удаление продукта с id {}", id);
        productRepository.delete(product);
        log.info("Продукт с id {} был удален", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Product> products = productRepository.findAll(pageable);
        return productMapper.toProductListResponseList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse searchProducts(ProductFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        String pattern = filter.getSearchQuery() != null
                ? "%" + filter.getSearchQuery().trim() + "%"
                : null;

        String brandPattern = filter.getBrand() != null
                ? "%" + filter.getBrand().trim() + "%"
                : null;

        Page<Product> byFilter = productRepository.findByFilters(
                pattern,
                filter.getCategoryId(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                brandPattern,
                pageable
        );

        return productMapper.toProductListResponseList(byFilter);
    }

    private void validateCategoryExists(Long id) {
        categoryService.validateCategoryExists(id);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException(String.format("Продукт с id %d не найден", id)));
    }

}
