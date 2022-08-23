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
import com.library.library.service.exception.LibraryException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "test@email.com", roles = "LIBRARIAN", password = "librarian")
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
    void updateLibTest() throws Exception {
        String oldName = libraryDto.getName();
        String newName = "new Test Name";
        libraryDto.setName(newName);

        when(libraryService.isNameAlreadyInUse(oldName)).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(newName)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);

        when(libraryService.updateLibrary(oldName, libraryDto)).thenReturn(libraryDto);

        mockMvc.perform(put("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()))
                .andExpect(jsonPath("$.postalCode").value(libraryDto.getPostalCode()))
                .andExpect(jsonPath("$.email").value(libraryDto.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateLibTestRoleAdmin() throws Exception {
        String oldName = libraryDto.getName();
        String newName = "new Test Name";
        libraryDto.setName(newName);

        when(libraryService.isNameAlreadyInUse(oldName)).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(newName)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);

        when(libraryService.updateLibrary(oldName, libraryDto)).thenReturn(libraryDto);

        mockMvc.perform(put("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()))
                .andExpect(jsonPath("$.postalCode").value(libraryDto.getPostalCode()))
                .andExpect(jsonPath("$.email").value(libraryDto.getEmail()));
    }

    @Test
    void updateLibTestEmailException() throws Exception {
        String message = "There is already entity with this email!";

        String oldName = libraryDto.getName();
        String newName = "new Test Name";
        libraryDto.setName(newName);

        when(libraryService.isNameAlreadyInUse(newName)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(true);

        mockMvc.perform(put("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value(message));

        verify(libraryService, never()).isNameAlreadyInUse(oldName);
        verify(libraryService).isNameAlreadyInUse(newName);
        verify(userService).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).updateLibrary(anyString(), any());
    }

    @Test
    void updateLibTestNameException() throws Exception {
        String message = "updateLibrary.name: This library name doesn't exists!";

        String oldName = libraryDto.getName();
        String newName = "new Test Name";
        libraryDto.setName(newName);


        when(libraryService.isNameAlreadyInUse(oldName)).thenReturn(false);
        when(libraryService.isNameAlreadyInUse(newName)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);

        mockMvc.perform(put("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService).isNameAlreadyInUse(oldName);
        verify(libraryService, times(2)).isNameAlreadyInUse(newName);
        verify(userService, times(2)).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, times(2)).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).updateLibrary(anyString(), any());
    }

    @Test
    @WithAnonymousUser
    void updateLibTestNotAuthorized() throws Exception {
        mockMvc.perform(put("/librarian/libraries/" + libraryDto.getName())
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).updateLibrary(anyString(), any());
    }

    @Test
    @WithMockUser
    void updateLibTestRoleUser() throws Exception {
        mockMvc.perform(put("/librarian/libraries/" + libraryDto.getName())
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).updateLibrary(anyString(), any());
    }

    @Test
    void partialUpdateLibTest() throws Exception {
        String oldName = libraryDto.getName();
        String newName = "new name test";
        LibraryDto newLibDto = LibraryDto.builder().name(newName).email("newEmail@test.com").build();
        libraryDto.setName(newLibDto.getName());
        libraryDto.setEmail(newLibDto.getEmail());

        when(libraryService.isNameAlreadyInUse(oldName)).thenReturn(true);
        when(libraryService.isNameAlreadyInUse(newName)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.updateLibrary(oldName, newLibDto)).thenReturn(libraryDto);

        mockMvc.perform(patch("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(newLibDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(libraryDto.getName()))
                .andExpect(jsonPath("$.email").value(libraryDto.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void partialUpdateLibTestEmailExc() throws Exception {
        String message = "There is already entity with this email!";
        String oldName = libraryDto.getName();
        String newName = "new name test";
        LibraryDto newLibDto = LibraryDto.builder().name(newName).email("newEmail@test.com").build();
        libraryDto.setName(newLibDto.getName());
        libraryDto.setEmail(newLibDto.getEmail());

        when(libraryService.isNameAlreadyInUse(newName)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(true);

        mockMvc.perform(patch("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(newLibDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value(message));

        verify(libraryService, never()).isNameAlreadyInUse(oldName);
        verify(libraryService).isNameAlreadyInUse(newName);
        verify(userService).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).updateLibrary(anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void partialUpdateLibTestNameExc() throws Exception {
        String message = "There is already library with this name!";
        String oldName = libraryDto.getName();
        String newName = "new name test";
        LibraryDto newLibDto = LibraryDto.builder().name(newName).email("newEmail@test.com").build();
        libraryDto.setName(newLibDto.getName());
        libraryDto.setEmail(newLibDto.getEmail());

        when(libraryService.isNameAlreadyInUse(newName)).thenReturn(true);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);

        mockMvc.perform(patch("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(newLibDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value(message));

        verify(libraryService, never()).isNameAlreadyInUse(oldName);
        verify(libraryService).isNameAlreadyInUse(newName);
        verify(userService).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService, never()).updateLibrary(anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void partialUpdateLibNameNullTest() throws Exception {
        String oldName = libraryDto.getName();
        libraryDto.setName(null);

        when(libraryService.isNameAlreadyInUse(oldName)).thenReturn(true);
        when(userService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(libraryDto.getEmail())).thenReturn(false);
        when(libraryService.updateLibrary(oldName, libraryDto)).thenReturn(libraryDto);

        mockMvc.perform(patch("/librarian/libraries/" + oldName)
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(libraryService).isNameAlreadyInUse(oldName);
        verify(libraryService, never()).isNameAlreadyInUse(libraryDto.getName());
        verify(userService).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(libraryDto.getEmail());
        verify(libraryService).updateLibrary(oldName, libraryDto);

        libraryDto.setName(oldName);
    }

    @Test
    @WithAnonymousUser
    void partialUpdateLibTestNotAuthorized() throws Exception {
        mockMvc.perform(patch("/librarian/libraries/" + libraryDto.getName())
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).updateLibrary(anyString(), any());
    }

    @Test
    @WithMockUser
    void partialUpdateLibTestRoleUser() throws Exception {
        mockMvc.perform(patch("/librarian/libraries/" + libraryDto.getName())
                .content(objectMapper.writeValueAsString(libraryDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).updateLibrary(anyString(), any());
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
    void addUserTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doNothing().when(libraryService).addUser(libraryDto.getName(), userDto.getEmail());

        mockMvc.perform(post("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).addUser(libraryDto.getName(), userDto.getEmail());
    }

    @Test
    void addUserTestRoleUserException() throws Exception {
        String message = format("This user already exists in this library");
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doThrow(new LibraryException(message)).when(libraryService).addUser(libraryDto.getName(), userDto.getEmail());

        mockMvc.perform(post("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).addUser(libraryDto.getName(), userDto.getEmail());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void addUserTestRoleLibrarian() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doNothing().when(libraryService).addUser(libraryDto.getName(), userDto.getEmail());

        mockMvc.perform(post("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).addUser(libraryDto.getName(), userDto.getEmail());
    }

    @Test
    @WithMockUser
    void addUserTestRoleUser() throws Exception {
        mockMvc.perform(post("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).addUser(anyString(), anyString());
    }

    @Test
    @WithAnonymousUser
    void addUserTestRoleNotAuthorized() throws Exception {
        mockMvc.perform(post("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).addUser(anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUserTestEmailException() throws Exception {
        String message = "addUser.email: This email doesn't exists!";
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).addUser(anyString(), anyString());
    }

    @Test
    void deleteUserTest() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doNothing().when(libraryService).deleteUser(libraryDto.getName(), userDto.getEmail());

        mockMvc.perform(delete("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).deleteUser(libraryDto.getName(), userDto.getEmail());
    }

    @Test
    void deleteUserTestRoleUserException() throws Exception {
        String message = format("This user already exists in this library");
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doThrow(new LibraryException(message)).when(libraryService).deleteUser(libraryDto.getName(), userDto.getEmail());

        mockMvc.perform(delete("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).deleteUser(libraryDto.getName(), userDto.getEmail());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void deleteUserTestRoleLibrarian() throws Exception {
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doNothing().when(libraryService).deleteUser(libraryDto.getName(), userDto.getEmail());

        mockMvc.perform(delete("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, times(1)).deleteUser(libraryDto.getName(), userDto.getEmail());
    }

    @Test
    @WithMockUser
    void deleteUserTestRoleUser() throws Exception {
        mockMvc.perform(delete("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).deleteUser(anyString(), anyString());
    }

    @Test
    @WithAnonymousUser
    void deleteUserTestRoleNotAuthorized() throws Exception {
        mockMvc.perform(delete("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(libraryService, never()).isNameAlreadyInUse(anyString());
        verify(userService, never()).isEmailAlreadyInUse(anyString());
        verify(libraryService, never()).deleteUser(anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserTestEmailException() throws Exception {
        String message = "deleteUser.email: This email doesn't exists!";
        when(libraryService.isNameAlreadyInUse(libraryDto.getName())).thenReturn(true);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(delete("/librarian/libraries/users")
                .param("libraryName", libraryDto.getName())
                .param("email", userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));

        verify(libraryService, times(1)).isNameAlreadyInUse(libraryDto.getName());
        verify(userService, times(1)).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).deleteUser(anyString(), anyString());
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
