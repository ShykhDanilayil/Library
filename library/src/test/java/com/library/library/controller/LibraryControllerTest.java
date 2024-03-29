package com.library.library.controller;

import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.BookService;
import com.library.library.service.LibraryService;
import com.library.library.service.UserService;
import com.library.library.service.exception.BookNotAvailableException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.LibraryException;
import com.library.library.service.exception.ReservedException;
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

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
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

    public LibraryControllerTest() throws ParseException {
        this.libraryDto = getLibraryDto();
        this.userDto = getUserDto();
        this.bookDto = getBookDto();
    }

    @Test
    @WithAnonymousUser
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
    @WithAnonymousUser
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
    @WithAnonymousUser
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
    @WithMockUser(username = "user@email.com", roles = "USER")
    void addUserTestRoleUser() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).addUser(libraryDto.getName(), "user@email.com");

        mockMvc.perform(post("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, times(1)).addUser(libraryDto.getName(), "user@email.com");
    }

    @Test
    @WithMockUser(username = "user@email.com", roles = "USER")
    void addUserTestRoleUserException() throws Exception {
        String message = format("This user already exists in this library");
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doThrow(new LibraryException(message)).when(libraryService).addUser(libraryDto.getName(), "user@email.com");

        mockMvc.perform(post("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, times(1)).addUser(libraryDto.getName(), "user@email.com");
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void addUserTestRoleLibrarian() throws Exception {
        mockMvc.perform(post("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).addUser(anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUserTestRoleAdmin() throws Exception {
        mockMvc.perform(post("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).addUser(anyString(), anyString());
    }

    @Test
    @WithAnonymousUser
    void addUserTestRoleNotAuthorized() throws Exception {
        mockMvc.perform(post("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).deleteUser(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "user@email.com", roles = "USER")
    void deleteUserTestRoleUser() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).deleteUser(libraryDto.getName(), "user@email.com");

        mockMvc.perform(delete("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, times(1)).deleteUser(libraryDto.getName(), "user@email.com");
    }

    @Test
    @WithMockUser(username = "user@email.com", roles = "USER")
    void deleteUserTestRoleUserException() throws Exception {
        String message = format("This user already exists in this library");
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doThrow(new LibraryException(message)).when(libraryService).deleteUser(libraryDto.getName(), "user@email.com");

        mockMvc.perform(delete("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, times(1)).deleteUser(libraryDto.getName(), "user@email.com");
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void deleteUserTestRoleLibrarian() throws Exception {
        mockMvc.perform(delete("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).deleteUser(anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserTestRoleAdmin() throws Exception {
        mockMvc.perform(delete("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).deleteUser(anyString(), anyString());
    }

    @Test
    @WithAnonymousUser
    void deleteUserTestRoleNotAuthorized() throws Exception {
        mockMvc.perform(delete("/libraries/users")
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).deleteUser(anyString(), anyString());
    }

    @Test
    @WithAnonymousUser
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
    @WithAnonymousUser
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
    @WithAnonymousUser
    void getAllLibrariesByBookTitleNotAvailableExceptionTest() throws Exception {
        String message = format("The book with this title %s isn't available", bookDto.getTitle());
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.getAllLibrariesByBookTitle(bookDto.getTitle())).thenThrow(new BookNotAvailableException(message));

        mockMvc.perform(get("/libraries/books/")
                .param("bookTitle", bookDto.getTitle()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    @WithAnonymousUser
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
    @WithAnonymousUser
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
    @WithAnonymousUser
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
    @WithMockUser(username = "test@email.com")
    void reserveBookTest() throws Exception {
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    @WithAnonymousUser
    void reserveBookTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/libraries/reserve")
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
    @WithMockUser(roles = "ADMIN")
    void reserveBookTestRoleAdmin() throws Exception {
        mockMvc.perform(post("/libraries/reserve")
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
    @WithMockUser(roles = "LIBRARIAM")
    void reserveBookTestRoleLibrarian() throws Exception {
        mockMvc.perform(post("/libraries/reserve")
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
    @WithMockUser
    void reserveBookLibNameExceptionTest() throws Exception {
        String message = "reserveBook.libraryName: This library name doesn't exists!";
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

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).reserveBook(any(), any(), any());
    }

    @Test
    @WithMockUser
    void reserveBookTitleExceptionTest() throws Exception {
        String message = "reserveBook.bookTitle: This book title doesn't exists!";
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

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).reserveBook(any(), any(), any());
    }


    @Test
    @WithMockUser(username = "test@email.com")
    void reserveBookNotAvailableExceptionTest() throws Exception {
        String message = format("Available book with this title %s doesn't exist in library with this name %s", bookDto.getTitle(), libraryDto.getName());
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);

        doThrow(new BookNotAvailableException(message)).when(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/reserve")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    @WithMockUser(username = "test@email.com")
    void borrowBookTest() throws Exception {
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        mockMvc.perform(post("/libraries/borrow")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    @WithAnonymousUser
    void borrowBookTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/libraries/borrow")
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
    @WithMockUser(roles = "ADMIN")
    void borrowBookTestRoleAdmin() throws Exception {
        mockMvc.perform(post("/libraries/borrow")
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
    @WithMockUser
    void borrowBookTitleExceptionTest() throws Exception {
        String message = "borrowBook.bookTitle: This book title doesn't exists!";
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

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).borrowBook(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "test@email.com")
    void borrowBookReservedExceptionTest() throws Exception {
        String message = format("Reserved with book %s is not found", bookDto.getTitle());
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

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());
    }

    @Test
    @WithMockUser(username = "test@email.com")
    void returnBookTest() throws Exception {
        when(bookService.isExistBookTitle(bookDto.getTitle())).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        doNothing().when(libraryService).returnBook(eq(bookDto.getTitle()), eq(userDto.getEmail()), eq(libraryDto.getName()), isA(HttpServletRequest.class));

        mockMvc.perform(post("/libraries/return")
                .param("userEmail", userDto.getEmail())
                .param("bookTitle", bookDto.getTitle())
                .param("libraryName", libraryDto.getName()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService).returnBook(eq(bookDto.getTitle()), eq(userDto.getEmail()), eq(libraryDto.getName()), isA(HttpServletRequest.class));
    }

    @Test
    @WithAnonymousUser
    void returnBookTestNotAuthorized() throws Exception {
        mockMvc.perform(post("/libraries/return")
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
    @WithMockUser(roles = "ADMIN")
    void returnBookTestRoleAdmin() throws Exception {
        mockMvc.perform(post("/libraries/return")
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
    @WithMockUser(roles = "LIBRARIAN")
    void returnBookTestRoleLibrarian() throws Exception {
        mockMvc.perform(post("/libraries/return")
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
    @WithMockUser
    void returnBookTestNotLib() throws Exception {
        String message = "returnBook.libraryName: This library name doesn't exists!";
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

        verify(bookService).isExistBookTitle(bookDto.getTitle());
        verify(libraryService).isNameAlreadyInUse(libraryDto.getName());
        verify(libraryService, never()).returnBook(anyString(), anyString(), anyString(), any());
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
                .password("1111")
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
