package com.library.library.controller;

import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.IsEmailUser;
import com.library.library.controller.validation.IsNameLibrary;
import com.library.library.controller.validation.IsTitleBook;
import com.library.library.controller.validation.PatchGroup;
import com.library.library.service.LibraryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @ApiOperation(value = "Update all fields library", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{name}")
    public LibraryDto updateLibrary(@PathVariable @IsNameLibrary String name, @RequestBody @Valid LibraryDto newLibrary) {
        return libraryService.updateLibrary(name, newLibrary);
    }

    @ApiOperation(value = "Partial update library", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{name}")
    public LibraryDto partialUpdateLibrary(@PathVariable @IsNameLibrary String name, @RequestBody @Validated(PatchGroup.class) LibraryDto newLibrary) {
        return libraryService.updateLibrary(name, newLibrary);
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

    @ApiOperation(value = "Library add user", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/users")
    public void addUser(@RequestParam @IsNameLibrary String libraryName, @RequestParam @EmailValid @IsEmailUser String email) {
        libraryService.addUser(libraryName, email);
    }

    @ApiOperation(value = "Library delete user", authorizations = {@Authorization(value = "basicAuth")})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/users")
    public void deleteUser(@RequestParam @IsNameLibrary String libraryName, @RequestParam @EmailValid @IsEmailUser String email) {
        libraryService.deleteUser(libraryName, email);
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
