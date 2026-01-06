package com.ecommerce.service.impl;

import com.ecommerce.model.dto.CategoryRequest;
import com.ecommerce.model.dto.CategoryResponse;
import com.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        return null;
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
