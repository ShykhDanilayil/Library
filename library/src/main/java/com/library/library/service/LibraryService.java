package com.library.library.service;

import com.library.library.controller.dto.LibraryDto;

import java.util.List;
import java.util.Set;

public interface LibraryService {

    LibraryDto createLibrary(LibraryDto newLibrary);

    LibraryDto getLibrary(String address);

    List<LibraryDto> getAllLibraries();

    LibraryDto addBook(String libraryName, String bookTitle);

    void deleteLibrary(String address);

    Set<LibraryDto> getLibraryByBookTitle(String bookTitle);
}
