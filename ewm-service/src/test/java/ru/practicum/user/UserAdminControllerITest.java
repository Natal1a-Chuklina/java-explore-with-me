package ru.practicum.user;

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
import ru.practicum.user.controller.UserAdminController;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserAdminControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @SneakyThrows
    @Test
    void createUser_WhenNoInputBody_ThenReturnBadRequest() {
        assertThat(mockMvc.perform(post("/admin/users"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(userService);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidNewUserRequestsStream")
    void createUser_WhenInputBodyNotValid_ThenReturnBadRequest(NewUserRequest input) {
        assertThat(mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(userService);
    }

    static Stream<NewUserRequest> notValidNewUserRequestsStream() {
        NewUserRequest nullName = new NewUserRequest(null, "email@mail.ru");
        NewUserRequest blankName = new NewUserRequest("  ", "email@mail.ru");
        NewUserRequest shortName = new NewUserRequest("a", "email@mail.ru");
        NewUserRequest longName = new NewUserRequest(RandomStringUtils.random(251), "email@mail.ru");
        NewUserRequest nullEmail = new NewUserRequest("name", null);
        NewUserRequest blankEmail = new NewUserRequest("name", "  ");
        NewUserRequest shortEmail = new NewUserRequest("name", "m@.ru");
        NewUserRequest longEmail = new NewUserRequest("name", RandomStringUtils.random(250) + "@mail.ru");
        NewUserRequest incorrectEmail = new NewUserRequest("name", "wrong_email");

        return Stream.of(nullName, blankName, shortName, longName, nullEmail, blankEmail, shortEmail, longEmail,
                incorrectEmail);
    }

    @SneakyThrows
    @Test
    void createUser_WhenEmailAlreadyExists_ThenReturnConflict() {
        NewUserRequest input = new NewUserRequest("name", "mail@mail.ru");
        when(userService.createUser(input)).thenThrow(DataIntegrityViolationException.class);

        assertThat(mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.DATA_INTEGRITY_VIOLATION_MESSAGE);

        verify(userService, Mockito.times(1)).createUser(input);
    }

    @SneakyThrows
    @Test
    void createUser_WhenAllDataValid_ThenReturnCreated() {
        NewUserRequest input = new NewUserRequest("name", "mail@mail.ru");
        UserDto expectedOutput = new UserDto(1, "name", "mail@mail.ru");
        when(userService.createUser(input)).thenReturn(expectedOutput);

        String output = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Check return value when creating user")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(expectedOutput));
        verify(userService, Mockito.times(1)).createUser(input);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidPathVariablesStream")
    void deleteUser_WhenPathVariableNotValid_ThenReturnBadRequest(Object pathVariable) {
        assertThat(mockMvc.perform(delete("/admin/users/{userId}", pathVariable))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(userService);
    }

    static Stream<Object> notValidPathVariablesStream() {
        return Stream.of(-1, 0, "wrong_value");
    }

    @SneakyThrows
    @Test
    void deleteUser_WhenUserDoesNotExist_ThenReturnNotFound() {
        long userId = 1;
        doThrow(EntityNotFoundException.class).when(userService).deleteUser(userId);

        assertThat(mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.NOT_FOUND_MESSAGE);

        verify(userService, Mockito.times(1)).deleteUser(userId);
    }

    @SneakyThrows
    @Test
    void deleteUser_WhenUserExists_ThenReturnNoContent() {
        long userId = 1;

        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService, Mockito.times(1)).deleteUser(userId);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidFromParamStream")
    void getUsers_WhenFromRequestParamIsNotValid_ThenReturnBadRequest(String input) {
        assertThat(mockMvc.perform(get("/admin/users")
                        .param("from", input))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(userService);
    }

    static Stream<String> notValidFromParamStream() {
        String negativeValue = "-100";
        String wrongValue = "wrong_value";

        return Stream.of(negativeValue, wrongValue);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidSizeParamStream")
    void getUsers_WhenSizeRequestParamIsNotValid_ThenReturnBadRequest(String input) {
        assertThat(mockMvc.perform(get("/admin/users")
                        .param("size", input))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(userService);
    }

    static Stream<String> notValidSizeParamStream() {
        String negativeValue = "-100";
        String zeroValue = "0";
        String wrongValue = "wrong_value";

        return Stream.of(negativeValue, zeroValue, wrongValue);
    }

    @SneakyThrows
    @Test
    void getUsers_WhenIdsRequestParamIsNotValid_ThenReturnBadRequest() {
        assertThat(mockMvc.perform(get("/admin/users")
                        .param("ids", "wrong_value"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getUsers_WhenNoRequestParams_ThenReturnOk() {
        List<UserDto> expectedOutput = List.of(new UserDto(1L, "name1", "mail1@mail.ru"),
                new UserDto(2L, "name2", "mail2@mail.ru"));
        when(userService.getUsers(null, 0, 10)).thenReturn(expectedOutput);

        String actualOutput = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Check return value when getting users")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(expectedOutput));

        verify(userService, Mockito.times(1)).getUsers(null, 0, 10);
    }

    @SneakyThrows
    @Test
    void getUsers_WhenRequestParamsExist_ThenReturnOk() {
        Long[] ids = {1L, 2L, 3L};
        int from = 1;
        int size = 5;

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1,2,3")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(userService, Mockito.times(1)).getUsers(ids, from, size);
    }
}