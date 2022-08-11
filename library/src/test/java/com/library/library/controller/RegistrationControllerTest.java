package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.LibraryService;
import com.library.library.service.UserService;
import com.library.library.service.exception.UserAlreadyExistsException;
import com.library.library.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.lang.String.format;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithAnonymousUser
@WebMvcTest(value = RegistrationController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private LibraryService libraryService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    private DataSource dataSource;

    private final UserDto userDto;

    public RegistrationControllerTest() throws ParseException {
        this.userDto = getUserDto();
    }

    @Test
    void createUserTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(userService.createUser(userDto)).thenReturn(userDto);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    void createUserTest2() throws Exception {
        String message = "There is already entity with this email!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService, never()).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
    }

    @Test
    void createUserInvalidPasswordNullTest() throws Exception {
        String message = "Please enter password";
        userDto.setPassword(null);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setPassword("12345Qq");
    }

    @Test
    void createUserInvalidPasswordTest() throws Exception {
        String message = "Incorrect password format! It should contains at least one digit, one upper case or lower case letter and min length 6 symbols";
        userDto.setPassword("1111");
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setPassword("12345Qq");
    }

    @Test
    void createUserInvalidEmailNullTest() throws Exception {
        String message = "Email address may not be empty";
        userDto.setEmail(null);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setEmail("test@email.com");
    }

    @Test
    void createUserInvalidEmailTest() throws Exception {
        String message = "Incorrect email format!";
        userDto.setEmail("fgs.ds.wwe");
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setEmail("test@email.com");
    }

    @Test
    void createUserInvalidPostalCodeNullTest() throws Exception {
        String message = "Postal code may not be empty";
        userDto.setPostalCode(null);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setPostalCode("12345");
    }

    @Test
    void createUserInvalidPostalCodeTest() throws Exception {
        String message = "Incorrect postal code format!";
        userDto.setPostalCode("12435221");
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setPostalCode("12345");
    }

    @Test
    void createUserInvalidPhoneNumberNullTest() throws Exception {
        String message = "Phone number may not be empty";
        userDto.setPhone(null);
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setPhone("0986555423");
    }

    @Test
    void createUserInvalidPhoneNumberTest() throws Exception {
        String message = "Incorrect phone number format!";
        userDto.setPhone("234324");
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].message").value(message));

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(libraryService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).createUser(userDto);
        userDto.setPhone("0986555423");
    }

    @Test
    void createUserUserAlreadyExistsExceptionTest() throws Exception {
        String message = format("User with email %s exists", userDto.getEmail());
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(libraryService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);
        when(userService.createUser(userDto)).thenThrow(new UserAlreadyExistsException(message));

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));
    }

    private UserDto getUserDto() throws ParseException {
        return UserDto.builder()
                .firstName("test name")
                .lastName("last name")
                .email("test@email.com")
                .password("12345Qq")
                .phone("0986555423")
                .birthday(new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).parse("7-Jun-1987"))
                .country("English")
                .city("London")
                .address("St. She 2")
                .postalCode("12345")
                .build();
    }
}
