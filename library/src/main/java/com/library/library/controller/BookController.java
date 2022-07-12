package com.library.library.controller;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
@Api(tags = "API description for SWAGGER documentation")
@ApiResponses({
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
})
public class BookController {

    private final BookService bookService;

    @ApiOperation("Create book")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{authorId}")
    public BookDto createBook(@PathVariable Long authorId, @RequestBody BookDto bookDto) {
        return bookService.createBook(authorId, bookDto);
    }

    @ApiOperation("Get author by book")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{bookTitle}")
    public AuthorDto getAuthorByBook(@PathVariable String bookTitle) {
        return bookService.getAuthorByBook(bookTitle);
    }
}
