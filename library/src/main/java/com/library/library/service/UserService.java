package com.library.library.service;

import com.library.library.controller.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto getUser(String email);

    List<UserDto> listUsers();

    UserDto createUser(UserDto userDto, String password);

    UserDto updateUser(String email, UserDto userDto);

    void addLibrary(String email, String libraryName);

    void deleteUser(String email);

}