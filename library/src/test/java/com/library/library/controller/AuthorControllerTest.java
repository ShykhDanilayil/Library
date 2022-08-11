package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.AuthorService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "librarian", roles = "LIBRARIAN", password = "librarian")
@WebMvcTest(AuthorController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    private DataSource dataSource;

    private final AuthorDto authorDto = getAuthorDto();
    private final BookDto bookDto = getBookDto();

    @Test
    void createAuthorTest() throws Exception {
        when(authorService.isNicknameAlreadyInUse(authorDto.getNickname())).thenReturn(false);
        when(authorService.createAuthor(authorDto)).thenReturn(authorDto);

        mockMvc.perform(post("/authors")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(authorDto.getName()))
                .andExpect(jsonPath("$.nickname").value(authorDto.getNickname()));
    }

    @Test
    @WithMockUser
    void createAuthorTestRoleUser() throws Exception {
        mockMvc.perform(post("/authors")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(authorService, never()).isNicknameAlreadyInUse(any());
        verify(authorService, never()).createAuthor(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createAuthorTestRoleAdmin() throws Exception {
        mockMvc.perform(post("/authors")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(authorService, never()).isNicknameAlreadyInUse(any());
        verify(authorService, never()).createAuthor(any());
    }

    @Test
    @WithAnonymousUser
    void createAuthorTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/authors")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(authorService, never()).isNicknameAlreadyInUse(any());
        verify(authorService, never()).createAuthor(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAuthorNicknameException() throws Exception {
        String message = "There is already author with this nickname!";
        when(authorService.isNicknameAlreadyInUse(authorDto.getNickname())).thenReturn(true);

        mockMvc.perform(post("/authors")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(authorService, only()).isNicknameAlreadyInUse(authorDto.getNickname());
        verify(authorService, never()).createAuthor(any());
    }

    @Test
    void createAuthorTestWithNullName() throws Exception {
        String message = "must not be blank";
        authorDto.setName(null);
        when(authorService.createAuthor(authorDto)).thenReturn(authorDto);

        mockMvc.perform(post("/authors")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value(message));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAuthorsTest() throws Exception {
        Pageable pageable = PageRequest.of(1, 8);
        List<AuthorDto> authorDtos = new ArrayList<>();
        authorDtos.add(authorDto);
        Page<AuthorDto> authorDtoPage = new PageImpl<>(authorDtos, pageable, authorDtos.size());
        when(authorService.getAllAuthors(pageable)).thenReturn(authorDtoPage);

        mockMvc.perform(get("/authors?page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['pageable']['paged']").value("true"))
                .andExpect(jsonPath("$['content'][0].nickname").value(authorDto.getNickname()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBooksAuthorTest() throws Exception {
        when(authorService.isNicknameAlreadyInUse(authorDto.getNickname())).thenReturn(true);
        when(authorService.getAuthorBooks(authorDto.getNickname())).thenReturn(Collections.singleton(bookDto));

        mockMvc.perform(get("/authors/" + authorDto.getNickname() + "/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value(bookDto.getTitle()))
                .andExpect(jsonPath("$[0].description").value(bookDto.getDescription()))
                .andExpect(jsonPath("$[0].pages").value(bookDto.getPages()))
                .andExpect(jsonPath("$[0].status").value(bookDto.getStatus()));
    }

    @Test
    void getBooksAuthorNickNameExceptionTest() throws Exception {
        String message = "getBooksAuthor.nickname: This nickname doesn't exists!";
        when(authorService.isNicknameAlreadyInUse(authorDto.getNickname())).thenReturn(false);

        mockMvc.perform(get("/authors/" + authorDto.getNickname() + "/books"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(authorService, only()).isNicknameAlreadyInUse(authorDto.getNickname());
        verify(authorService, never()).getAuthorBooks(any());
    }

    private AuthorDto getAuthorDto() {
        return AuthorDto.builder()
                .name("Test")
                .nickname("TEST NICKNANE")
                .build();
    }

    private BookDto getBookDto() {
        return BookDto.builder()
                .title("test book")
                .description("test description")
                .pages(110)
                .build();
    }
}
