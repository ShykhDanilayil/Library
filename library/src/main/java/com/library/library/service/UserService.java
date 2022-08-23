package com.library.library.service;

import com.library.library.controller.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    boolean isEmailAlreadyInUse(String email);

    UserDto getUser(String email);

    Page<UserDto> pageUsers(Pageable pageable);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(String email, UserDto userDto);

    void deleteUser(String email);
}