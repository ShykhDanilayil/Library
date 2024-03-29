package com.library.library.service.impl;

import com.library.library.controller.dto.Role;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.UserService;
import com.library.library.service.exception.UserAlreadyExistsException;
import com.library.library.service.mapper.UserMapper;
import com.library.library.service.model.User;
import com.library.library.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean isEmailAlreadyInUse(String email) {
        log.info("Checking email {}", email);
        return userRepository.existsUserByEmail(email);
    }

    @Override
    public UserDto getUser(String email) {
        log.info("Search User by email {}", email);
        User user = getUserByEmail(email);
        return UserMapper.INSTANCE.mapUserDto(user);
    }

    @Override
    public Page<UserDto> pageUsers(Pageable pageable) {
        log.info("Get page users");
        return userRepository.findAll(pageable).map(this::mapUserDto);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        String email = userDto.getEmail();
        log.info("Create User with email {}", email);
        if (userRepository.existsUserByEmail(email)) {
            throw new UserAlreadyExistsException(format("User with email %s exists", email));
        }
        User user = UserMapper.INSTANCE.mapUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(Role.USER);
        user.setIsAccountNonLocked(true);
        user.setWrittenOn(Instant.now());
        userRepository.save(user);
        log.info("User with email {} successfully created", email);
        return UserMapper.INSTANCE.mapUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(String email, UserDto userDto) {
        log.info("Updating user with email {}", email);
        User user = getUserByEmail(email);
        populatedFields(user, userDto);
        userRepository.save(user);
        log.info("User with email {} successfully updated", user.getEmail());
        return UserMapper.INSTANCE.mapUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        log.info("Delete User with email {}", email);
        User user = getUserByEmail(email);
        userRepository.delete(user);
        log.info("User with email {} successfully deleted", email);
    }

    private User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    private UserDto mapUserDto(User user) {
        return UserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .isAccountNonLocked(user.getIsAccountNonLocked())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .country(user.getCountry())
                .city(user.getCity())
                .address(user.getAddress())
                .postalCode(user.getPostalCode())
                .build();
    }

    private void populatedFields(User user, UserDto userDto) {
        if (Objects.nonNull(userDto.getFirstName())) {
            user.setFirstName(userDto.getFirstName());
        }
        if (Objects.nonNull(userDto.getLastName())) {
            user.setLastName(userDto.getLastName());
        }
        if (Objects.nonNull(userDto.getEmail())) {
            user.setEmail(userDto.getEmail());
        }
        if (Objects.nonNull(userDto.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        if (Objects.nonNull(userDto.getRole())) {
            user.setRole(userDto.getRole());
        }
        if (Objects.nonNull(userDto.getIsAccountNonLocked())) {
            user.setIsAccountNonLocked(userDto.getIsAccountNonLocked());
        }
        if (Objects.nonNull(userDto.getPhone())) {
            user.setPhone(userDto.getPhone());
        }
        if (Objects.nonNull(userDto.getBirthday())) {
            user.setBirthday(userDto.getBirthday());
        }
        if (Objects.nonNull(userDto.getCountry())) {
            user.setCountry(userDto.getCountry());
        }
        if (Objects.nonNull(userDto.getCity())) {
            user.setCity(userDto.getCity());
        }
        if (Objects.nonNull(userDto.getAddress())) {
            user.setAddress(userDto.getAddress());
        }
        if (Objects.nonNull(userDto.getPostalCode())) {
            user.setPostalCode(userDto.getPostalCode());
        }
    }
}