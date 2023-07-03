package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryMapper;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryStorage categoryStorage;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryStorage.save(CategoryMapper.toCategory(newCategoryDto));
        log.info("Created category with id = {}", category.getId());
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteCategory(long catId) {
        try {
            categoryStorage.deleteById(catId);
            log.info("Category with id = {} was deleted", catId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Attempt to delete nonexistent category by id = {}", catId);
            throw new EntityNotFoundException(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, long catId) {
        Category category = getCategory(catId);
        category.setName(newCategoryDto.getName());
        categoryStorage.save(category);
        log.info("Updated category with id = {}", category.getId());
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        List<Category> categories = categoryStorage.findAll(page).getContent();
        log.info("Received {} categories", categories.size());
        return CategoryMapper.toCategoryDto(categories);
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        Category category = getCategory(catId);
        log.info("Received category with id = {}", catId);
        return CategoryMapper.toCategoryDto(category);
    }

    private Category getCategory(long catId) {
        Optional<Category> category = categoryStorage.findById(catId);

        if (category.isEmpty()) {
            log.warn("Attempt to get nonexistent category by id = {}", catId);
            throw new EntityNotFoundException(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));
        }

        return category.get();
    }
}
