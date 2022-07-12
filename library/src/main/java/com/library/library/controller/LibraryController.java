package com.library.library.controller;

import com.library.library.controller.dto.LibraryDto;
import com.library.library.service.LibraryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/library")
@RequiredArgsConstructor
@Api(tags = "API description for SWAGGER documentation")
@ApiResponses({
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Internal Server Error")
})
public class LibraryController {

    private final LibraryService libraryService;

    @ApiOperation("Create Library")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public LibraryDto createLibrary(@RequestBody LibraryDto libraryDto) {
        return libraryService.createLibrary(libraryDto);
    }

    @ApiOperation("All libraries")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<LibraryDto> getAllLibraries() {
        return libraryService.getAllLibraries();
    }

    @ApiOperation("Get library")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{address}")
    public LibraryDto getLibrary(@PathVariable String address) {
        return libraryService.getLibrary(address);
    }

    @ApiOperation("Delete library")
    @DeleteMapping(value = "/{address}")
    public ResponseEntity<Void> deleteLibrary(@PathVariable String address) {
        libraryService.deleteLibrary(address);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("Library add book")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "{libraryName}/book/{bookTitle}")
    public LibraryDto addBook(@PathVariable String libraryName, @PathVariable String bookTitle) {
        return libraryService.addBook(libraryName, bookTitle);
    }

    @ApiOperation("Get library by books title")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/book/{bookTitle}")
    public Set<LibraryDto> getLibraryByBookTitle(@PathVariable String bookTitle) {
        return libraryService.getLibraryByBookTitle(bookTitle);
    }
}
