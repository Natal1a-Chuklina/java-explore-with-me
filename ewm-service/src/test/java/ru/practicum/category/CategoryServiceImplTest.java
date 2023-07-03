package ru.practicum.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryStorage categoryStorage;
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Captor
    private ArgumentCaptor<Category> requestCaptor;

    @Test
    void createCategory_WhenCategoryWithSuchNameAlreadyExists_ThenThrowsDataIntegrityViolationException() {
        NewCategoryDto input = new NewCategoryDto("name");
        when(categoryStorage.save(any(Category.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Check category creation with already existing name")
                .isThrownBy(() -> categoryService.createCategory(input));

        verify(categoryStorage, Mockito.times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_WhenCategoryDataIsCorrect_ThenCategoryCreated() {
        String name = "name";
        NewCategoryDto input = new NewCategoryDto(name);
        CategoryDto expectedOutput = new CategoryDto(1, name);
        when(categoryStorage.save(any(Category.class))).thenReturn(new Category(1L, name));

        assertThatCode(() -> {
            CategoryDto actualOutput = categoryService.createCategory(input);
            assertThat(actualOutput)
                    .as("Check category creation when input data is correct")
                    .isNotNull()
                    .isEqualTo(expectedOutput);
        }).doesNotThrowAnyException();

        verify(categoryStorage, Mockito.times(1)).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .as("Check passed data to the category's save method argument")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", null)
                .hasFieldOrPropertyWithValue("name", name);
    }

    @Test
    void deleteCategory_WhenCategoryDoesNotExist_ThenThrowsNotFoundException() {
        long catId = 1;
        doThrow(EmptyResultDataAccessException.class).when(categoryStorage).deleteById(catId);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check category deletion when category does not exist")
                .isThrownBy(() -> categoryService.deleteCategory(catId))
                .withMessage(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));

        verify(categoryStorage, Mockito.times(1)).deleteById(catId);
    }

    @Test
    void deleteCategory_WhenCategoryExists_ThenCategoryDeleted() {
        long catId = 1;

        assertThatCode(() -> categoryService.deleteCategory(catId))
                .as("Check category deletion when category exists")
                .doesNotThrowAnyException();

        verify(categoryStorage, Mockito.times(1)).deleteById(catId);
    }

    @Test
    void updateCategory_WhenCategoryDoesNotExist_ThenThrowsNotFoundException() {
        NewCategoryDto input = new NewCategoryDto("new_name");
        long catId = 1;
        when(categoryStorage.findById(catId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check category update when category does not exist")
                .isThrownBy(() -> categoryService.updateCategory(input, catId))
                .withMessage(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));

        verify(categoryStorage, Mockito.times(1)).findById(catId);
        verify(categoryStorage, Mockito.never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryWithSuchNameAlreadyExists_ThenThrowsDataIntegrityViolationException() {
        NewCategoryDto input = new NewCategoryDto("name");
        long catId = 1;
        when(categoryStorage.findById(catId)).thenReturn(Optional.of(new Category(catId, "old_name")));
        when(categoryStorage.save(any(Category.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Check category update when category with such name already exists")
                .isThrownBy(() -> categoryService.updateCategory(input, catId));

        verify(categoryStorage, Mockito.times(1)).findById(catId);
        verify(categoryStorage, Mockito.times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryDataIsCorrect_ThenCategoryUpdated() {
        String name = "new_name";
        NewCategoryDto input = new NewCategoryDto(name);
        long catId = 1;
        CategoryDto expectedOutput = new CategoryDto(catId, name);
        when(categoryStorage.findById(catId)).thenReturn(Optional.of(new Category(catId, "old_name")));

        assertThatCode(() -> {
            CategoryDto actualOutput = categoryService.updateCategory(input, catId);
            assertThat(actualOutput)
                    .as("Check category update when input data is correct")
                    .isNotNull()
                    .isEqualTo(expectedOutput);
        }).doesNotThrowAnyException();

        verify(categoryStorage, Mockito.times(1)).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .as("Check passed data to the category's update method argument")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", catId)
                .hasFieldOrPropertyWithValue("name", name);
    }

    @Test
    void getCategoryById_WhenCategoryDoesNotExist_ThenThrowsNotFoundException() {
        long catId = 1;
        when(categoryStorage.findById(catId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check getting category when category does not exist")
                .isThrownBy(() -> categoryService.getCategoryById(catId))
                .withMessage(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));

        verify(categoryStorage, Mockito.times(1)).findById(catId);
    }

    @Test
    void getCategoryById_WhenCategoryExists_ThenReturnCategory() {
        long catId = 1;
        CategoryDto expectedCategory = new CategoryDto(catId, "name");
        when(categoryStorage.findById(catId)).thenReturn(Optional.of(new Category(catId, "name")));

        assertThatCode(() -> {
            CategoryDto actualOutput = categoryService.getCategoryById(catId);
            assertThat(actualOutput)
                    .as("Check getting category when category exists")
                    .isNotNull()
                    .isEqualTo(expectedCategory);
        }).doesNotThrowAnyException();

        verify(categoryStorage, Mockito.times(1)).findById(catId);
    }
}