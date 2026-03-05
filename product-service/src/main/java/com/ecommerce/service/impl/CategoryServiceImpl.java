package com.ecommerce.service.impl;

import com.ecommerce.exception.CategoryAlreadyExistsException;
import com.ecommerce.exception.CategoryHasProductsException;
import com.ecommerce.exception.CategoryNotFoundException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.model.dto.CategoryRequest;
import com.ecommerce.model.dto.CategoryResponse;
import com.ecommerce.model.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException("Категория с именем " + request.getName() + " уже существует");
        }
        log.info("Создание новой категории: {}", request.getName());
        Category category = categoryMapper.toEntity(request);
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        Category savedCategory = categoryRepository.save(category);
        log.info("Создана новая категория: {}", savedCategory.getName());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);
        log.info("Обновление категории: {}", category.getName());
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException("Категория с именем " + request.getName() + " уже существует");
        }

        categoryMapper.updateEntity(category, request);
        category.setUpdatedAt(LocalDateTime.now(Clock.systemDefaultZone()));
        CategoryResponse updatedCategory = categoryMapper.toResponse(category);
        log.info("Категория обновлена: {}", updatedCategory.getName());
        return updatedCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryHasProductsException("Категория с ID " + id + " не может быть удалена," +
                    " так как она содержит товары");
        }
        log.info("Удаление категории: ID={}, название={}", id, category.getName());
        categoryRepository.delete(category);
    }

    @Override
    public void validateCategoryExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Категория с ID " + id + " не найдена");
        }
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new CategoryNotFoundException("Категория с ID " + id + " не найдена"));
    }
}
