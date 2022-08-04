package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.BookStatus;
import com.library.library.service.impl.BookServiceImpl;
import com.library.library.service.mapper.AuthorMapper;
import com.library.library.service.mapper.BookMapper;
import com.library.library.service.model.Author;
import com.library.library.service.model.Book;
import com.library.library.service.repository.AuthorRepository;
import com.library.library.service.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {
    @InjectMocks
    private BookServiceImpl bookService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;


    private final Author author = getAuthor();
    private final AuthorDto authorDto = getAuthorDto();
    private final Book book = getBook();
    private final BookDto bookDto = getBookDto();

    @Test
    void getBookTest() {
        //given
        when(bookRepository.findDistinctFirstByTitle(bookDto.getTitle())).thenReturn(book);

        //when
        BookDto actual = bookService.getBook(book.getTitle());

        //then
        assertEquals(bookDto, actual);
        verify(bookRepository, only()).findDistinctFirstByTitle(bookDto.getTitle());
    }

    @Test
    void getAllBooksTest() {
        Pageable pageable = PageRequest.of(0, 4);

        List<Book> books = new ArrayList<>();
        books.add(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        //given
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        //when
        Page<BookDto> actualPage = bookService.getAllBooks(pageable);

        //then
        List<BookDto> bookDtos = new ArrayList<>();
        bookDtos.add(bookDto);
        Page<BookDto> expectedPage = new PageImpl<>(bookDtos, pageable, bookDtos.size());

        assertEquals(expectedPage, actualPage);
    }

    @Test
    void createBookTest() {
        //given
        when(authorRepository.findAuthorByNickname(author.getNickname())).thenReturn(author);
        when(bookRepository.save(book)).thenReturn(book);

        //when
        BookDto actual = bookService.createBook(author.getNickname(), bookDto);

        //then
        assertEquals(bookDto, actual);
    }


    @Test
    void getAuthorByBookTest() {
        //given
        when(bookRepository.findDistinctFirstByTitle(bookDto.getTitle())).thenReturn(book);

        //when
        AuthorDto actual = bookService.getAuthorByBook(bookDto.getTitle());

        //then
        assertEquals(authorDto, actual);
    }

    @Test
    void isExistBookTitleTest() {
        //given
        when(bookRepository.existsBookByTitle(bookDto.getTitle())).thenReturn(true);

        //when
        boolean actual = bookService.isExistBookTitle(bookDto.getTitle());

        //then
        assertTrue(actual);
        verify(bookRepository, only()).existsBookByTitle(bookDto.getTitle());
    }

    private Book getBook() {
        Book newBook = BookMapper.INSTANCE.mapBook(getBookDto());
        newBook.setAuthor(author);
        return newBook;
    }

    private BookDto getBookDto() {
        return BookDto.builder()
                .title("TEST TITLE")
                .description("TEST DESCRIPTION")
                .status(BookStatus.AVAILABLE)
                .build();
    }

    private Author getAuthor() {
        return AuthorMapper.INSTANCE.mapAuthor(getAuthorDto());
    }

    private AuthorDto getAuthorDto() {
        return AuthorDto.builder()
                .name("Test")
                .nickname("NICKNAME TEST")
                .build();
    }
}
