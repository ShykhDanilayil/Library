package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.BookStatus;
import com.library.library.controller.dto.Genre;
import com.library.library.service.exception.BookNotAvailableException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
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
    public void updateBookTest() {
        book.setId(132L);
        //given
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookRepository.save(isA(Book.class))).thenReturn(book);

        //when
        BookDto actual = bookService.updateBook(book.getId(), bookDto);

        //then
        assertEquals(bookDto, actual);
        verify(bookRepository).findById(book.getId());
        verify(bookRepository).save(book);
    }

    @Test
    public void updateBookTestException() {
        book.setId(132L);
        //given
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        //when
        assertThrows(BookNotAvailableException.class, () ->
                bookService.updateBook(book.getId(), bookDto));

        //then
        verify(bookRepository).findById(book.getId());
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void updateBookTitleAndDescriptionAndPagesTest() {
        Book newBook = getBook();
        newBook.setId(132L);
        newBook.setGenre(null);
        newBook.setStatus(null);
        BookDto updateBoolDto = BookMapper.INSTANCE.mapBookDto(newBook);
        //given
        when(bookRepository.findById(newBook.getId())).thenReturn(Optional.of(newBook));
        when(bookRepository.save(isA(Book.class))).thenReturn(newBook);

        //when
        BookDto actual = bookService.updateBook(newBook.getId(), updateBoolDto);

        //then
        assertEquals(updateBoolDto, actual);
        verify(bookRepository).findById(newBook.getId());
        verify(bookRepository).save(newBook);
    }

    @Test
    public void updateBookGenreAndStatusAndPagesAndPubYearTest() {
        Book newBook = getBook();
        newBook.setTitle(null);
        newBook.setDescription(null);
        newBook.setId(132L);
        newBook.setGenre(Genre.DRAMA);
        newBook.setPages(213);
        newBook.setPublicationYear(2132);
        BookDto updateBoolDto = BookMapper.INSTANCE.mapBookDto(newBook);
        //given
        when(bookRepository.findById(newBook.getId())).thenReturn(Optional.of(newBook));
        when(bookRepository.save(isA(Book.class))).thenReturn(newBook);

        //when
        BookDto actual = bookService.updateBook(newBook.getId(), updateBoolDto);

        //then
        assertEquals(updateBoolDto, actual);
        verify(bookRepository).findById(newBook.getId());
        verify(bookRepository).save(newBook);
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

    @Test
    void deleteBookTest() {
        book.setId(132L);
        //given
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        doNothing().when(bookRepository).delete(book);

        //when
        bookService.deleteBook(book.getId());

        //then
        verify(bookRepository).findById(book.getId());
        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBookTestException() {
        book.setId(132L);
        //given
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        //when
        assertThrows(BookNotAvailableException.class, () ->
                bookService.deleteBook(book.getId()));

        //then
        verify(bookRepository).findById(book.getId());
        verify(bookRepository, never()).delete(any());
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
