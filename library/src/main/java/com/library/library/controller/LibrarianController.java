package com.library.library.controller;

import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.IsEmailUser;
import com.library.library.controller.validation.IsNameLibrary;
import com.library.library.controller.validation.IsTitleBook;
import com.library.library.service.LibraryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
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
@RequestMapping("/librarian/libraries")
@RequiredArgsConstructor
public class LibrarianController {

    private final LibraryService libraryService;

    @ApiOperation(value = "Create Library", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping()
    public LibraryDto createLibrary(@RequestBody @Valid LibraryDto libraryDto) {
        return libraryService.createLibrary(libraryDto);
    }

    @ApiOperation(value = "Delete library", authorizations = {@Authorization(value = "basicAuth")})
    @DeleteMapping(value = "/{name}")
    public ResponseEntity<Void> deleteLibrary(@PathVariable @IsNameLibrary String name) {
        libraryService.deleteLibrary(name);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Library add book", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/books")
    public LibraryDto addBook(@RequestParam @IsNameLibrary String libraryName, @RequestParam @IsTitleBook String bookTitle) {
        return libraryService.addBook(libraryName, bookTitle);
    }

    @ApiOperation(value = "Reserved book", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/reserve")
    public void reserveBookControl(@RequestParam @EmailValid @IsEmailUser String userEmail, @RequestParam @IsTitleBook String bookTitle, @RequestParam @IsNameLibrary String libraryName) {
        libraryService.reserveBook(bookTitle, userEmail, libraryName);
    }

    @ApiOperation(value = "Borrow book", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/borrow")
    public void borrowBookControl(@RequestParam @EmailValid @IsEmailUser String userEmail, @RequestParam @IsTitleBook String bookTitle, @RequestParam @IsNameLibrary String libraryName) {
        libraryService.borrowBook(bookTitle, userEmail, libraryName);
    }

    @ApiOperation(value = "Return book", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/return")
    public void returnBookControl(@RequestParam @EmailValid @IsEmailUser String userEmail, @RequestParam @IsTitleBook String bookTitle, @RequestParam @IsNameLibrary String libraryName) throws Exception {
        libraryService.returnBook(bookTitle, userEmail, libraryName, null);
    }
}
