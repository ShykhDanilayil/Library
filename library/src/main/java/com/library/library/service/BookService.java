package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookDto getBook(String bookTitle);

    Page<BookDto> getAllBooks(Pageable pageable);

    BookDto createBook(String nickname, BookDto bookDto);

    BookDto updateBook(Long id, BookDto bookDto);

    AuthorDto getAuthorByBook(String bookTitle);

    boolean isExistBookTitle(String bookTitle);

    void deleteBook(Long id);
}
