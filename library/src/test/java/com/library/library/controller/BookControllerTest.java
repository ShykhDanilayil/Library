package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.AuthorService;
import com.library.library.service.BookService;
import com.library.library.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "librarian", roles = "LIBRARIAN", password = "librarian")
@WebMvcTest(value = BookController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    private DataSource dataSource;

    private final AuthorDto authorDto = getAuthorDto();
    private final BookDto bookDto = getBookDto();

    @Test
    void getBookTest() throws Exception {
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(bookService.getBook(bookDto.getTitle())).thenReturn(bookDto);

        mockMvc.perform(get("/books/" + bookDto.getTitle())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(bookDto.getTitle()))
                .andExpect(jsonPath("$.description").value(bookDto.getDescription()))
                .andExpect(jsonPath("$.pages").value(bookDto.getPages()))
                .andExpect(jsonPath("$.publicationYear").value(bookDto.getPublicationYear()));
    }

    @Test
    @WithMockUser
    void getBookTestRoleUser() throws Exception {
        mockMvc.perform(get("/books/" + bookDto.getTitle())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(bookService, never()).isExistBookTitle(bookDto.getTitle());
        verify(bookService, never()).getBook(bookDto.getTitle());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBookTestRoleAdmin() throws Exception {
        mockMvc.perform(get("/books/" + bookDto.getTitle())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(bookService, never()).isExistBookTitle(bookDto.getTitle());
        verify(bookService, never()).getBook(bookDto.getTitle());
    }

    @Test
    @WithAnonymousUser
    void getBookTestNotAuthorized() throws Exception {
        mockMvc.perform(get("/books/" + bookDto.getTitle())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).isExistBookTitle(bookDto.getTitle());
        verify(bookService, never()).getBook(bookDto.getTitle());
    }

    @Test
    void getBookTitleExceptionTest() throws Exception {
        String message = "getBook.bookTitle: This book title doesn't exists!";
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);

        mockMvc.perform(get("/books/" + bookDto.getTitle())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(bookService, only()).isExistBookTitle(bookDto.getTitle());
        verify(bookService, never()).getBook(bookDto.getTitle());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBooksTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        List<BookDto> bookDtos = Collections.singletonList(bookDto);
        Page<BookDto> bookDtosPage = new PageImpl<>(bookDtos, pageable, bookDtos.size());

        when(bookService.getAllBooks(pageable)).thenReturn(bookDtosPage);

        mockMvc.perform(get("/books?page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['pageable']['paged']").value("true"))
                .andExpect(jsonPath("$['content'][0].title").value(bookDto.getTitle()));
    }

    @Test
    void createBookTest() throws Exception {
        when(authorService.isNicknameAlreadyInUse(authorDto.getNickname())).thenReturn(true);
        when(bookService.createBook(authorDto.getNickname(), bookDto)).thenReturn(bookDto);

        mockMvc.perform(post("/books/" + authorDto.getNickname())
                .content(objectMapper.writeValueAsString(bookDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(bookDto.getTitle()))
                .andExpect(jsonPath("$.description").value(bookDto.getDescription()))
                .andExpect(jsonPath("$.pages").value(bookDto.getPages()))
                .andExpect(jsonPath("$.publicationYear").value(bookDto.getPublicationYear()));
    }

    @Test
    void createBookNickNameExceptionTest() throws Exception {
        String message = "createBook.nickname: This nickname doesn't exists!";
        when(authorService.isNicknameAlreadyInUse(authorDto.getNickname())).thenReturn(false);

        mockMvc.perform(post("/books/" + authorDto.getNickname())
                .content(objectMapper.writeValueAsString(bookDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(authorService, only()).isNicknameAlreadyInUse(authorDto.getNickname());
        verify(bookService, never()).createBook(any(), any());
    }

    @Test
    void createBookDescriptionExceptionTest() throws Exception {
        bookDto.setDescription("test");
        String messageDescription = "Invalid book description. Description must be 4 words";

        mockMvc.perform(post("/books/" + authorDto.getNickname())
                .content(objectMapper.writeValueAsString(bookDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(messageDescription));

        verify(authorService, never()).isNicknameAlreadyInUse(authorDto.getNickname());
        verify(bookService, never()).createBook(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuthorByBookTest() throws Exception {
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(bookService.getAuthorByBook(bookDto.getTitle())).thenReturn(authorDto);

        mockMvc.perform(get("/books/" + bookDto.getTitle() + "/author")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(authorDto.getName()))
                .andExpect(jsonPath("$.nickname").value(authorDto.getNickname()));
    }

    @Test
    void getAuthorByBookTitleExceptionTest() throws Exception {
        String message = "getAuthorByBook.bookTitle: This book title doesn't exists!";
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);

        mockMvc.perform(get("/books/" + bookDto.getTitle() + "/author")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(bookService, only()).isExistBookTitle(bookDto.getTitle());
        verify(bookService, never()).getAuthorByBook(bookDto.getTitle());
    }

    private AuthorDto getAuthorDto() {
        return AuthorDto.builder()
                .name("Test")
                .nickname("NICKNANE")
                .build();
    }

    private BookDto getBookDto() {
        return BookDto.builder()
                .title("test")
                .description("new t e s t description")
                .pages(86)
                .publicationYear(2020)
                .build();
    }
}
