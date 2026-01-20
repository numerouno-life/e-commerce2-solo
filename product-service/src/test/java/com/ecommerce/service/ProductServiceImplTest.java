package com.ecommerce.service;

import com.ecommerce.exception.CategoryNotFoundException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.dto.ProductRequest;
import com.ecommerce.model.dto.ProductResponse;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private ProductServiceImpl productService;

    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2026, 1, 19, 10, 0);
        Instant fixedInstant = fixedTime.atZone(ZoneId.systemDefault()).toInstant();
        lenient().when(clock.instant()).thenReturn(fixedInstant);
        lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    void createProduct_shouldCreateAndReturnProductResponse_whenCategoryExists() {
        Long categoryId = 1L;
        ProductRequest request = new ProductRequest("Phone", "Smartphone", new BigDecimal("999.99"),
                categoryId, "Apple", 10, "url");

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Electronics");

        Product productEntity = Product.builder()
                .name("Phone")
                .description("Smartphone")
                .price(new BigDecimal("999.99"))
                .category(category)
                .brand("Apple")
                .stockQuantity(10)
                .imageUrl("url")
                .build();

        Product savedProduct = Product.builder()
                .id(100L)
                .name("Phone")
                .description("Smartphone")
                .price(new BigDecimal("999.99"))
                .category(category)
                .brand("Apple")
                .stockQuantity(10)
                .imageUrl("url")
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        ProductResponse response = new ProductResponse(100L, "Phone", "Smartphone", new BigDecimal("999.99"),
                "Electronics", "Apple", 10, "url", fixedTime, fixedTime);

        given(productMapper.toEntity(request)).willReturn(productEntity);
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);
        given(productMapper.toProductResponse(savedProduct)).willReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertThat(result).isEqualTo(response);
        verify(productMapper).toEntity(request);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toProductResponse(savedProduct);
        verify(categoryService).getCategoryById(categoryId);

        assertThat(savedProduct.getCreatedAt()).isEqualTo(fixedTime);
        assertThat(savedProduct.getUpdatedAt()).isEqualTo(fixedTime);
    }

    @Test
    void createProduct_shouldThrowException_whenCategoryDoesNotExist() {
        Long categoryId = 999L;
        ProductRequest request = new ProductRequest("Phone", "Smartphone", new BigDecimal("999.99"),
                categoryId, "Apple", 10, "url");

        willThrow(new CategoryNotFoundException("Категория с ID " + categoryId + " не найдена"))
                .given(categoryService).getCategoryById(categoryId);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Категория с ID " + categoryId + " не найдена");

        verify(productMapper, never()).toEntity(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_shouldReturnProductResponse_whenProductExists() {
        Long productId = 1L;
        Product product = Product.builder().id(productId).name("Laptop").build();
        ProductResponse response = new ProductResponse(productId, "Laptop", null, null,
                null, null, null, null, null, null);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productMapper.toProductResponse(product)).willReturn(response);

        ProductResponse result = productService.getProductById(productId);

        assertThat(result).isEqualTo(response);
        verify(productRepository).findById(productId);
        verify(productMapper).toProductResponse(product);
    }

    @Test
    void getProductById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Продукт с id 999 не найден");
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProductResponse_whenProductAndCategoryExist() {
        Long productId = 1L;
        Long categoryId = 2L;
        ProductRequest request = new ProductRequest("New Phone", "Updated",
                new BigDecimal("1099.99"), categoryId, "Samsung", 5, "new-url");

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Gadgets");

        Product existingProduct = Product.builder()
                .id(productId)
                .name("Old Phone")
                .category(category)
                .build();

        ProductResponse response = new ProductResponse(productId, "New Phone", "Updated",
                new BigDecimal("1099.99"), "Gadgets", "Samsung", 5,
                "new-url", null, fixedTime);

        given(productRepository.findById(productId)).willReturn(Optional.of(existingProduct));
        willDoNothing().given(productMapper).updateEntity(existingProduct, request);
        given(productMapper.toProductResponse(existingProduct)).willReturn(response);

        ProductResponse result = productService.updateProduct(productId, request);

        assertThat(result).isEqualTo(response);
        verify(productRepository).findById(productId);
        verify(categoryService).getCategoryById(categoryId);
        verify(productMapper).updateEntity(existingProduct, request);
        verify(productMapper).toProductResponse(existingProduct);

        assertThat(existingProduct.getUpdatedAt()).isEqualTo(fixedTime);
    }

    @Test
    void updateProduct_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 999L;
        Long categoryId = 1L;
        ProductRequest request = new ProductRequest("X", "Y", BigDecimal.TEN, categoryId, "Z", 1, "url");

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Продукт с id 999 не найден");

        verify(categoryService).getCategoryById(any());
    }

    @Test
    void updateProduct_shouldThrowException_whenCategoryDoesNotExist() {
        Long productId = 1L;
        Long categoryId = 999L;
        ProductRequest request = new ProductRequest("X", "Y", BigDecimal.TEN, categoryId, "Z", 1, "url");

        Product product = new Product();
        product.setId(productId);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        willThrow(new RuntimeException("Category missing")).given(categoryService).getCategoryById(categoryId);

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category missing");

        verify(productMapper, never()).updateEntity(any(), any());
    }

    @Test
    void deleteProduct_shouldDeleteProduct_whenProductExists() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Продукт с id 999 не найден");

        verify(productRepository, never()).delete((Product) any());
    }

}
