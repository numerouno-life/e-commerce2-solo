package com.ecommerce.service;

import com.ecommerce.model.dto.ProductFilterRequest;
import com.ecommerce.model.dto.ProductListResponse;
import com.ecommerce.model.dto.ProductRequest;
import com.ecommerce.model.dto.ProductResponse;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductListResponse getAllProducts(int page, int size);

    ProductListResponse searchProducts(ProductFilterRequest filter, int page, int size);

}
