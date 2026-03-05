package com.ecommerce.service;

import com.ecommerce.exception.CategoryNotFoundException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.dto.ProductFilterRequest;
import com.ecommerce.model.dto.ProductListResponse;
import com.ecommerce.model.dto.ProductRequest;
import com.ecommerce.model.dto.ProductResponse;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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

    private Product product;
    private ProductResponse productResponse;
    private ProductRequest productRequest;
    @Captor
    private ArgumentCaptor<String> patternCaptor;

    @Captor
    private ArgumentCaptor<Long> categoryIdCaptor;

    @Captor
    private ArgumentCaptor<BigDecimal> minPriceCaptor;

    @Captor
    private ArgumentCaptor<BigDecimal> maxPriceCaptor;

    @Captor
    private ArgumentCaptor<String> brandPatternCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Product product1;
    private Product product2;
    private ProductResponse productResponse1;
    private ProductResponse productResponse2;
    private final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private LocalDateTime fixedTime;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        fixedInstant = Instant.parse("2024-01-01T12:00:00Z");
        fixedTime = LocalDateTime.ofInstant(fixedInstant, UTC_ZONE);

        Clock fixedClock = Clock.fixed(fixedInstant, UTC_ZONE);
        lenient().when(clock.instant()).thenReturn(fixedClock.instant());
        lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setCreatedAt(fixedTime);
        product.setUpdatedAt(fixedTime);

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Test Product");
        productResponse.setPrice(BigDecimal.valueOf(100.0));

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setPrice(BigDecimal.valueOf(100.0));
        productRequest.setCategoryId(1L);

        product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone 13");
        product1.setDescription("Apple iPhone 13");
        product1.setPrice(BigDecimal.valueOf(999.99));
        product1.setBrand("Apple");
        product1.setId(1L);
        product1.setCreatedAt(fixedTime);
        product1.setUpdatedAt(fixedTime);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Galaxy S21");
        product2.setDescription("Samsung Galaxy S21");
        product2.setPrice(BigDecimal.valueOf(899.99));
        product2.setBrand("Samsung");
        product2.setId(1L);
        product2.setCreatedAt(fixedTime);
        product2.setUpdatedAt(fixedTime);

        // Инициализация ответов
        productResponse1 = new ProductResponse();
        productResponse1.setId(1L);
        productResponse1.setName("iPhone 13");
        productResponse1.setDescription("Apple iPhone 13");
        productResponse1.setPrice(BigDecimal.valueOf(999.99));
        productResponse1.setBrand("Apple");
        productResponse1.setId(1L);

        productResponse2 = new ProductResponse();
        productResponse2.setId(2L);
        productResponse2.setName("Galaxy S21");
        productResponse2.setDescription("Samsung Galaxy S21");
        productResponse2.setPrice(BigDecimal.valueOf(899.99));
        productResponse2.setBrand("Samsung");
        productResponse2.setId(1L);
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

        willDoNothing().given(categoryService).validateCategoryExists(categoryId);

        ProductResponse result = productService.createProduct(request);

        assertThat(result).isEqualTo(response);
        verify(productMapper).toEntity(request);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toProductResponse(savedProduct);
        verify(categoryService).validateCategoryExists(categoryId);

        assertThat(savedProduct.getCreatedAt()).isEqualTo(fixedTime);
        assertThat(savedProduct.getUpdatedAt()).isEqualTo(fixedTime);
    }

    @Test
    void createProduct_shouldThrowException_whenCategoryDoesNotExist() {
        Long categoryId = 999L;
        ProductRequest request = new ProductRequest("Phone", "Smartphone", new BigDecimal("999.99"),
                categoryId, "Apple", 10, "url");

        willThrow(new CategoryNotFoundException("Категория с ID " + categoryId + " не найдена"))
                .given(categoryService).validateCategoryExists(categoryId);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Категория с ID " + categoryId + " не найдена");

        verify(categoryService).validateCategoryExists(categoryId);
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
        willDoNothing().given(categoryService).validateCategoryExists(categoryId);
        willDoNothing().given(productMapper).updateEntity(existingProduct, request);
        given(productMapper.toProductResponse(existingProduct)).willReturn(response);

        ProductResponse result = productService.updateProduct(productId, request);

        assertThat(result).isEqualTo(response);
        verify(productRepository).findById(productId);
        verify(categoryService).validateCategoryExists(categoryId);
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

        verify(productRepository).findById(productId);
    }

    @Test
    void updateProduct_shouldThrowException_whenCategoryDoesNotExist() {
        Long productId = 1L;
        Long categoryId = 999L;
        ProductRequest request = new ProductRequest("X", "Y", BigDecimal.TEN, categoryId, "Z", 1, "url");

        willThrow(new CategoryNotFoundException("Категория с ID " + categoryId + " не найдена"))
                .given(categoryService).validateCategoryExists(categoryId);

        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Категория с ID " + categoryId + " не найдена");
        verify(categoryService).validateCategoryExists(categoryId);
        verify(productRepository, never()).findById(anyLong());
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

    @Test
    void getAllProducts_shouldReturnListOfProductResponses() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        List<Product> productList = List.of(product);
        Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());

        ProductListResponse expectedResponse = new ProductListResponse();
        expectedResponse.setProducts(List.of(productResponse));
        expectedResponse.setTotalElements(1L);
        expectedResponse.setTotalPages(1);
        expectedResponse.setCurrentPage(0);

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

        ProductListResponse actualResponse = productService.getAllProducts(page, size);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getProducts()).hasSize(1);
        assertThat(actualResponse.getTotalElements()).isEqualTo(1L);
        assertThat(actualResponse.getCurrentPage()).isEqualTo(0);

        verify(productRepository).findAll(pageable);
        verify(productMapper).toProductListResponseList(productPage);
    }

    @Nested
    class SearchProductsTests {

        @Test
        void searchProducts_WithAllFilters_ShouldReturnFilteredProducts() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery("iphone");
            filter.setCategoryId(1L);
            filter.setMinPrice(BigDecimal.valueOf(500.0));
            filter.setMaxPrice(BigDecimal.valueOf(1500.0));
            filter.setBrand("Apple");

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product1);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse1));
            expectedResponse.setTotalElements(1L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    eq("%iphone%"),
                    eq(1L),
                    eq(BigDecimal.valueOf(500.0)),
                    eq(BigDecimal.valueOf(1500.0)),
                    eq("%Apple%"),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(1);
            assertThat(actualResponse.getProducts().get(0).getName()).isEqualTo("iPhone 13");
            assertThat(actualResponse.getProducts().get(0).getBrand()).isEqualTo("Apple");
            assertThat(actualResponse.getTotalElements()).isEqualTo(1L);

            verify(productRepository).findByFilters(
                    "%iphone%",
                    1L,
                    BigDecimal.valueOf(500.0),
                    BigDecimal.valueOf(1500.0),
                    "%Apple%",
                    expectedPageable
            );
        }


        @Test
        void searchProducts_WithSearchQueryOnly_ShouldSearchByNameAndDescription() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery("samsung");
            filter.setCategoryId(null);
            filter.setMinPrice(null);
            filter.setMaxPrice(null);
            filter.setBrand(null);

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product2);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse2));
            expectedResponse.setTotalElements(1L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    eq("%samsung%"),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull(),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(1);
            assertThat(actualResponse.getProducts().get(0).getName()).isEqualTo("Galaxy S21");
            assertThat(actualResponse.getProducts().get(0).getBrand()).isEqualTo("Samsung");

            verify(productRepository).findByFilters(
                    "%samsung%", null, null, null, null, expectedPageable
            );
        }

        @Test
        void searchProducts_WithCategoryOnly_ShouldFilterByCategory() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery(null);
            filter.setCategoryId(1L);
            filter.setMinPrice(null);
            filter.setMaxPrice(null);
            filter.setBrand(null);

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product1, product2);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse1, productResponse2));
            expectedResponse.setTotalElements(2L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    isNull(),
                    eq(1L),
                    isNull(),
                    isNull(),
                    isNull(),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(2);
            assertThat(actualResponse.getTotalElements()).isEqualTo(2L);

            verify(productRepository).findByFilters(
                    null, 1L, null, null, null, expectedPageable
            );
        }

        @Test
        void searchProducts_WithPriceRangeOnly_ShouldFilterByPrice() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery(null);
            filter.setCategoryId(null);
            filter.setMinPrice(BigDecimal.valueOf(900.0));
            filter.setMaxPrice(BigDecimal.valueOf(1000.0));
            filter.setBrand(null);

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product1);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse1));
            expectedResponse.setTotalElements(1L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    isNull(),
                    isNull(),
                    eq(BigDecimal.valueOf(900.0)),
                    eq(BigDecimal.valueOf(1000.0)),
                    isNull(),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(1);
            assertThat(actualResponse.getProducts().get(0).getPrice()).isEqualTo(BigDecimal.valueOf(999.99));

            verify(productRepository).findByFilters(
                    null,
                    null,
                    BigDecimal.valueOf(900.0),
                    BigDecimal.valueOf(1000.0),
                    null,
                    expectedPageable
            );
        }

        @Test
        void searchProducts_WithBrandOnly_ShouldFilterByBrand() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery(null);
            filter.setCategoryId(null);
            filter.setMinPrice(null);
            filter.setMaxPrice(null);
            filter.setBrand("Samsung");

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product2);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse2));
            expectedResponse.setTotalElements(1L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull(),
                    eq("%Samsung%"),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(1);
            assertThat(actualResponse.getProducts().get(0).getBrand()).isEqualTo("Samsung");

            verify(productRepository).findByFilters(
                    null, null, null, null, "%Samsung%", expectedPageable
            );
        }

        @Test
        void searchProducts_WithEmptyFilter_ShouldReturnAllProducts() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery(null);
            filter.setCategoryId(null);
            filter.setMinPrice(null);
            filter.setMaxPrice(null);
            filter.setBrand(null);

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product1, product2);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse1, productResponse2));
            expectedResponse.setTotalElements(2L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull(),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(2);
            assertThat(actualResponse.getTotalElements()).isEqualTo(2L);

            verify(productRepository).findByFilters(
                    null, null, null, null, null, expectedPageable
            );
        }

        @Test
        void searchProducts_WithSearchQueryAndBrand_ShouldCombineFilters() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery("iphone");
            filter.setCategoryId(null);
            filter.setMinPrice(null);
            filter.setMaxPrice(null);
            filter.setBrand("Apple");

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product1);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse1));
            expectedResponse.setTotalElements(1L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    eq("%iphone%"),
                    isNull(),
                    isNull(),
                    isNull(),
                    eq("%Apple%"),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(1);
            assertThat(actualResponse.getProducts().get(0).getName()).isEqualTo("iPhone 13");
            assertThat(actualResponse.getProducts().get(0).getBrand()).isEqualTo("Apple");

            verify(productRepository).findByFilters(
                    "%iphone%", null, null, null, "%Apple%", expectedPageable
            );
        }

        @Test
        void searchProducts_WithPriceRangeAndCategory_ShouldCombineFilters() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery(null);
            filter.setCategoryId(1L);
            filter.setMinPrice(BigDecimal.valueOf(800.0));
            filter.setMaxPrice(BigDecimal.valueOf(1000.0));
            filter.setBrand(null);

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            List<Product> productList = List.of(product1, product2);
            Page<Product> productPage = new PageImpl<>(productList, expectedPageable, productList.size());

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of(productResponse1, productResponse2));
            expectedResponse.setTotalElements(2L);
            expectedResponse.setTotalPages(1);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    isNull(),
                    eq(1L),
                    eq(BigDecimal.valueOf(800.0)),
                    eq(BigDecimal.valueOf(1000.0)),
                    isNull(),
                    eq(expectedPageable)
            )).thenReturn(productPage);

            when(productMapper.toProductListResponseList(productPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).hasSize(2);
            assertThat(actualResponse.getTotalElements()).isEqualTo(2L);

            verify(productRepository).findByFilters(
                    null,
                    1L,
                    BigDecimal.valueOf(800.0),
                    BigDecimal.valueOf(1000.0),
                    null,
                    expectedPageable
            );
        }


        @Test
        void searchProducts_WithLeadingAndTrailingSpaces_ShouldTrimSearchQuery() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery("  iphone  ");
            filter.setBrand("  Apple  ");

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            when(productRepository.findByFilters(
                    eq("%iphone%"),
                    isNull(),
                    isNull(),
                    isNull(),
                    eq("%Apple%"),
                    eq(expectedPageable)
            )).thenReturn(Page.empty());

            
            productService.searchProducts(filter, page, size);

            
            verify(productRepository).findByFilters(
                    "%iphone%", null, null, null, "%Apple%", expectedPageable
            );
        }

        @Test
        void searchProducts_WithNoResults_ShouldReturnEmptyList() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery("nonexistent");
            filter.setCategoryId(999L);

            Pageable expectedPageable = PageRequest.of(page, size, Sort.by("id").ascending());

            Page<Product> emptyPage = Page.empty(expectedPageable);

            ProductListResponse expectedResponse = new ProductListResponse();
            expectedResponse.setProducts(List.of());
            expectedResponse.setTotalElements(0L);
            expectedResponse.setTotalPages(0);
            expectedResponse.setCurrentPage(0);

            when(productRepository.findByFilters(
                    eq("%nonexistent%"),
                    eq(999L),
                    isNull(),
                    isNull(),
                    isNull(),
                    eq(expectedPageable)
            )).thenReturn(emptyPage);

            when(productMapper.toProductListResponseList(emptyPage)).thenReturn(expectedResponse);

            
            ProductListResponse actualResponse = productService.searchProducts(filter, page, size);

            
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getProducts()).isEmpty();
            assertThat(actualResponse.getTotalElements()).isZero();
        }

        @Test
        void searchProducts_VerifySortingOrder() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();

            
            productService.searchProducts(filter, page, size);

            
            verify(productRepository).findByFilters(
                    any(), any(), any(), any(), any(), pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getSort()).isEqualTo(Sort.by("id").ascending());
            assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
            assertThat(capturedPageable.getPageSize()).isEqualTo(size);
        }

        @Test
        void searchProducts_VerifyPatternFormats() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery("test");
            filter.setBrand("brand");

            
            productService.searchProducts(filter, page, size);

            
            verify(productRepository).findByFilters(
                    patternCaptor.capture(),
                    any(),
                    any(),
                    any(),
                    brandPatternCaptor.capture(),
                    any()
            );

            assertThat(patternCaptor.getValue()).isEqualTo("%test%");
            assertThat(brandPatternCaptor.getValue()).isEqualTo("%brand%");
        }

        @Test
        void searchProducts_WithNullSearchQuery_ShouldPassNullPattern() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery(null);

            
            productService.searchProducts(filter, page, size);

            
            verify(productRepository).findByFilters(
                    patternCaptor.capture(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
            );

            assertThat(patternCaptor.getValue()).isNull();
        }

        @Test
        void searchProducts_WithNullBrand_ShouldPassNullBrandPattern() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setBrand(null);

            
            productService.searchProducts(filter, page, size);

            
            verify(productRepository).findByFilters(
                    any(),
                    any(),
                    any(),
                    any(),
                    brandPatternCaptor.capture(),
                    any()
            );

            assertThat(brandPatternCaptor.getValue()).isNull();
        }

        @Test
        void searchProducts_WithEmptySearchQuery_ShouldHandleEmptyString() {
            
            int page = 0;
            int size = 10;
            ProductFilterRequest filter = new ProductFilterRequest();
            filter.setSearchQuery("");
            filter.setBrand("");

            
            productService.searchProducts(filter, page, size);

            
            verify(productRepository).findByFilters(
                    patternCaptor.capture(),
                    any(),
                    any(),
                    any(),
                    brandPatternCaptor.capture(),
                    any()
            );

            // Пустая строка должна стать "%%" после trim
            assertThat(patternCaptor.getValue()).isEqualTo("%%");
            assertThat(brandPatternCaptor.getValue()).isEqualTo("%%");
        }
    }
}
