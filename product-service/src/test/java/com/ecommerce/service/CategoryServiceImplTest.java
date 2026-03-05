package com.ecommerce.service;

import com.ecommerce.exception.CategoryAlreadyExistsException;
import com.ecommerce.exception.CategoryHasProductsException;
import com.ecommerce.exception.CategoryNotFoundException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.model.dto.CategoryRequest;
import com.ecommerce.model.dto.CategoryResponse;
import com.ecommerce.model.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest request;
    private CategoryResponse response;
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-06T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(
                categoryRepository,
                productRepository,
                categoryMapper
        );

        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .createdAt(LocalDateTime.of(2026, 1, 6, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 6, 12, 0))
                .build();

        request = new CategoryRequest("Electronics", "Electronic devices");

        response = new CategoryResponse(
                1L,
                "Electronics",
                "Electronic devices",
                LocalDateTime.of(2026, 1, 6, 12, 0),
                LocalDateTime.of(2026, 1, 6, 12, 0)
        );
    }

    @Test
    void createCategory_Success() {
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result).isEqualTo(response);
        verify(categoryRepository).existsByName(request.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_ThrowsCategoryAlreadyExistsException() {
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(CategoryAlreadyExistsException.class)
                .hasMessage("Категория с именем Electronics уже существует");

        verify(categoryRepository).existsByName(request.getName());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result).isEqualTo(response);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_ThrowsCategoryNotFoundException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Категория с ID 1 не найдена");

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1).containsExactly(response);
        verify(categoryRepository).findAll();
    }

    @Test
    void updateCategory_Success() {
        CategoryRequest updatedRequest = new CategoryRequest("Updated Electronics", "Updated devices");

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Updated Electronics")).thenReturn(false);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        CategoryResponse result = categoryService.updateCategory(1L, updatedRequest);

        assertThat(result).isEqualTo(response);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("Updated Electronics");
    }

    @Test
    void updateCategory_ThrowsCategoryNotFoundException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).existsByName(anyString());
    }

    @Test
    void updateCategory_ThrowsCategoryAlreadyExistsException() {
        CategoryRequest updatedRequest = new CategoryRequest("New Electronics", "New devices");
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("New Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(1L, updatedRequest))
                .isInstanceOf(CategoryAlreadyExistsException.class)
                .hasMessage("Категория с именем New Electronics уже существует");

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("New Electronics");
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(anyLong())).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository).findById(1L);
        verify(productRepository).existsByCategoryId(1L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_ThrowsCategoryNotFoundException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).existsByCategoryId(anyLong());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_ThrowsCategoryHasProductsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(anyLong())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(CategoryHasProductsException.class)
                .hasMessage("Категория с ID 1 не может быть удалена, так как она содержит товары");

        verify(categoryRepository).findById(1L);
        verify(productRepository).existsByCategoryId(1L);
        verify(categoryRepository, never()).delete(any());
    }
}
