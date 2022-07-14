package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.AuthorService;
import com.library.library.service.exception.AuthorAlreadyExistsException;
import com.library.library.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static java.lang.String.format;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthorController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    AuthorService authorService;

    private final AuthorDto authorDto = getAuthorDto();
    private final BookDto bookDto = getBookDto();

    @Test
    void getAuthorByIdTest() throws Exception {
        when(authorService.getAuthorInfo(authorDto.getId())).thenReturn(authorDto);

        mockMvc.perform(get("/author/" + authorDto.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(authorDto.getId()))
                .andExpect(jsonPath("$.name").value(authorDto.getName()))
                .andExpect(jsonPath("$.nickname").value(authorDto.getNickname()));
    }

    @Test
    void getAuthor_expectException_onError_EntityNotFoundException() throws Exception {
        String message = format("Author with id %s is not found", authorDto.getId());
        when(authorService.getAuthorInfo(authorDto.getId())).thenThrow(new EntityNotFoundException(message));

        mockMvc.perform(get("/author/" + authorDto.getId()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void createAuthorTest() throws Exception {
        AuthorDto authorDtoWithIdNull = authorDto;
        authorDtoWithIdNull.setId(null);

        when(authorService.createAuthor(authorDtoWithIdNull)).thenReturn(authorDto);

        mockMvc.perform(post("/author")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(authorDto.getId()))
                .andExpect(jsonPath("$.name").value(authorDto.getName()))
                .andExpect(jsonPath("$.nickname").value(authorDto.getNickname()));
    }

    @Test
    void createAuthor_expectException_onError_AuthorAlreadyExistsException() throws Exception {
        String message = format("Author with name %s  and nickname %s exists", authorDto.getId(), authorDto.getNickname());

        AuthorDto authorDtoWithIdNull = authorDto;
        authorDtoWithIdNull.setId(null);

        when(authorService.createAuthor(authorDtoWithIdNull)).thenThrow(new AuthorAlreadyExistsException(message));

        mockMvc.perform(post("/author")
                .content(objectMapper.writeValueAsString(authorDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void getAuthorBooksTest() throws Exception {
        when(authorService.getAuthorBooks(authorDto.getId())).thenReturn(Collections.singleton(bookDto));

        mockMvc.perform(get("/author/" + authorDto.getId() + "/book"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value(bookDto.getTitle()))
                .andExpect(jsonPath("$[0].description").value(bookDto.getDescription()))
                .andExpect(jsonPath("$[0].pages").value(bookDto.getPages()));
    }

    private AuthorDto getAuthorDto() {
        return AuthorDto.builder()
                .id(456L)
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
