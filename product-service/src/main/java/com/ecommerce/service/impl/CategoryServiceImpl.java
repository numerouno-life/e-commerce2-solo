package com.ecommerce.service.impl;

import com.ecommerce.exception.CategoryAlreadyExistsException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.model.dto.CategoryRequest;
import com.ecommerce.model.dto.CategoryResponse;
import com.ecommerce.model.entity.Category;
import com.ecommerce.repository.CategoryRepository;
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
    private final CategoryMapper categoryMapper;
    private final Clock clock;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException("Категория с именем " + request.getName() + " уже существует");
        }
        log.info("Создание новой категории: {}", request.getName());
        Category category = categoryMapper.toEntity(request);
        LocalDateTime now = LocalDateTime.now(clock);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        Category savedCategory = categoryRepository.save(category);
        log.info("Создана новая категория: {}", savedCategory.getName());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return List.of();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {

    }
}
