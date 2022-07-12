package com.library.library.controller.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {

    @NotNull
    private String firstName;
    private String lastName;
    @NotEmpty(message = "User email may not be empty")
    private String email;
}
