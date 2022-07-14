package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.exception.AuthorAlreadyExistsException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.impl.AuthorServiceImpl;
import com.library.library.service.mapper.AuthorMapper;
import com.library.library.service.mapper.BookMapper;
import com.library.library.service.model.Author;
import com.library.library.service.model.Book;
import com.library.library.service.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceImplTest {

    @InjectMocks
    private AuthorServiceImpl libraryService;
    @Mock
    private AuthorRepository authorRepository;


    private final Book book = getBook();
    private final Author author = getAuthor();
    private final AuthorDto authorDto = getAuthorDto();

    @Test
    void getAuthorInfoTest() {
        //given
        when(authorRepository.findById(author.getId())).thenReturn(Optional.of(author));
        //when
        AuthorDto expected = AuthorMapper.INSTANCE.mapAuthorDto(author);
        AuthorDto actual = libraryService.getAuthorInfo(author.getId());
        //then
        assertEquals(expected, actual);
    }

    @Test
    void getAuthorInfoWithExceptionTest() {
        when(authorRepository.findById(author.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> libraryService.getAuthorInfo(author.getId()));
    }

    @Test
    void createAuthorTest() {
        //given
        when(authorRepository.existsAuthorByAuthorNameAndNickname(authorDto.getName(), authorDto.getNickname())).thenReturn(false);
        when(authorRepository.save(author)).thenReturn(author);

        //when
        AuthorDto actual = libraryService.createAuthor(authorDto);

        //then
        assertEquals(authorDto, actual);
    }

    @Test
    void createAuthorWithExceptionTest() {
        when(authorRepository.existsAuthorByAuthorNameAndNickname(authorDto.getName(), authorDto.getNickname())).thenReturn(true);
        assertThrows(AuthorAlreadyExistsException.class,
                () -> libraryService.createAuthor(authorDto));
        verify(authorRepository, only()).existsAuthorByAuthorNameAndNickname(authorDto.getName(), authorDto.getNickname());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void getAuthorBooksTest() {
        author.setBooks(Collections.singleton(book));
        Set<BookDto> expectedBooks = BookMapper.INSTANCE.mapBookDtos(author.getBooks());
        //given
        when(authorRepository.findById(author.getId())).thenReturn(Optional.of(author));

        //when
        Set<BookDto> actualBooks = libraryService.getAuthorBooks(author.getId());

        //then
        assertEquals(expectedBooks, actualBooks);
    }

    @Test
    void getAuthorBooksWithExceptionTest() {
        when(authorRepository.findById(author.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> libraryService.getAuthorBooks(author.getId()));
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
