package com.library.library.controller;

import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.IsEmailUser;
import com.library.library.controller.validation.IsNameLibrary;
import com.library.library.controller.validation.IsTitleBook;
import com.library.library.service.LibraryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/libraries")
@RequiredArgsConstructor
@Api(tags = "API description for SWAGGER documentation")
@ApiResponses({
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
})
public class LibraryController {

    private final LibraryService libraryService;

    @ApiOperation("All libraries page")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<LibraryDto> getAllLibraries(Pageable pageable) {
        return libraryService.getPageLibraries(pageable);
    }

    @ApiOperation("Get library")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{name}")
    public LibraryDto getLibrary(@PathVariable @IsNameLibrary String name) {
        return libraryService.getLibrary(name);
    }

    @ApiOperation("Library add user")
    @PreAuthorize("hasAnyRole('USER','LIBRARIAN')")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/users")
    public void addUser(@RequestParam @IsNameLibrary String libraryName, @RequestParam @EmailValid @IsEmailUser String email) {
        libraryService.addUser(libraryName, email);
    }

    @ApiOperation("Search book in another library")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/books")
    public Set<LibraryDto> getAllLibrariesByBookTitle(@RequestParam @IsTitleBook String bookTitle) {
        return libraryService.getAllLibrariesByBookTitle(bookTitle);
    }

    @ApiOperation("Show all books in library")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/books/{libraryName}")
    public Set<BookDto> getAllBooks(@PathVariable @IsNameLibrary String libraryName) {
        return libraryService.getAllBooks(libraryName);
    }

    @ApiOperation("Reserved book")
    @PreAuthorize("hasAnyRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/reserve")
    public void reserveBook(@RequestParam @EmailValid @IsEmailUser String userEmail, @RequestParam @IsTitleBook String bookTitle, @RequestParam String libraryName) {
        libraryService.reserveBook(bookTitle, userEmail, libraryName);
    }

    @ApiOperation("Borrow book")
    @PreAuthorize("hasAnyRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/borrow")
    public void borrowBook(@RequestParam @IsTitleBook String bookTitle, @RequestParam @IsNameLibrary String libraryName, Principal principal) {
        libraryService.borrowBook(bookTitle, principal.getName(), libraryName);
    }

    @ApiOperation("Return book")
    @PreAuthorize("hasAnyRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/return")
    public void returnBook(@RequestParam @IsTitleBook String bookTitle, @RequestParam @IsNameLibrary String libraryName, @AuthenticationPrincipal UserDetails activeUser) throws Exception {
        libraryService.returnBook(bookTitle, activeUser.getUsername(), libraryName);
    }
}
