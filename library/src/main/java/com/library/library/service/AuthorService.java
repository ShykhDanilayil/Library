package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface AuthorService {

    AuthorDto createAuthor(AuthorDto authorDto);

    Page<AuthorDto> getAllAuthors(Pageable pageable);

    Set<BookDto> getAuthorBooks(String nickname);

    boolean isNicknameAlreadyInUse(String nickname);
}
