package ru.practicum.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private UserServiceImpl userService;
    @Captor
    private ArgumentCaptor<User> requestCaptor;

    @Test
    void createUser_WhenUserWithSuchEmailAlreadyExists_ThenThrowsDataIntegrityViolationException() {
        NewUserRequest input = new NewUserRequest("name", "mail@mail.ru");
        when(userStorage.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Check user creation with already existing email")
                .isThrownBy(() -> userService.createUser(input));

        verify(userStorage, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void createUser_WhenUserDataIsCorrect_ThenUserCreated() {
        String name = "name";
        String email = "mail@mail.ru";
        NewUserRequest input = new NewUserRequest(name, email);
        UserDto expectedOutput = new UserDto(1L, name, email);
        when(userStorage.save(any(User.class))).thenReturn(new User(1L, name, email));

        assertThatCode(() -> {
            UserDto actualOutput = userService.createUser(input);
            assertThat(actualOutput)
                    .as("Check user creation when input data is correct")
                    .isNotNull()
                    .isEqualTo(expectedOutput);
        }).doesNotThrowAnyException();

        verify(userStorage, Mockito.times(1)).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .as("Check passed data to the user's save method argument")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", null)
                .hasFieldOrPropertyWithValue("name", name)
                .hasFieldOrPropertyWithValue("email", email);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        doThrow(EmptyResultDataAccessException.class).when(userStorage).deleteById(userId);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check user deletion when user does not exist")
                .isThrownBy(() -> userService.deleteUser(userId))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));

        verify(userStorage, Mockito.times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_WhenUserExists_ThenUserDeleted() {
        long userId = 1;

        assertThatCode(() -> userService.deleteUser(userId))
                .as("Check user deletion when user exists")
                .doesNotThrowAnyException();

        verify(userStorage, Mockito.times(1)).deleteById(userId);
    }
}