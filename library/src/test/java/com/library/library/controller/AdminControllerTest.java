package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.config.TestWebConfig;
import com.library.library.controller.dto.UserDto;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(value = AdminController.class)
@AutoConfigureMockMvc
@Import(TestWebConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private LibraryService libraryService;

    @MockBean
    private MyUserDetailsService myUserDetailsService;

    @MockBean
    private DataSource dataSource;

    private final UserDto userDto;

    public AdminControllerTest() throws ParseException {
        this.userDto = getUserDto();
    }

    @Test
    void updateUserTest() throws Exception {
        String oldEmail = userDto.getEmail();
        String newEmail = "newEmail@test.com";
        userDto.setEmail(newEmail);
        when(userService.isEmailAlreadyInUse(oldEmail)).thenReturn(true);
        when(libraryService.isEmailAlreadyInUse(newEmail)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(newEmail)).thenReturn(false);
        when(userService.updateUser(oldEmail, userDto)).thenReturn(userDto);

        mockMvc.perform(put("/admin/users/" + oldEmail)
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    @WithAnonymousUser
    void updateUserTestNotAuthorized() throws Exception {
        mockMvc.perform(put("/admin/users/" + userDto.getEmail())
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(libraryService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    @WithMockUser
    void updateUserTestRoleUser() throws Exception {
        mockMvc.perform(put("/admin/users/" + userDto.getEmail())
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(libraryService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void updateUserTestRoleLibrarian() throws Exception {
        mockMvc.perform(put("/admin/users/" + userDto.getEmail())
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(libraryService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void partialUpdateUserTest() throws Exception {
        String oldEmail = userDto.getEmail();
        String newEmail = "newEmail@test.com";
        UserDto newUserDto = UserDto.builder().email(newEmail).isAccountNonLocked(false).build();
        userDto.setEmail(newUserDto.getEmail());
        userDto.setAccountNonLocked(newUserDto.isAccountNonLocked());

        when(userService.isEmailAlreadyInUse(oldEmail)).thenReturn(true);
        when(libraryService.isEmailAlreadyInUse(newEmail)).thenReturn(false);
        when(userService.isEmailAlreadyInUse(newEmail)).thenReturn(false);
        when(userService.updateUser(oldEmail, newUserDto)).thenReturn(userDto);

        mockMvc.perform(patch("/admin/users/" + oldEmail)
                .content(objectMapper.writeValueAsString(newUserDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.accountNonLocked").value(userDto.isAccountNonLocked()));
    }

    @Test
    @WithAnonymousUser
    void partialUpdateUserTestNotAuthorized() throws Exception {
        mockMvc.perform(patch("/admin/users/" + userDto.getEmail())
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(libraryService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    @WithMockUser
    void partialUpdateUserTestRoleUser() throws Exception {
        mockMvc.perform(patch("/admin/users/" + userDto.getEmail())
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(libraryService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void partialUpdateUserTestRoleLibrarian() throws Exception {
        mockMvc.perform(patch("/admin/users/" + userDto.getEmail())
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(libraryService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).isEmailAlreadyInUse(any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void deleteUserTest() throws Exception {
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(true);
        doNothing().when(userService).deleteUser(userDto.getEmail());

        mockMvc.perform(delete("/admin/users/" + userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService).deleteUser(userDto.getEmail());
    }

    @Test
    @WithAnonymousUser
    void deleteUserTestNotAuthorized() throws Exception {
        mockMvc.perform(delete("/admin/users/" + userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).deleteUser(userDto.getEmail());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void deleteUserTestRoleLibrarian() throws Exception {
        mockMvc.perform(delete("/admin/users/" + userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).deleteUser(userDto.getEmail());
    }

    @Test
    @WithMockUser
    void deleteUserTestRoleUser() throws Exception {
        mockMvc.perform(delete("/admin/users/" + userDto.getEmail()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).deleteUser(userDto.getEmail());
    }

    @Test
    void deleteUserTest2() throws Exception {
        String message = "deleteUser.email: This email doesn't exists!";
        when(userService.isEmailAlreadyInUse(userDto.getEmail())).thenReturn(false);

        mockMvc.perform(delete("/admin/users/" + userDto.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(message));

        verify(userService, only()).isEmailAlreadyInUse(userDto.getEmail());
        verify(userService, never()).deleteUser(userDto.getEmail());
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
