package com.library.library.controller;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.validation.IsNickName;
import com.library.library.service.AuthorService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @ApiOperation(value = "Create author (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public AuthorDto createAuthor(@RequestBody @Valid AuthorDto authorDto) {
        return authorService.createAuthor(authorDto);
    }

    @ApiOperation(value = "Get authors page (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<AuthorDto> getAllAuthors(Pageable pageable) {
        return authorService.getAllAuthors(pageable);
    }

    @Validated
    @ApiOperation(value = "Get all books by author (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{nickname}/books")
    public Set<BookDto> getBooksAuthor(@PathVariable @IsNickName String nickname) {
        return authorService.getAuthorBooks(nickname);
    }
}
