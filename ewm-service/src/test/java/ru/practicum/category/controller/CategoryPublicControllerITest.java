package ru.practicum.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.CategoryService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryPublicController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CategoryPublicControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private CategoryService categoryService;

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidFromParamStream")
    void getCategories_WhenFromRequestParamIsNotValid_ThenReturnBadRequest(String input) {
        assertThat(mockMvc.perform(get("/categories")
                        .param("from", input))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    static Stream<String> notValidFromParamStream() {
        String negativeValue = "-100";
        String wrongValue = "wrong_value";

        return Stream.of(negativeValue, wrongValue);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidSizeParamStream")
    void getCategories_WhenSizeRequestParamIsNotValid_ThenReturnBadRequest(String input) {
        assertThat(mockMvc.perform(get("/categories")
                        .param("size", input))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(categoryService);
    }

    static Stream<String> notValidSizeParamStream() {
        String negativeValue = "-100";
        String zeroValue = "0";
        String wrongValue = "wrong_value";

        return Stream.of(negativeValue, zeroValue, wrongValue);
    }

    @SneakyThrows
    @Test
    void getCategories_WhenNoRequestParams_ThenReturnOk() {
        List<CategoryDto> expectedOutput = List.of(new CategoryDto(1L, "name1"),
                new CategoryDto(2L, "name2"));
        when(categoryService.getCategories(0, 10)).thenReturn(expectedOutput);

        String actualOutput = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Check return value when getting categories")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(expectedOutput));
        verify(categoryService, Mockito.times(1)).getCategories(0, 10);
    }

    @SneakyThrows
    @Test
    void getCategories_WhenRequestParamsExist_ThenReturnOk() {
        int from = 1;
        int size = 5;

        mockMvc.perform(get("/categories")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(categoryService, Mockito.times(1)).getCategories(from, size);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidPathVariablesStream")
    void getCategoryById_WhenPathVariableNotValid_ThenReturnBadRequest(Object pathVariable) {
        assertThat(mockMvc.perform(get("/categories/{catId}", pathVariable))
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
    void getCategoryById_WhenCategoryDoesNotExist_ThenReturnNotFound() {
        long catId = 1;
        when(categoryService.getCategoryById(catId)).thenThrow(EntityNotFoundException.class);

        assertThat(mockMvc.perform(get("/categories/{catId}", catId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.NOT_FOUND_MESSAGE);

        verify(categoryService, Mockito.times(1)).getCategoryById(catId);
    }

    @SneakyThrows
    @Test
    void getCategoryById_WhenCategoryExists_ThenReturnOk() {
        long catId = 1;
        CategoryDto expectedOutput = new CategoryDto(catId, "name");
        when(categoryService.getCategoryById(catId)).thenReturn(expectedOutput);

        String actualOutput = mockMvc.perform(get("/categories/{catId}", catId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Check return value when getting category by id")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(expectedOutput));
        verify(categoryService, Mockito.times(1)).getCategoryById(catId);
    }
}