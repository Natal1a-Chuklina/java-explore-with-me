package ru.practicum.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.CategoryService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryAdminController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CategoryAdminControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private CategoryService categoryService;

    @SneakyThrows
    @Test
    void createCategory_WhenNoInputBody_ThenReturnBadRequest() {
        assertThat(mockMvc.perform(post("/admin/categories"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidNewCategoryDtosStream")
    void createCategory_WhenInputBodyNotValid_ThenReturnBadRequest(NewCategoryDto input) {
        assertThat(mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    static Stream<NewCategoryDto> notValidNewCategoryDtosStream() {
        NewCategoryDto nullName = new NewCategoryDto(null);
        NewCategoryDto blankName = new NewCategoryDto("  ");
        NewCategoryDto emptyName = new NewCategoryDto("");
        NewCategoryDto longName = new NewCategoryDto(RandomStringUtils.random(51));

        return Stream.of(nullName, blankName, emptyName, longName);
    }

    @SneakyThrows
    @Test
    void createCategory_WhenCategoryWithSuchNameAlreadyExists_ThenReturnConflict() {
        NewCategoryDto input = new NewCategoryDto("name");
        when(categoryService.createCategory(input)).thenThrow(DataIntegrityViolationException.class);

        assertThat(mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.DATA_INTEGRITY_VIOLATION_MESSAGE);

        verify(categoryService, Mockito.times(1)).createCategory(input);
    }

    @SneakyThrows
    @Test
    void createCategory_WhenAllDataValid_ThenReturnCreated() {
        NewCategoryDto input = new NewCategoryDto("name");
        CategoryDto expectedOutput = new CategoryDto(1, "name");
        when(categoryService.createCategory(input)).thenReturn(expectedOutput);

        String output = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Check return value when creating category")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(expectedOutput));
        verify(categoryService, Mockito.times(1)).createCategory(input);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidPathVariablesStream")
    void deleteCategory_WhenPathVariableNotValid_ThenReturnBadRequest(Object pathVariable) {
        assertThat(mockMvc.perform(delete("/admin/categories/{catId}", pathVariable))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    static Stream<Object> notValidPathVariablesStream() {
        return Stream.of(-1, 0, "wrong_value");
    }

    @SneakyThrows
    @Test
    void deleteCategory_WhenCategoryDoesNotExist_ThenReturnNotFound() {
        long catId = 1;
        doThrow(EntityNotFoundException.class).when(categoryService).deleteCategory(catId);

        assertThat(mockMvc.perform(delete("/admin/categories/{catId}", catId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.NOT_FOUND_MESSAGE);

        verify(categoryService, Mockito.times(1)).deleteCategory(catId);
    }

    @SneakyThrows
    @Test
    void deleteCategory_WhenCategoryExists_ThenReturnNoContent() {
        long catId = 1;

        mockMvc.perform(delete("/admin/categories/{catId}", catId))
                .andExpect(status().isNoContent());

        verify(categoryService, Mockito.times(1)).deleteCategory(catId);
    }

    @SneakyThrows
    @Test
    void updateCategory_WhenNoInputBody_ThenReturnBadRequest() {
        assertThat(mockMvc.perform(patch("/admin/categories/{catId}", 1))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidNewCategoryDtosStream")
    void updateCategory_WhenInputBodyNotValid_ThenReturnBadRequest(NewCategoryDto input) {
        assertThat(mockMvc.perform(patch("/admin/categories/{catId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidPathVariablesStream")
    void updateCategory_WhenPathVariableNotValid_ThenReturnBadRequest(Object pathVariable) {
        NewCategoryDto input = new NewCategoryDto("name");

        assertThat(mockMvc.perform(patch("/admin/categories/{catId}", pathVariable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    @SneakyThrows
    @Test
    void updateCategory_WhenCategoryDoesNotExist_ThenReturnNotFound() {
        long catId = 1;
        NewCategoryDto input = new NewCategoryDto("name");
        doThrow(EntityNotFoundException.class).when(categoryService).updateCategory(input, catId);

        assertThat(mockMvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.NOT_FOUND_MESSAGE);

        verify(categoryService, Mockito.times(1)).updateCategory(input, catId);
    }

    @SneakyThrows
    @Test
    void updateCategory_WhenCategoryWithSuchNameAlreadyExists_ThenReturnConflict() {
        long catId = 1;
        NewCategoryDto input = new NewCategoryDto("name");
        when(categoryService.updateCategory(input, catId)).thenThrow(DataIntegrityViolationException.class);

        assertThat(mockMvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.DATA_INTEGRITY_VIOLATION_MESSAGE);

        verify(categoryService, Mockito.times(1)).updateCategory(input, catId);
    }

    @SneakyThrows
    @Test
    void updateCategory_WhenAllDataValid_ThenReturnOk() {
        long catId = 1;
        NewCategoryDto input = new NewCategoryDto("name");
        CategoryDto expectedOutput = new CategoryDto(1, "name");
        when(categoryService.updateCategory(input, catId)).thenReturn(expectedOutput);

        String output = mockMvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Check return value when updating category")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(expectedOutput));
        verify(categoryService, Mockito.times(1)).updateCategory(input, catId);
    }
}