package com.library.library.service.impl;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.BookStatus;
import com.library.library.service.BookService;
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
    public AuthorDto getAuthorByBook(String bookTitle) {
        log.info("Get author by book title {}", bookTitle);
        Book book = bookRepo.findDistinctFirstByTitle(bookTitle);
        return AuthorMapper.INSTANCE.mapAuthorDto(book.getAuthor());
    }

    @Override
    public boolean isExistBookTitle(String bookTitle) {
        return bookRepo.existsBookByTitle(bookTitle);
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
