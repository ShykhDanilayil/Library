package com.library.library.service;

import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.LibraryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface LibraryService {

    LibraryDto createLibrary(LibraryDto newLibrary);

    LibraryDto getLibrary(String libraryName);

    Page<LibraryDto> getPageLibraries(Pageable pageable);

    LibraryDto addBook(String libraryName, String bookTitle);

    void addUser(String libraryName, String email);

    Set<LibraryDto> getAllLibrariesByBookTitle(String bookTitle);

    Set<BookDto> getAllBooks(String libraryName);

    void deleteLibrary(String libraryName);

    boolean isNameAlreadyInUse(String name);

    boolean isEmailAlreadyInUse(String email);

    void reserveBook(String bookTitle, String userEmail, String libraryName);

    void borrowBook(String bookTitle, String userEmail, String libraryName);

    void returnBook(String bookTitle, String userEmail, String libraryName);
}
