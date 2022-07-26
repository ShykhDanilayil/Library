package com.library.library.service;

import com.library.library.controller.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDto getUser(String email);

    Page<UserDto> pageUsers(Pageable pageable);

    UserDto createUser(UserDto userDto, String password);

    UserDto updateUser(String email, UserDto userDto);

    void addLibrary(String email, String libraryName);

    void deleteUser(String email);
}