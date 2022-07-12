package com.library.library.service.mapper;

import com.library.library.controller.dto.UserDto;
import com.library.library.service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto mapUserDto(User user);

    User mapUser(UserDto userDto);

    List<UserDto> mapPageUserDto(List<User> users);
}
