package com.library.library.service;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.BookStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void createAuthorTest() {
        //given
        when(authorRepository.save(author)).thenReturn(author);

        //when
        AuthorDto actual = libraryService.createAuthor(authorDto);

        //then
        assertEquals(authorDto, actual);
    }

    @Test
    void getAllAuthorsTest() {
        Pageable pageable = PageRequest.of(0, 12);

        List<Author> authors = new ArrayList<>();
        authors.add(author);
        Page<Author> authorPage = new PageImpl<>(authors, pageable, authors.size());
        //given
        when(authorRepository.findAll(pageable)).thenReturn(authorPage);

        //when
        Page<AuthorDto> actualPage = libraryService.getAllAuthors(pageable);

        //then
        List<AuthorDto> authorDtos = new ArrayList<>();
        authorDtos.add(authorDto);
        Page<AuthorDto> expectedPage = new PageImpl<>(authorDtos, pageable, authorDtos.size());
        assertEquals(expectedPage, actualPage);
    }

    @Test
    void getAuthorBooksTest() {
        author.setBooks(Collections.singleton(book));
        Set<BookDto> expectedBooks = BookMapper.INSTANCE.mapBookDtos(author.getBooks());
        //given
        when(authorRepository.findAuthorByNickname(author.getNickname())).thenReturn(author);

        //when
        Set<BookDto> actualBooks = libraryService.getAuthorBooks(author.getNickname());

        //then
        assertEquals(expectedBooks, actualBooks);
    }

    @Test
    void isNicknameAlreadyInUseTest() {
        //given
        when(authorRepository.existsAuthorByNickname(author.getNickname())).thenReturn(true);

        //when
        boolean actual = libraryService.isNicknameAlreadyInUse(author.getNickname());

        //then
        assertTrue(actual);
    }

    @Test
    void isNicknameAlreadyInUseFalseTest() {
        //given
        when(authorRepository.existsAuthorByNickname(author.getNickname())).thenReturn(false);

        //when
        boolean actual = libraryService.isNicknameAlreadyInUse(author.getNickname());

        //then
        assertFalse(actual);
    }

    private Book getBook() {
        return BookMapper.INSTANCE.mapBook(getBookDto());
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
