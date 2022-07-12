package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;

import java.util.Set;

public interface AuthorService {
    AuthorDto getAuthorInfo(Long authorId);

    AuthorDto createAuthor(AuthorDto authorDto);

    Set<BookDto> getAuthorBooks(Long authorId);
}
