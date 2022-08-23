package com.library.library.controller;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.validation.IsNickName;
import com.library.library.controller.validation.IsTitleBook;
import com.library.library.controller.validation.PatchGroup;
import com.library.library.service.BookService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @ApiOperation(value = "Get book (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{bookTitle}")
    public BookDto getBook(@PathVariable @IsTitleBook String bookTitle) {
        return bookService.getBook(bookTitle);
    }

    @ApiOperation(value = "Get books page (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<BookDto> getAllBooks(Pageable pageable) {
        return bookService.getAllBooks(pageable);
    }

    @ApiOperation(value = "Create book (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{nickname}")
    public BookDto createBook(@PathVariable @IsNickName String nickname, @RequestBody @Valid BookDto bookDto) {
        return bookService.createBook(nickname, bookDto);
    }

    @ApiOperation(value = "Update all fields book (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{id}")
    public BookDto updateBook(@PathVariable Long id, @RequestBody @Valid BookDto bookDto) {
        return bookService.updateBook(id, bookDto);
    }

    @ApiOperation(value = "Partial update book (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}")
    public BookDto partialUpdateBook(@PathVariable Long id, @RequestBody @Validated(PatchGroup.class) BookDto bookDto) {
        System.out.println(bookDto);
        return bookService.updateBook(id, bookDto);
    }

    @ApiOperation(value = "Get author by book (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{bookTitle}/author")
    public AuthorDto getAuthorByBook(@PathVariable @IsTitleBook String bookTitle) {
        return bookService.getAuthorByBook(bookTitle);
    }

    @ApiOperation(value = "Delete book (LIBRARIAN, ADMIN)", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}
