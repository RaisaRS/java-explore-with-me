package ru.practicum.explore.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.user.User;
import ru.practicum.explore.user.UserRepository;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.dto.UserMapper;
import ru.practicum.explore.util.CreateRequest;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;


    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User saveUser = userRepository.save(user);
        log.info("Добавлен пользователь {} ", userDto);
        return UserMapper.toUserDto(saveUser);
    }

    @Override
    @Transactional
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageRequest = CreateRequest.createRequest(from, size);
        List<User> listUserDtos = ids != null
                ? userRepository.findAllByIdIn(ids, pageRequest).getContent()
                : userRepository.findAll(pageRequest).getContent();
        log.info("Получен список пользователей");
        return listUserDtos.isEmpty() ? Collections.emptyList() : UserMapper.listToUserDtos(listUserDtos);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удалён пользователь (id): {}", userId);
        userRepository.deleteById(userId);
    }
}
