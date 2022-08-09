package com.library.library.controller;

import com.library.library.controller.dto.UserDto;
import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.IsEmailUser;
import com.library.library.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @ApiOperation(value = "Update user", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/users/{email}")
    public UserDto updateUser(@PathVariable @EmailValid @IsEmailUser String email, @RequestBody @Valid UserDto userDto) {
        return userService.updateUser(email, userDto);
    }

    @ApiOperation(value = "Delete user", authorizations = {@Authorization(value = "basicAuth")})
    @DeleteMapping(value = "/users/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable @EmailValid @IsEmailUser String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}
