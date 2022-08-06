package com.library.library.controller;

import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.UserService;
import com.library.library.service.impl.MyUserDetailsService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MyUserDetailsService myUserDetailsService;

    @MockBean
    private DataSource dataSource;

    private final UserDto userDto;

    public UserControllerTest() throws ParseException {
        this.userDto = getUserDto();
    }

    @Test
    @WithMockUser
    void getAllUsersTest() throws Exception {
        Pageable pageable = PageRequest.of(1, 3);
        List<UserDto> userDtos = new ArrayList<>();
        userDtos.add(userDto);
        Page<UserDto> userDtoPage = new PageImpl<>(userDtos, pageable, userDtos.size());

        when(userService.pageUsers(pageable)).thenReturn(userDtoPage);

        mockMvc.perform(get("/users?page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['pageable']['paged']").value("true"))
                .andExpect(jsonPath("$['content'][0].firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$['content'][0].lastName").value(userDto.getLastName()))
                .andExpect(jsonPath("$['content'][0].email").value(userDto.getEmail()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getUserTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        when(userService.getUser(userDto.getEmail())).thenReturn(userDto);

        mockMvc.perform(get("/users/" + userDto.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.postalCode").value(userDto.getPostalCode()));
    }

    @Test
    @WithAnonymousUser
    void getUserTestNotAuthorized() throws Exception {
        mockMvc.perform(get("/users/" + userDto.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).getUser(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserTest2() throws Exception {
        String message = "getUser.email: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(get("/users/" + userDto.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService, only()).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).getUser(any());
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