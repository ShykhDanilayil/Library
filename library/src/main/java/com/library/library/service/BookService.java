package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;

public interface BookService {
    BookDto createBook(Long authorId, BookDto bookDto);

    AuthorDto getAuthorByBook(String bookTitle);
}
