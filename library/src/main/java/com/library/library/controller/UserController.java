package com.library.library.controller;

import com.library.library.controller.dto.UserDto;
import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.IsEmail;
import com.library.library.controller.validation.PasswordValid;
import com.library.library.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "API description for SWAGGER documentation")
@ApiResponses({
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
})
public class UserController {

    private final UserService userService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userService.pageUsers(pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{email}")
    public UserDto getUser(@PathVariable @EmailValid @IsEmail String email) {
        return userService.getUser(email);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDto createUser(@RequestBody @Valid UserDto userDto, @PasswordValid @RequestParam String password) {
        return userService.createUser(userDto, password);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{email}")
    public UserDto updateUser(@PathVariable @EmailValid @IsEmail String email, @RequestBody UserDto userDto) {
        return userService.updateUser(email, userDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{email}/library/{libraryName}")
    public void addLibrary(@PathVariable @EmailValid @IsEmail String email, @PathVariable String libraryName) {
        userService.addLibrary(email, libraryName);
    }

    @DeleteMapping(value = "/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable @EmailValid @IsEmail String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}