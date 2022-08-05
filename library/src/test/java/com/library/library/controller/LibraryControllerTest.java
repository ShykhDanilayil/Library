package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.BookService;
import com.library.library.service.LibraryService;
import com.library.library.service.UserService;
import com.library.library.service.exception.BookNotAvailableException;
import com.library.library.service.exception.BorrowedException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.ReservedException;
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
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
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

    @MockBean
    UserService userService;

    @MockBean
    BookService bookService;

    private final LibraryDto libraryDto = getLibraryDto();
    private final UserDto userDto = getUserDto();
    private final BookDto bookDto = getBookDto();

    public LibraryControllerTest() throws ParseException {
    }

    @Test
    void createLibraryTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);

        when(libraryService.createLibrary(libraryDto)).thenReturn(libraryDto);

        mockMvc.perform(post("/libraries")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(libraryDto.getName()))
                .andExpect(jsonPath("$.email").value(libraryDto.getEmail()))
                .andExpect(jsonPath("$.address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$.postalCode").value(libraryDto.getPostalCode()));
    }

    @Test
    void createLibraryAlreadyEmailExceptionTest() throws Exception {
        String message = "There is already entity with this email!";

        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(true);

        mockMvc.perform(post("/libraries")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).createLibrary(any());
    }

    @Test
    void createLibraryAlreadyEmailExceptionTest2() throws Exception {
        String message = "There is already entity with this email!";

        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);

        mockMvc.perform(post("/libraries")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).isEmailAlreadyInUse(any());
        verify(libraryService, never()).createLibrary(any());
    }

    @Test
    void createLibraryAlreadyNameExceptionTest() throws Exception {
        String message = "There is already library with this name!";

        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/libraries")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(any());
        verify(libraryService, times(1)).isEmailAlreadyInUse(any());
        verify(libraryService, never()).createLibrary(any());
    }

    @Test
    void getAllLibrariesTest() throws Exception {
        Pageable pageable = PageRequest.of(1, 3);
        List<LibraryDto> libraryDtos = new ArrayList<>();
        libraryDtos.add(libraryDto);
        Page<LibraryDto> libraryDtoPage = new PageImpl<>(libraryDtos, pageable, libraryDtos.size());
        when(libraryService.getPageLibraries(pageable)).thenReturn(libraryDtoPage);

        mockMvc.perform(get("/libraries?page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['pageable']['paged']").value("true"))
                .andExpect(jsonPath("$['content'][0].name").value(libraryDto.getName()))
                .andExpect(jsonPath("$['content'][0].email").value(libraryDto.getEmail()));
    }

    @Test
    void getLibraryTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(libraryService.getLibrary(libraryDto.getName())).thenReturn(libraryDto);

        mockMvc.perform(get("/libraries/" + libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()))
                .andExpect(jsonPath("$.email").value(libraryDto.getEmail()))
                .andExpect(jsonPath("$.address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$.postalCode").value(libraryDto.getPostalCode()));
    }

    @Test
    void getLibraryNameExceptionTest() throws Exception {
        String message = "getLibrary.name: This library name doesn't exists!";
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(get("/libraries/" + libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, only()).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).getLibrary(any());
    }

    @Test
    void deleteLibraryTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).deleteLibrary(libraryDto.getName());

        mockMvc.perform(delete("/libraries/" + libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, times(1)).deleteLibrary(libraryDto.getName());
    }

    @Test
    void deleteLibraryNameFalseTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(delete("/libraries/" + libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(libraryService, only()).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).deleteLibrary(any());
    }

    @Test
    void addBookTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.addBook(libraryDto.getName(), bookDto.getTitle())).thenReturn(libraryDto);

        mockMvc.perform(post("/libraries/books")
                .param("libraryName", libraryDto.getName())
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()))
                .andExpect(jsonPath("$.email").value(libraryDto.getEmail()))
                .andExpect(jsonPath("$.address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$.postalCode").value(libraryDto.getPostalCode()));
    }

    @Test
    void addBookTitleExceptionTest() throws Exception {
        String message = "addBook.bookTitle: This book title doesn't exists!";
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);

        mockMvc.perform(post("/libraries/books")
                .param("libraryName", libraryDto.getName())
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(bookService, times(1)).isExistBookTitle(bookDto.getTitle());
        verify(libraryService, never()).addBook(any(), any());
    }

    @Test
    void addBookLibNameExceptionTest() throws Exception {
        String message = "addBook.libraryName: This library name doesn't exists!";
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);

        mockMvc.perform(post("/libraries/books")
                .param("libraryName", libraryDto.getName())
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(bookService, times(1)).isExistBookTitle(bookDto.getTitle());
        verify(libraryService, never()).addBook(any(), any());
    }

    @Test
    void addUserTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doNothing().when(libraryService).addUser(libraryDto.getName(), userDto.getEmail());

        mockMvc.perform(post("/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).addUser(libraryDto.getName(), userDto.getEmail());
    }

    @Test
    void addUserTest2() throws Exception {
        String message = "addUser.email: This email doesn't exists!";
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).addUser(any(), any());
    }

    @Test
    void getAllLibrariesByBookTitleTest() throws Exception {
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.getAllLibrariesByBookTitle(bookDto.getTitle())).thenReturn(Collections.singleton(libraryDto));

        mockMvc.perform(get("/libraries/books/")
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(libraryDto.getName()))
                .andExpect(jsonPath("$[0].email").value(libraryDto.getEmail()))
                .andExpect(jsonPath("$[0].address").value(libraryDto.getAddress()))
                .andExpect(jsonPath("$[0].postalCode").value(libraryDto.getPostalCode()));
    }

    @Test
    void getAllLibrariesByBookTitleExceptionTest() throws Exception {
        String message = "getAllLibrariesByBookTitle.bookTitle: This book title doesn't exists!";
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);

        mockMvc.perform(get("/libraries/books/")
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(bookService, only()).isExistBookTitle(bookDto.getTitle());
        verify(libraryService, never()).getAllLibrariesByBookTitle(any());
    }

    @Test
    void getAllLibrariesByBookTitleNotAvailableExceptionTest() throws Exception {
        String message = format("The book with this title %s isn't available", bookDto.getTitle());
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.getAllLibrariesByBookTitle(bookDto.getTitle())).thenThrow(new BookNotAvailableException(message));

        mockMvc.perform(get("/libraries/books/")
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void getAllLibrariesByBookTitleEntityNotFoundExceptionTest() throws Exception {
        String message = format("Available book with this title %s doesn't exist in any library", bookDto.getTitle());
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.getAllLibrariesByBookTitle(bookDto.getTitle())).thenThrow(new EntityNotFoundException(message));

        mockMvc.perform(get("/libraries/books/")
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void getAllBooksTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(libraryService.getAllBooks(libraryDto.getName())).thenReturn(Collections.singleton(bookDto));

        mockMvc.perform(get("/libraries/books/" + libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value(bookDto.getTitle()))
                .andExpect(jsonPath("$[0].description").value(bookDto.getDescription()))
                .andExpect(jsonPath("$[0].pages").value(bookDto.getPages()))
                .andExpect(jsonPath("$[0].publicationYear").value(bookDto.getPublicationYear()));
    }

    @Test
    void getAllBooksLibNameExceptionTest() throws Exception {
        String message = "getAllBooks.libraryName: This library name doesn't exists!";
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);
        when(libraryService.getAllBooks(libraryDto.getName())).thenReturn(Collections.singleton(bookDto));

        mockMvc.perform(get("/libraries/books/" + libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, only()).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).getAllBooks(libraryDto.getName());
    }


    @Test
    void reserveBookTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void reserveBookLibNameExceptionTest() throws Exception {
        String message = "reserveBook.libraryName: This library name doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).reserveBook(any(), any(), any());
    }

    @Test
    void reserveBookTitleExceptionTest() throws Exception {
        String message = "reserveBook.bookTitle: This book title doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).reserveBook(any(), any(), any());
    }

    @Test
    void reserveBookUserEmailExceptionTest() throws Exception {
        String message = "reserveBook.userEmail: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).reserveBook(any(), any(), any());
    }

    @Test
    void reserveBookReservedExceptionTest() throws Exception {
        String message = format("User with email %s didn't return the last book", getUserDto().getEmail());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        doThrow(new ReservedException(message)).when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void reserveBookNotAvailableExceptionTest() throws Exception {
        String message = format("Available book with this title %s doesn't exist in library with this name %s", bookDto.getTitle(), libraryDto.getName());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        doThrow(new BookNotAvailableException(message)).when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void borrowBookTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void borrowBookLibNameExceptionTest() throws Exception {
        String message = "borrowBook.libraryName: This library name doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(post("/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void borrowBookTitleExceptionTest() throws Exception {
        String message = "borrowBook.bookTitle: This book title doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void borrowBookUserEmailExceptionTest() throws Exception {
        String message = "borrowBook.userEmail: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void borrowBookReservedExceptionTest() throws Exception {
        String message = format("Reserved with book %s is not found", bookDto.getTitle());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        doThrow(new ReservedException(message)).when(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void returnBookTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    void returnBookLibNameExceptionTest() throws Exception {
        String message = "returnBook.libraryName: This library name doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(post("/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString());
    }

    @Test
    void returnBookTitleExceptionTest() throws Exception {
        String message = "returnBook.bookTitle: This book title doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString());
    }

    @Test
    void returnBookUserEmailExceptionTest() throws Exception {
        String message = "returnBook.userEmail: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString());
    }

    @Test
    void returnBookBorrowedExceptionTest() throws Exception {
        String message = format("Borrowed with book %s is not found", bookDto.getTitle());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doThrow(new BorrowedException(message)).when(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    private LibraryDto getLibraryDto() {
        return LibraryDto.builder()
                .name("TEST")
                .email("test@email.com")
                .phone("0943453456")
                .country("English")
                .city("London")
                .address("St. She 2")
                .postalCode("12345")
                .build();
    }

    private UserDto getUserDto() throws ParseException {
        return UserDto.builder()
                .firstName("test name")
                .lastName("last name")
                .email("test@email.com")
                .phone("0986555423")
                .birthday(new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).parse("7-Jun-1987"))
                .country("English")
                .city("London")
                .address("St. She 2")
                .postalCode("12345")
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
