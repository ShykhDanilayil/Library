package com.library.library.service.impl;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.BookStatus;
import com.library.library.service.BookService;
import com.library.library.service.exception.BookNotAvailableException;
import com.library.library.service.mapper.AuthorMapper;
import com.library.library.service.mapper.BookMapper;
import com.library.library.service.model.Author;
import com.library.library.service.model.Book;
import com.library.library.service.repository.AuthorRepository;
import com.library.library.service.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static java.lang.String.format;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepo;
    private final BookRepository bookRepo;

    @Override
    public BookDto getBook(String bookTitle) {
        log.info("Get book with title {}" + bookTitle);
        Book book = bookRepo.findDistinctFirstByTitle(bookTitle);
        return BookMapper.INSTANCE.mapBookDto(book);
    }

    @Override
    public Page<BookDto> getAllBooks(Pageable pageable) {
        log.info("Get page books");
        return bookRepo.findAll(pageable).map(this::mapBookDto);
    }

    @Override
    public BookDto createBook(String nickname, BookDto bookDto) {
        log.info("Create book with title {} and author nickname {}", bookDto.getTitle(), nickname);
        Book newBook = BookMapper.INSTANCE.mapBook(bookDto);
        Author author = authorRepo.findAuthorByNickname(nickname);
        newBook.setStatus(BookStatus.AVAILABLE);
        newBook.setAuthor(author);
        author.getBooks().add(newBook);
        bookRepo.save(newBook);
        log.info("Book with title {} successfully created" + newBook.getTitle());
        return BookMapper.INSTANCE.mapBookDto(newBook);
    }

    @Override
    @Transactional
    public BookDto updateBook(Long id, BookDto bookDto) {
        log.info("Updating book with id {}", id);
        Book book = bookRepo.findById(id).orElseThrow(() ->
                new BookNotAvailableException(format("Book with id %s not available", id)));
        populatedFields(book, bookDto);
        bookRepo.save(book);
        log.info("Book with id {} successfully updated", id);
        return BookMapper.INSTANCE.mapBookDto(book);
    }

    @Override
    public AuthorDto getAuthorByBook(String bookTitle) {
        log.info("Get author by book title {}", bookTitle);
        Book book = bookRepo.findDistinctFirstByTitle(bookTitle);
        return AuthorMapper.INSTANCE.mapAuthorDto(book.getAuthor());
    }

    @Override
    public boolean isExistBookTitle(String bookTitle) {
        return bookRepo.existsBookByTitle(bookTitle);
    }

    @Override
    public void deleteBook(Long id) {
        log.info("Delete Book with id {}", id);
        Book book = bookRepo.findById(id).orElseThrow(() ->
                new BookNotAvailableException(format("Book with id %s not available", id)));
        bookRepo.delete(book);
        log.info("Book with id {} successfully deleted", id);
    }

    private void populatedFields(Book book, BookDto bookDto) {
        if (Objects.nonNull(bookDto.getTitle())) {
            book.setTitle(bookDto.getTitle());
        }
        if (Objects.nonNull(bookDto.getDescription())) {
            book.setDescription(bookDto.getDescription());
        }
        if (bookDto.getPages() != 0) {
            book.setPages(bookDto.getPages());
        }
        if (bookDto.getPublicationYear() != 0) {
            book.setPublicationYear(bookDto.getPublicationYear());
        }
        if (Objects.nonNull(bookDto.getGenre())) {
            book.setGenre(bookDto.getGenre());
        }
        if (Objects.nonNull(bookDto.getStatus())) {
            book.setStatus(bookDto.getStatus());
        }
    }

    private BookDto mapBookDto(Book book) {
        return BookDto.builder()
                .title(book.getTitle())
                .description(book.getDescription())
                .pages(book.getPages())
                .publicationYear(book.getPublicationYear())
                .genre(book.getGenre())
                .status(book.getStatus())
                .build();
    }
}
