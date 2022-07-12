package com.library.library.service.impl;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.BookService;
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

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepo;
    private final BookRepository bookRepo;

    @Override
    public BookDto createBook(Long authorId, BookDto bookDto) {
        if (!authorRepo.existsAuthorById(authorId)) {
            throw new EntityNotFoundException(format("Author with id %s is not found", authorId));
        }
        Book newBook = BookMapper.INSTANCE.mapBook(bookDto);
        newBook.setAuthorId(authorId);
        newBook = bookRepo.save(newBook);
        log.info("Book successfully created");
        return BookMapper.INSTANCE.mapBookDto(newBook);
    }

    @Override
    public AuthorDto getAuthorByBook(String bookTitle) {
        log.info("get author by book title {}", bookTitle);
        Book book = bookRepo.getByTitle(bookTitle).orElseThrow(() ->
                new EntityNotFoundException(format("The book with this title {} doesn't exist {}", bookTitle)));
        Author author = authorRepo.getById(book.getAuthorId());
        return AuthorMapper.INSTANCE.mapAuthorDto(author);
    }
}
