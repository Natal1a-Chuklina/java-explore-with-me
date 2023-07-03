package ru.practicum.category;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(long catId);

    CategoryDto updateCategory(NewCategoryDto newCategoryDto, long catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(long catId);
}
