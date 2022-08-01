package com.library.library.controller;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.validation.IsNickName;
import com.library.library.controller.validation.IsTitleBook;
import com.library.library.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

@Validated
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Api(tags = "API description for SWAGGER documentation")
@ApiResponses({
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
})
public class BookController {

    private final BookService bookService;

    @ApiOperation("Get book")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{bookTitle}")
    public BookDto getBook(@PathVariable @IsTitleBook String bookTitle) {
        return bookService.getBook(bookTitle);
    }

    @ApiOperation("All books page")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<BookDto> getAllBooks(Pageable pageable) {
        return bookService.getAllBooks(pageable);
    }

    @ApiOperation("Create book")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{nickname}")
    public BookDto createBook(@PathVariable @IsNickName String nickname, @RequestBody @Valid BookDto bookDto) {
        return bookService.createBook(nickname, bookDto);
    }

    @ApiOperation("Get author by book")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{bookTitle}/author")
    public AuthorDto getAuthorByBook(@PathVariable @IsTitleBook String bookTitle) {
        return bookService.getAuthorByBook(bookTitle);
    }
}
