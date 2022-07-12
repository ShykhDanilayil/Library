package com.library.library.service.impl;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.AuthorService;
import com.library.library.service.exception.AuthorAlreadyExistsException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.mapper.AuthorMapper;
import com.library.library.service.mapper.BookMapper;
import com.library.library.service.model.Author;
import com.library.library.service.model.Book;
import com.library.library.service.repository.AuthorRepository;
import com.library.library.service.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepo;
    private final BookRepository bookRepo;

    @Override
    public AuthorDto getAuthorInfo(Long authorId) {
        Author author = authorRepo.findById(authorId).orElseThrow(() ->
                new EntityNotFoundException(format("Author with id %s is not found", authorId)));
        return AuthorMapper.INSTANCE.mapAuthorDto(author);
    }

    @Override
    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        if (authorRepo.existsAuthorByAuthorNameAndNickname(authorDto.getName(), authorDto.getNickname())) {
            throw new AuthorAlreadyExistsException(format("Author with name %s  and nickname %s exists", authorDto.getName(), authorDto.getNickname()));
        }
        Author newAuthor = authorRepo.save(AuthorMapper.INSTANCE.mapAuthor(authorDto));
        log.info("Author with id {} successfully created", authorDto.getId());
        return AuthorMapper.INSTANCE.mapAuthorDto(newAuthor);
    }

    @Override
    public Set<BookDto> getAuthorBooks(Long authorId) {
        Author author = authorRepo.findById(authorId).orElseThrow(() ->
                new EntityNotFoundException(format("Author with id %s is not found", authorId)));
        Set<Book> books = author.getBooks();
        return BookMapper.INSTANCE.mapBookDtos(books);
    }
}
