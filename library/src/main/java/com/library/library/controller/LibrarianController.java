package com.library.library.controller;

import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.validation.IsNameLibrary;
import com.library.library.controller.validation.IsTitleBook;
import com.library.library.service.LibraryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/librarian")
@RequiredArgsConstructor
@Api(tags = "API description for SWAGGER documentation")
@ApiResponses({
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
})
public class LibrarianController {

    private final LibraryService libraryService;

    @ApiOperation("Create Library")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/libraries")
    public LibraryDto createLibrary(@RequestBody @Valid LibraryDto libraryDto) {
        return libraryService.createLibrary(libraryDto);
    }

    @ApiOperation("Delete library")
    @DeleteMapping(value = "/libraries/{name}")
    public ResponseEntity<Void> deleteLibrary(@PathVariable @IsNameLibrary String name) {
        libraryService.deleteLibrary(name);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("Library add book")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/books")
    public LibraryDto addBook(@RequestParam @IsNameLibrary String libraryName, @RequestParam @IsTitleBook String bookTitle) {
        return libraryService.addBook(libraryName, bookTitle);
    }
}
