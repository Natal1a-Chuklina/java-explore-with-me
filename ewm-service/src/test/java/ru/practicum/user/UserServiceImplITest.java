package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplITest {
    private final UserService userService;

    @Test
    void getUsers_WhenDbIsEmpty_ThenReturnEmptyList() {
        assertThatCode(() -> {
            List<UserDto> users = userService.getUsers(null, 0, 10);
            assertThat(users)
                    .as("Check getting empty user list with empty database")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUsers_WhenIdsExist_ThenReturnUsersWithSuchIds() {
        UserDto user1 = userService.createUser(new NewUserRequest("user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new NewUserRequest("user2", "user2@mail.ru"));

        assertThatCode(() -> {
            List<UserDto> users = userService.getUsers(new Long[]{user1.getId(), user2.getId()}, 0, 10);

            assertThat(users)
                    .as("Check getting not empty users list by list of ids")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(user1)
                    .contains(user2);
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUsers_WhenIdsDoNotExist_ThenReturnUsersPage() {
        UserDto user1 = userService.createUser(new NewUserRequest("user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new NewUserRequest("user2", "user2@mail.ru"));

        assertThatCode(() -> {
            List<UserDto> users = userService.getUsers(null, 0, 10);
            assertThat(users)
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(user1)
                    .contains(user2);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<UserDto> users = userService.getUsers(null, 3, 1);
            assertThat(users)
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<UserDto> users = userService.getUsers(new Long[]{}, 1, 1);
            assertThat(users)
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(user2);
        }).doesNotThrowAnyException();
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ThenThrowsEntityNotFoundException() {
        long userId = 1;

        assertThatExceptionOfType(EntityNotFoundException.class)
                .as("Check user deletion when user does not exist")
                .isThrownBy(() -> userService.deleteUser(userId))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteUser_WhenUserExists_ThenUserDeleted() {
        UserDto user = userService.createUser(new NewUserRequest("user", "user@mail.ru"));

        assertThat(userService.getUsers(new Long[]{user.getId()}, 0, 10))
                .as("Check user existence before deletion")
                .asList()
                .hasSize(1)
                .contains(user);
        assertThatCode(() -> userService.deleteUser(user.getId())).doesNotThrowAnyException();
        assertThat(userService.getUsers(new Long[]{user.getId()}, 0, 10))
                .as("Check that user does not exist after deletion")
                .asList()
                .isEmpty();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createUser_WhenUserIsCorrect_ThenUserCreated() {
        NewUserRequest newUser = new NewUserRequest("name", "email@mail.ru");

        assertThat(userService.getUsers(null, 0, 10))
                .as("Check that database is empty before user creation")
                .asList()
                .isEmpty();
        assertThatCode(() -> {
            UserDto user = userService.createUser(newUser);

            assertThat(userService.getUsers(new Long[]{user.getId()}, 0, 10))
                    .as("Check that database contains user after user creation")
                    .asList()
                    .hasSize(1)
                    .contains(user);
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createUser_WhenUserWithSuchEmailAlreadyExists_ThenThrowsDataIntegrityViolationException() {
        userService.createUser(new NewUserRequest("name1", "email@mail.ru"));
        NewUserRequest newUser = new NewUserRequest("name2", "email@mail.ru");

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Check user creation with already existing mail")
                .isThrownBy(() -> userService.createUser(newUser));
    }
}