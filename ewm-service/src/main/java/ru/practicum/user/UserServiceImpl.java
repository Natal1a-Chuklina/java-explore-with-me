package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserMapper;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        User user = userStorage.save(UserMapper.toUser(newUserRequest));
        log.info("Created user with id = {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(long userId) {
        try {
            userStorage.deleteById(userId);
            log.info("User with id = {} was deleted", userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Attempt to delete nonexistent user by id = {}", userId);
            throw new EntityNotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    @Override
    public List<UserDto> getUsers(Long[] ids, int from, int size) {
        List<User> users;
        if (!ArrayUtils.isEmpty(ids)) {
            users = userStorage.findAllById(Arrays.asList(ids));
        } else {
            Sort sortById = Sort.by(Sort.Direction.ASC, "id");
            Pageable page = PageRequest.of(from / size, size, sortById);
            users = userStorage.findAll(page).getContent();
        }

        log.info("Received {} users", users.size());
        return UserMapper.toUserDto(users);
    }
}
