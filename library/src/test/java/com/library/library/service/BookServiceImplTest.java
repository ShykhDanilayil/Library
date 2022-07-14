package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.exception.EntityNotFoundException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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


    private final Book book = getBook();
    private final BookDto bookDto = getBookDto();
    private final Author author = getAuthor();

    @Test
    void createBookTest() {
        book.setAuthorId(author.getId());
        //given
        when(authorRepository.existsAuthorById(author.getId())).thenReturn(true);
        when(bookRepository.save(book)).thenReturn(book);

        //when
        BookDto actual = bookService.createBook(author.getId(), bookDto);

        //then
        assertEquals(bookDto, actual);
    }

    @Test
    void createBookWithExceptionTest() {
        when(authorRepository.existsAuthorById(author.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class,
                () -> bookService.createBook(author.getId(), bookDto));
        verify(authorRepository, only()).existsAuthorById(author.getId());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void getAuthorByBookTest() {
        //given
        when(bookRepository.getByTitle(bookDto.getTitle())).thenReturn(Optional.of(book));
        when(authorRepository.getById(book.getAuthorId())).thenReturn(author);

        //when
        AuthorDto actual = bookService.getAuthorByBook(bookDto.getTitle());

        //then
        assertEquals(getAuthorDto(), actual);
    }

    @Test
    void getAuthorByBookWithExceptionTest() {
        when(bookRepository.getByTitle(bookDto.getTitle())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> bookService.getAuthorByBook(bookDto.getTitle()));
        verify(bookRepository, only()).getByTitle(bookDto.getTitle());
        verify(authorRepository, never()).getById(book.getAuthorId());
    }

    @Test
    void getAuthorByBookWithExceptionTest2() {
        when(bookRepository.getByTitle(bookDto.getTitle())).thenReturn(Optional.of(book));
        when(authorRepository.getById(book.getAuthorId())).thenThrow(NullPointerException.class);
        assertThrows(NullPointerException.class,
                () -> bookService.getAuthorByBook(bookDto.getTitle()));
        verify(bookRepository, only()).getByTitle(bookDto.getTitle());
        verify(authorRepository, only()).getById(book.getAuthorId());
    }

    private Book getBook() {
        return BookMapper.INSTANCE.mapBook(getBookDto());
    }

    private BookDto getBookDto() {
        return BookDto.builder()
                .title("TEST TITLE")
                .description("TEST DESCRIPTION")
                .build();
    }

    private Author getAuthor() {
        return AuthorMapper.INSTANCE.mapAuthor(getAuthorDto());
    }

    private AuthorDto getAuthorDto() {
        return AuthorDto.builder()
                .id(32L)
                .name("Test")
                .nickname("NICKNAME TEST")
                .build();
    }
}
