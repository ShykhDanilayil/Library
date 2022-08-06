package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.BookService;
import com.library.library.service.LibraryService;
import com.library.library.service.UserService;
import com.library.library.service.impl.MyUserDetailsService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
    private MyUserDetailsService myUserDetailsService;

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
    @WithMockUser(roles = "ADMIN")
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
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

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

        mockMvc.perform(post("/librarian/books")
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

        mockMvc.perform(post("/librarian/books")
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

        mockMvc.perform(post("/librarian/books")
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
