package com.library.library.controller;

import com.library.library.controller.dto.UserDto;
import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.IsEmailUser;
import com.library.library.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = "API description for SWAGGER documentation")
@ApiResponses({
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
})
public class AdminController {

    private final UserService userService;

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/users/{email}")
    public UserDto updateUser(@PathVariable @EmailValid @IsEmailUser String email, @RequestBody @Valid UserDto userDto) {
        return userService.updateUser(email, userDto);
    }

    @DeleteMapping(value = "/users/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable @EmailValid @IsEmailUser String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}
