package com.ecommerce.service.impl;

import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.dto.ProductRequest;
import com.ecommerce.model.dto.ProductResponse;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        findCategoryById(request.getCategoryId());
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
        findCategoryById(request.getCategoryId());
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

    private void findCategoryById(Long id) {
        categoryService.getCategoryById(id);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException(String.format("Продукт с id %d не найден", id)));
    }

}
