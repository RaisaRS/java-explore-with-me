package ru.practicum.explore.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.Category;
import ru.practicum.explore.category.CategoryRepository;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.CategoryMapper;
import ru.practicum.explore.exceptions.NotFoundException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category created = CategoryMapper.toCategory(categoryDto);
        Category after = categoryRepository.save(created);
        return CategoryMapper.toCategoryDto(after);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Long categoryId) {

        Category updatedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория события не найдена, id = " + categoryId));
        updatedCategory.setName(categoryDto.getName());
        Category afterUpdate = categoryRepository.save(updatedCategory);
        return CategoryMapper.toCategoryDto(afterUpdate);
    }

    @Override
    public void deleteCategory(Long catId) {
        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        //PageRequest pageRequest = CreateRequest.createRequest(from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categories.isEmpty() ? Collections.emptyList() : CategoryMapper.listCategoryDtos(categories);
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория события не найдена, id = " + catId));
        return CategoryMapper.toCategoryDto(category);
    }

}
