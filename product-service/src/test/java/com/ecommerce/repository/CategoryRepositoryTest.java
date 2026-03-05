package com.ecommerce.repository;

import com.ecommerce.model.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(categoryRepository).isNotNull();
    }

    @Test
    void saveCategory_ShouldWork() {
        
        Category category = Category.builder()
                .name("Test Category")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .build();

        
        Category saved = categoryRepository.save(category);

        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Category");
        assertThat(saved.getDescription()).isEqualTo("Test Description");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_ShouldReturnCategory() {
        
        Category category = Category.builder()
                .name("Electronics")
                .build();
        Category saved = categoryRepository.save(category);

        
        Optional<Category> found = categoryRepository.findById(saved.getId());

        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        
        categoryRepository.save(Category.builder().name("Cat1").build());
        categoryRepository.save(Category.builder().name("Cat2").build());

        
        var categories = categoryRepository.findAll();

        
        assertThat(categories).hasSize(2);
    }

    @Test
    void existsByName_ShouldReturnTrueForExistingCategory() {
        
        Category category = Category.builder()
                .name("Books")
                .build();
        categoryRepository.save(category);

        
        boolean exists = categoryRepository.existsByName("Books");

        
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalseForNonExistingCategory() {
        
        boolean exists = categoryRepository.existsByName("Non Existing");

        
        assertThat(exists).isFalse();
    }

    @Test
    void findByName_ShouldReturnCategory() {

        Category category = Category.builder()
                .name("Clothing")
                .build();
        categoryRepository.save(category);


        Optional<Category> found = categoryRepository.findByName("Clothing");


        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Clothing");
    }

    @Test
    void findByName_ShouldReturnEmptyForNonExisting() {
        
        Optional<Category> found = categoryRepository.findByName("Unknown");

        
        assertThat(found).isEmpty();
    }

    @Test
    void uniqueNameConstraint_ShouldWork() {
        
        Category category1 = Category.builder()
                .name("Sports")
                .build();
        categoryRepository.save(category1);

        Category category2 = Category.builder()
                .name("Sports")
                .build();

        assertThat(categoryRepository.existsByName("Sports")).isTrue();

        var categories = categoryRepository.findAll();
        long sportsCount = categories.stream()
                .filter(c -> "Sports".equals(c.getName()))
                .count();
        assertThat(sportsCount).isEqualTo(1);
    }

    @Test
    void deleteCategory_ShouldWork() {
        
        Category category = Category.builder()
                .name("ToDelete")
                .build();
        Category saved = categoryRepository.save(category);
        long initialCount = categoryRepository.count();

        
        categoryRepository.delete(saved);

        
        assertThat(categoryRepository.count()).isEqualTo(initialCount - 1);
        assertThat(categoryRepository.existsByName("ToDelete")).isFalse();
    }

}