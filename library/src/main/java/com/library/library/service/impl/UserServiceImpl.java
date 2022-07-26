package com.library.library.service.impl;

import com.library.library.controller.dto.UserDto;
import com.library.library.service.UserService;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.UserAlreadyExistsException;
import com.library.library.service.mapper.UserMapper;
import com.library.library.service.model.Library;
import com.library.library.service.model.Role;
import com.library.library.service.model.User;
import com.library.library.service.repository.LibraryRepository;
import com.library.library.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;

    @Override
    public UserDto getUser(String email) {
        log.info("Search User by email {}", email);
        User user = userRepository.findUserByEmail(email).orElseThrow(() ->
                new EntityNotFoundException(format("User with email %s is not found", email)));
        return UserMapper.INSTANCE.mapUserDto(user);
    }

    @Override
    public Page<UserDto> pageUsers(Pageable pageable) {
        log.info("Get page users");
        return userRepository.findAll(pageable).map(UserDto::new);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto, String password) {
        String email = userDto.getEmail();
        log.info("Create User with email {}", email);
        if (userRepository.existsUserByEmail(email)) {
            throw new UserAlreadyExistsException(format("User with email %s exists", email));
        }
        User user = UserMapper.INSTANCE.mapUser(userDto);
        user.setPassword(password);
        user.setWrittenOn(Instant.now());
        user = userRepository.save(user);
        log.info("User with email {} successfully created", email);
        return UserMapper.INSTANCE.mapUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(String email, UserDto userDto) {
        log.info("Updating user with email {}", email);
        User user = UserMapper.INSTANCE.mapUser(getUser(email));
        populateUserWithPresentUserDtoFields(user, userDto);
        userRepository.save(user);
        log.info("User with email {} successfully updated", user.getEmail());
        return UserMapper.INSTANCE.mapUserDto(user);
    }

    @Override
    @Transactional
    public void addLibrary(String email, String libraryName) {
        log.info("User with email {} add library with name {}", email, libraryName);
        User user = UserMapper.INSTANCE.mapUser(getUser(email));
        Library library = libraryRepository.getLibraryByLibraryName(libraryName).orElseThrow(() ->
                new EntityNotFoundException(format("Library with name %s is not found", libraryName)));

        user.getLibraries().add(library);
        library.getUsers().add(user);

        userRepository.save(user);
        libraryRepository.save(library);
        log.info("User with email {} successfully added library with name {}", email, libraryName);
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        log.info("Delete User with email {}", email);
        UserDto userDto = getUser(email);
        userRepository.delete(UserMapper.INSTANCE.mapUser(userDto));
        log.info("User with email {} successfully deleted", email);
    }

    private User populateUserWithPresentUserDtoFields(User user, UserDto userDto) {
        if (Objects.nonNull(userDto.getFirstName())) {
            user.setFirstName(userDto.getFirstName());
        }
        if (Objects.nonNull(userDto.getLastName())) {
            user.setLastName(userDto.getLastName());
        }
        if (Objects.nonNull(userDto.getEmail())) {
            user.setEmail(userDto.getEmail());
        }
        return user;
    }
}