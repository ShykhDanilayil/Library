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
import com.library.library.service.exception.ReservedException;
import com.library.library.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "librarian", roles = "LIBRARIAN", password = "librarian")
@WebMvcTest(value = LibrarianController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class LibrarianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibraryService libraryService;

    @MockBean
    private UserService userService;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    private DataSource dataSource;

    private final LibraryDto libraryDto;
    private final UserDto userDto;
    private final BookDto bookDto;

    public LibrarianControllerTest() throws ParseException {
        this.libraryDto = getLibraryDto();
        this.userDto = getUserDto();
        this.bookDto = getBookDto();
    }

    @Test
    void createLibraryTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);

        when(libraryService.createLibrary(libraryDto)).thenReturn(libraryDto);

        mockMvc.perform(post("/librarian/libraries")
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
    @WithMockUser
    void createLibraryTestRoleUser() throws Exception {
        mockMvc.perform(post("/librarian/libraries")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, never()).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).createLibrary(libraryDto);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createLibraryTestRoleAdmin() throws Exception {
        mockMvc.perform(post("/librarian/libraries")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, never()).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).createLibrary(libraryDto);
    }

    @Test
    @WithAnonymousUser
    void createLibraryTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/librarian/libraries")
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(libraryService, never()).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, never()).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).createLibrary(libraryDto);
    }

    @Test
    void createLibraryAlreadyEmailExceptionTest() throws Exception {
        String message = "There is already entity with this email!";

        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(true);

        mockMvc.perform(post("/librarian/libraries")
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

        mockMvc.perform(post("/librarian/libraries")
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

        mockMvc.perform(post("/librarian/libraries")
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
    void deleteLibraryTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).deleteLibrary(libraryDto.getName());

        mockMvc.perform(delete("/librarian/libraries/" + libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, times(1)).deleteLibrary(libraryDto.getName());
    }

    @Test
    void deleteLibraryNameFalseTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(delete("/librarian/libraries/" + libraryDto.getName()))
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

        mockMvc.perform(post("/librarian/libraries/books")
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

        mockMvc.perform(post("/librarian/libraries/books")
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

        mockMvc.perform(post("/librarian/libraries/books")
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
    void reserveBookControlTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/librarian/libraries/reserve")
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
    @WithAnonymousUser
    void reserveBookControlTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/librarian/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(bookService, never()).isExistBookTitle(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).reserveBook(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void reserveBookControlTestRoleUser() throws Exception {
        mockMvc.perform(post("/librarian/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(bookService, never()).isExistBookTitle(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).reserveBook(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reserveBookControlLibNameExceptionTest() throws Exception {
        String message = "reserveBookControl.libraryName: This library name doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(post("/librarian/libraries/reserve")
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
    void reserveBookControlTitleExceptionTest() throws Exception {
        String message = "reserveBookControl.bookTitle: This book title doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/librarian/libraries/reserve")
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
    void reserveBookControlUserEmailExceptionTest() throws Exception {
        String message = "reserveBookControl.userEmail: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/librarian/libraries/reserve")
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
    void reserveBookControlReservedExceptionTest() throws Exception {
        String message = format("User with email %s didn't return the last book", getUserDto().getEmail());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        doThrow(new ReservedException(message)).when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/librarian/libraries/reserve")
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
    @WithMockUser(roles = "ADMIN")
    void reserveBookControlNotAvailableExceptionTest() throws Exception {
        String message = format("Available book with this title %s doesn't exist in library with this name %s", bookDto.getTitle(), libraryDto.getName());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        doThrow(new BookNotAvailableException(message)).when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/librarian/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void borrowBookControlTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/librarian/libraries/borrow")
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
    @WithAnonymousUser
    void borrowBookControlTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/librarian/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(bookService, never()).isExistBookTitle(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).borrowBook(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void borrowBookControlTestRoleUser() throws Exception {
        mockMvc.perform(post("/librarian/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(bookService, never()).isExistBookTitle(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).borrowBook(anyString(), anyString(), anyString());
    }

    @Test
    void borrowBookControlLibNameExceptionTest() throws Exception {
        String message = "borrowBookControl.libraryName: This library name doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(post("/librarian/libraries/borrow")
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
    @WithMockUser(roles = "ADMIN")
    void borrowBookControlTitleExceptionTest() throws Exception {
        String message = "borrowBookControl.bookTitle: This book title doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/librarian/libraries/borrow")
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
    void borrowBookControlUserEmailExceptionTest() throws Exception {
        String message = "borrowBookControl.userEmail: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/librarian/libraries/borrow")
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
    @WithMockUser(roles = "ADMIN")
    void borrowBookControlReservedExceptionTest() throws Exception {
        String message = format("Reserved with book %s is not found", bookDto.getTitle());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        doThrow(new ReservedException(message)).when(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/librarian/libraries/borrow")
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
    void returnBookControlTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null);

        mockMvc.perform(post("/librarian/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null);
    }

    @Test
    @WithAnonymousUser
    void returnBookControlTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/librarian/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(bookService, never()).isExistBookTitle(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void returnBookControlTestRoleAdmin() throws Exception {
        mockMvc.perform(post("/librarian/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(bookService, never()).isExistBookTitle(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnBookControlLibNameExceptionTest() throws Exception {
        String message = "returnBookControl.libraryName: This library name doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(false);

        mockMvc.perform(post("/librarian/libraries/return")
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
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString(), any());
    }

    @Test
    void returnBookControlTitleExceptionTest() throws Exception {
        String message = "returnBookControl.bookTitle: This book title doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(false);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/librarian/libraries/return")
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
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString(), any());
    }

    @Test
    void returnBookControlUserEmailExceptionTest() throws Exception {
        String message = "returnBookControl.userEmail: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        mockMvc.perform(post("/librarian/libraries/return")
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
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnBookControlBorrowedExceptionTest() throws Exception {
        String message = format("Borrowed with book %s is not found", bookDto.getTitle());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doThrow(new BorrowedException(message)).when(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null);

        mockMvc.perform(post("/librarian/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null);
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
