package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.BookService;
import com.library.library.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = BookController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    BookService bookService;

    private final AuthorDto authorDto = getAuthorDto();
    private final BookDto bookDto = getBookDto();

    @Test
    void createBookTest() throws Exception {
        when(bookService.createBook(authorDto.getId(), bookDto)).thenReturn(bookDto);

        mockMvc.perform(post("/book/" + authorDto.getId())
                .content(objectMapper.writeValueAsString(bookDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(bookDto.getTitle()))
                .andExpect(jsonPath("$.description").value(bookDto.getDescription()))
                .andExpect(jsonPath("$.pages").value(bookDto.getPages()));
    }

    @Test
    void createBookTest_expectException_onError_EntityNotFoundException() throws Exception {
        String message = "Author exists";
        when(bookService.createBook(authorDto.getId(), bookDto)).thenThrow(new EntityNotFoundException(message));

        mockMvc.perform(post("/book/" + authorDto.getId())
                .content(objectMapper.writeValueAsString(bookDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void getAuthorByBookTest() throws Exception {
        when(bookService.getAuthorByBook(bookDto.getTitle())).thenReturn(authorDto);

        mockMvc.perform(get("/book/" + bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(authorDto.getId()))
                .andExpect(jsonPath("$.name").value(authorDto.getName()))
                .andExpect(jsonPath("$.nickname").value(authorDto.getNickname()));
    }

    @Test
    void getAuthorByBookTest_expectException() throws Exception {
        String message = "The book with this title doesn't exist";
        when(bookService.getAuthorByBook(bookDto.getTitle())).thenThrow(new EntityNotFoundException((message)));

        mockMvc.perform(get("/book/" + bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));
    }

    private AuthorDto getAuthorDto() {
        return AuthorDto.builder()
                .id(564L)
                .name("Test")
                .nickname("NICKNANE")
                .build();
    }

    private BookDto getBookDto() {
        return BookDto.builder()
                .title("test")
                .description("test_description")
                .pages(86)
                .build();
    }
}
