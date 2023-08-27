package ru.practicum.explore.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.Category;
import ru.practicum.explore.category.CategoryRepository;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.CategoryMapper;
import ru.practicum.explore.event.repositories.EventRepository;
import ru.practicum.explore.exceptions.ConflictException;
import ru.practicum.explore.exceptions.NotFoundException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category created = CategoryMapper.toCategory(categoryDto);
        Category after = categoryRepository.save(created);
        log.info("Добавлена новая категория {} ", categoryDto);
        return CategoryMapper.toCategoryDto(after);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Long categoryId) {

        Category updatedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория события не найдена, id = " + categoryId));
        updatedCategory.setName(categoryDto.getName());
        Category afterUpdate = categoryRepository.save(updatedCategory);
        log.info("Обновлена категория по идентификатору {} ", categoryId);
        return CategoryMapper.toCategoryDto(afterUpdate);
    }

    @Override
    public void deleteCategory(Long catId) {

        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = '" + catId + "' не найден"));
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(category);
        if (eventRepository.findByCategoryId(catId).isEmpty()) {
            throw new ConflictException("Удаление категории с привязанными событиями");
        }
        categoryRepository.deleteById(catId);
        log.info("Удалена категория по идентификатору {} ", catId);
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
