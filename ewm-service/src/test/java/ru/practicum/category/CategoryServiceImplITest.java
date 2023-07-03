package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CategoryServiceImplITest {
    private final CategoryService categoryService;

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createCategory_WhenDataIsCorrect_ThenCategoryCreated() {
        NewCategoryDto newCategory = new NewCategoryDto("name");

        assertThat(categoryService.getCategories(0, 10))
                .as("Check that database is empty before category creation")
                .asList()
                .isEmpty();
        assertThatCode(() -> {
            CategoryDto category = categoryService.createCategory(newCategory);

            assertThat(categoryService.getCategoryById(category.getId()))
                    .as("Check that database contains user after user creation")
                    .isNotNull()
                    .isEqualTo(category);
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createCategory_WhenCategoryWithSuchNameAlreadyExists_ThenThrowsDataIntegrityViolationException() {
        categoryService.createCategory(new NewCategoryDto("name"));
        NewCategoryDto newCategory = new NewCategoryDto("name");

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Check category creation with already existing name")
                .isThrownBy(() -> categoryService.createCategory(newCategory));
    }

    @Test
    void deleteCategory_WhenCategoryDoesNotExist_ThenThrowsEntityNotFoundException() {
        long catId = 1;

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check category deletion when category does not exist")
                .isThrownBy(() -> categoryService.deleteCategory(catId))
                .withMessage(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteCategory_WhenCategoryExists_ThenCategoryDeleted() {
        CategoryDto category = categoryService.createCategory(new NewCategoryDto("name"));

        assertThat(categoryService.getCategoryById(category.getId()))
                .as("Check category existence before deletion")
                .isNotNull()
                .isEqualTo(category);
        assertThatCode(() -> categoryService.deleteCategory(category.getId())).doesNotThrowAnyException();
        assertThat(categoryService.getCategories(0, 10))
                .as("Check that category does not exist after deletion")
                .asList()
                .isEmpty();
    }

    @Test
    void updateCategory_WhenCategoryDoesNotExist_ThenThrowsEntityNotFoundException() {
        long catId = 1;
        NewCategoryDto newCategoryDto = new NewCategoryDto("new_name");

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check category update when category does not exist")
                .isThrownBy(() -> categoryService.updateCategory(newCategoryDto, catId))
                .withMessage(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCategory_WhenCategoryWithSuchNameAlreadyExists_ThenThrowsDataIntegrityViolationException() {
        categoryService.createCategory(new NewCategoryDto("name"));
        CategoryDto categoryDto = categoryService.createCategory(new NewCategoryDto("name2"));
        NewCategoryDto newCategoryDto = new NewCategoryDto("name");

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Check category update when category with such name already exists")
                .isThrownBy(() -> categoryService.updateCategory(newCategoryDto, categoryDto.getId()));
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCategory_WhenDataIsCorrect_ThenCategoryUpdated() {
        String oldName = "old_name";
        String newName = "new_name";
        CategoryDto category = categoryService.createCategory(new NewCategoryDto(oldName));
        NewCategoryDto newCategory = new NewCategoryDto(newName);

        assertThat(categoryService.getCategoryById(category.getId()))
                .as("Check category before update")
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", oldName);
        assertThatCode(() -> categoryService.updateCategory(newCategory, category.getId())).doesNotThrowAnyException();
        assertThat(categoryService.getCategoryById(category.getId()))
                .as("Check that category's name changed after update")
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", newName);
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCategory_WhenNameEqualsOldName_ThenCategoryUpdated() {
        String oldName = "old_name";
        CategoryDto category = categoryService.createCategory(new NewCategoryDto(oldName));
        NewCategoryDto newCategory = new NewCategoryDto(oldName);

        assertThat(categoryService.getCategoryById(category.getId()))
                .as("Check category before update")
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", oldName);
        assertThatCode(() -> categoryService.updateCategory(newCategory, category.getId())).doesNotThrowAnyException();
        assertThat(categoryService.getCategoryById(category.getId()))
                .as("Check that category's name doesn't changed after update")
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", oldName);
    }

    @Test
    void getCategories_WhenDbIsEmpty_ThenReturnEmptyList() {
        assertThatCode(() -> {
            List<CategoryDto> categories = categoryService.getCategories(0, 10);
            assertThat(categories)
                    .as("Check getting empty category list with empty database")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCategories_WhenDbIsNotEmpty_ThenReturnNotEmptyList() {
        CategoryDto category1 = categoryService.createCategory(new NewCategoryDto("name1"));
        CategoryDto category2 = categoryService.createCategory(new NewCategoryDto("name2"));

        assertThatCode(() -> {
            List<CategoryDto> categories = categoryService.getCategories(0, 10);
            assertThat(categories)
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(category1)
                    .contains(category2);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<CategoryDto> categories = categoryService.getCategories(1, 1);
            assertThat(categories)
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(category2);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<CategoryDto> categories = categoryService.getCategories(6, 3);
            assertThat(categories)
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void getCategoryById_WhenCategoryDoesNotExist_ThenThrowsEntityNotFoundException() {
        long catId = 1;

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check getting category when category does not exist")
                .isThrownBy(() -> categoryService.getCategoryById(catId))
                .withMessage(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCategoryById_WhenCategoryExists_ThenReturnCategory() {
        CategoryDto category = categoryService.createCategory(new NewCategoryDto("name"));

        assertThatCode(() -> {
            CategoryDto receivedCategory = categoryService.getCategoryById(category.getId());
            assertThat(receivedCategory)
                    .as("Check getting category when category exists")
                    .isNotNull()
                    .isEqualTo(category);
        }).doesNotThrowAnyException();
    }
}