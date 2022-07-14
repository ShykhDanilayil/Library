package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.LibraryDto;
import com.library.library.service.LibraryService;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.LibraryAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LibraryController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    LibraryService libraryService;

    private final LibraryDto libraryDto = getLibraryDto();

    @Test
    void createLibraryTest() throws Exception {
        when(libraryService.createLibrary(libraryDto)).thenReturn(libraryDto);

        mockMvc.perform(post("/library/create")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()));
    }

    @Test
    void createUser_expectException_onError_LibraryAlreadyExistsException() throws Exception {
        String message = "library already exists";
        when(libraryService.createLibrary(libraryDto)).thenThrow(new LibraryAlreadyExistsException(message));

        mockMvc.perform(post("/library/create")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void getAllLibrariesTest() throws Exception {
        when(libraryService.getAllLibraries()).thenReturn(Collections.singletonList(libraryDto));

        mockMvc.perform(get("/library"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$[0].name").value(libraryDto.getName()));
    }

    @Test
    void getAllLibraries_expectException_onError_NullPointerException() throws Exception {
        String message = "error message";
        when(libraryService.getAllLibraries()).thenThrow(new NullPointerException(message));

        mockMvc.perform(get("/library"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(message));
    }

    @Test
    void getLibraryTest() throws Exception {
        when(libraryService.getLibrary(libraryDto.getAddress())).thenReturn(libraryDto);

        mockMvc.perform(get("/library/" + libraryDto.getAddress()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()));
    }

    @Test
    void getLibrary_expectException_onError_EntityNotFoundException() throws Exception {
        String message = "entity not found message";
        when(libraryService.getLibrary(libraryDto.getAddress())).thenThrow(new EntityNotFoundException(message));

        mockMvc.perform(get("/library/" + libraryDto.getAddress()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void deleteLibraryTest() throws Exception {
        doNothing().when(libraryService).deleteLibrary(libraryDto.getAddress());

        mockMvc.perform(delete("/library/" + libraryDto.getAddress()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(libraryService, only()).deleteLibrary(libraryDto.getAddress());
    }

    @Test
    void addBookTest() throws Exception {
        String bookTitle = "TestLibrary";
        when(libraryService.addBook(libraryDto.getName(), bookTitle)).thenReturn(libraryDto);

        mockMvc.perform(post("/library/" + libraryDto.getName() + "/book/" + bookTitle))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()));
    }

    @Test
    void getLibraryByBookTitleTest() throws Exception {
        String bookTitle = "TestLibrary";
        when(libraryService.getLibraryByBookTitle(bookTitle)).thenReturn(Collections.singleton(libraryDto));

        mockMvc.perform(get("/library/book/" + bookTitle))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$[0].name").value(libraryDto.getName()));
    }


    private LibraryDto getLibraryDto() {
        return LibraryDto.builder()
                .address("Lviv")
                .name("TEST")
                .build();
    }
}
