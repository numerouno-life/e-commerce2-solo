package com.ecommerce.repository;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.spicification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;
    private Category books;
    private Category clothing;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        electronics = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
        electronics = categoryRepository.save(electronics);

        books = Category.builder()
                .name("Books")
                .description("Books and magazines")
                .build();
        books = categoryRepository.save(books);

        clothing = Category.builder()
                .name("Clothing")
                .description("Clothes and accessories")
                .build();
        clothing = categoryRepository.save(clothing);

        Product laptop = Product.builder()
                .name("Laptop Dell XPS")
                .description("Powerful laptop")
                .price(BigDecimal.valueOf(1500.99))
                .category(electronics)
                .brand("Dell")
                .stockQuantity(10)
                .build();

        Product smartphone = Product.builder()
                .name("iPhone 15")
                .description("Latest smartphone")
                .price(BigDecimal.valueOf(999.99))
                .category(electronics)
                .brand("Apple")
                .stockQuantity(5)
                .build();

        Product book = Product.builder()
                .name("Clean Code")
                .description("Programming book")
                .price(BigDecimal.valueOf(49.99))
                .category(books)
                .brand("O'Reilly")
                .stockQuantity(100)
                .build();

        Product tshirt = Product.builder()
                .name("T-Shirt")
                .description("Cotton t-shirt")
                .price(BigDecimal.valueOf(19.99))
                .category(clothing)
                .brand("Nike")
                .stockQuantity(50)
                .build();

        productRepository.saveAll(List.of(laptop, smartphone, book, tshirt));
    }

    @Test
    void contextLoads() {
        assertThat(productRepository).isNotNull();
        assertThat(categoryRepository).isNotNull();
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAll(pageable);

        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    void findByCategoryId_ShouldReturnProductsInCategory() {
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findByCategoryId(electronics.getId(), pageable);

        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop Dell XPS", "iPhone 15");
    }

    @Test
    void findByCategoryId_ShouldReturnEmptyForNonExistingCategory() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Long nonExistingCategoryId = 999L;

        
        Page<Product> result = productRepository.findByCategoryId(nonExistingCategoryId, pageable);

        
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByPriceBetween_ShouldReturnProductsInPriceRange() {
        
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minPrice = BigDecimal.valueOf(500);
        BigDecimal maxPrice = BigDecimal.valueOf(1200);

        
        Page<Product> result = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
    }

    @Test
    void findByPriceBetween_ShouldReturnEmptyForNoMatches() {
        
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minPrice = BigDecimal.valueOf(2000);
        BigDecimal maxPrice = BigDecimal.valueOf(3000);

        
        Page<Product> result = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);

        
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findByNameContainingIgnoreCase("laptop", pageable);

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop Dell XPS");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldBeCaseInsensitive() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findByNameContainingIgnoreCase("LAPTOP", pageable);

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop Dell XPS");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnEmptyForNoMatches() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findByNameContainingIgnoreCase("nonexisting", pageable);

        
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllWithSpecification_ShouldFilterByMultipleCriteria() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findAll(
                ProductSpecification.withFilters(
                        "iPhone",  
                        electronics.getId(),  
                        BigDecimal.valueOf(500),  
                        BigDecimal.valueOf(1500), 
                        "Apple"  
                ),
                pageable
        );

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
    }

    @Test
    void findAllWithSpecification_ShouldFilterByNameOnly() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findAll(
                ProductSpecification.withFilters(
                        "Clean",
                        null,
                        null,
                        null,
                        null
                ),
                pageable
        );

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Clean Code");
    }

    @Test
    void findAllWithSpecification_ShouldFilterByCategoryOnly() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findAll(
                ProductSpecification.withFilters(
                        null,  
                        clothing.getId(),  
                        null,  
                        null, 
                        null  
                ),
                pageable
        );

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("T-Shirt");
    }

    @Test
    void findAllWithSpecification_ShouldFilterByPriceRangeOnly() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findAll(
                ProductSpecification.withFilters(
                        null,  
                        null,  
                        BigDecimal.valueOf(10),  
                        BigDecimal.valueOf(30), 
                        null  
                ),
                pageable
        );

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("T-Shirt");
    }

    @Test
    void findAllWithSpecification_ShouldFilterByBrandOnly() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findAll(
                ProductSpecification.withFilters(
                        null,  
                        null,  
                        null,  
                        null, 
                        "Nike"  
                ),
                pageable
        );

        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("T-Shirt");
    }

    @Test
    void findAllWithSpecification_ShouldReturnEmptyWhenNoMatches() {
        
        Pageable pageable = PageRequest.of(0, 10);

        
        Page<Product> result = productRepository.findAll(
                ProductSpecification.withFilters(
                        "Nonexisting",  
                        999L,  
                        BigDecimal.valueOf(10000),  
                        BigDecimal.valueOf(20000), 
                        "Unknown"  
                ),
                pageable
        );

        
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void saveProduct_ShouldSetCreatedAtAutomatically() {
        
        Product newProduct = Product.builder()
                .name("New Product")
                .price(BigDecimal.valueOf(100))
                .category(electronics)
                .build();

        
        Product saved = productRepository.save(newProduct);

        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNull();
    }

    @Test
    void deleteProduct_ShouldWork() {
        
        long initialCount = productRepository.count();
        Product product = productRepository.findAll().get(0);

        
        productRepository.delete(product);

        
        assertThat(productRepository.count()).isEqualTo(initialCount - 1);
    }

    @Test
    void productCategoryRelationship_ShouldBeCorrect() {
        
        Product product = productRepository.findByNameContainingIgnoreCase("iPhone", PageRequest.of(0, 1))
                .getContent()
                .get(0);

        
        Category category = product.getCategory();

        
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getId()).isEqualTo(electronics.getId());
    }
}