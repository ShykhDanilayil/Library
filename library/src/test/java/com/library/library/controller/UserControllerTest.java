//package com.library.library.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.library.library.config.TestWebConfig;
//import com.library.library.controller.dto.UserDto;
//import com.library.library.service.UserService;
//import com.library.library.service.exception.EntityNotFoundException;
//import com.library.library.service.exception.UserAlreadyExistsException;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//
//import static java.lang.String.format;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.only;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(value = UserController.class)
//@AutoConfigureMockMvc
//@Import(TestWebConfig.class)
//public class UserControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private UserService userService;
//
//    private final UserDto userDto = getUserDto();
//
////    @Test
////    void getAllUsersTest() throws Exception {
////        when(userService.pageUsers()).thenReturn(Collections.singletonList(userDto));
////
////        mockMvc.perform(get("/user"))
////                .andDo(print())
////                .andExpect(status().isOk())
////                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
////                .andExpect(jsonPath("$[0].firstName").value(userDto.getFirstName()))
////                .andExpect(jsonPath("$[0].lastName").value(userDto.getLastName()))
////                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()));
////    }
//
////    @Test
////    void getAllUsers_expectException_onError_NullPointerException() throws Exception {
////        String message = "error message";
////        when(userService.pageUsers()).thenThrow(new NullPointerException(message));
////
////        mockMvc.perform(get("/user"))
////                .andDo(print())
////                .andExpect(status().isInternalServerError())
////                .andExpect(content().string(message));
////    }
//
////    @Test
////    void getAllUsers_expectException_onError_EntityNotFoundException() throws Exception {
////        String message = "entity not found message";
////        when(userService.pageUsers()).thenThrow(new EntityNotFoundException(message));
////
////        mockMvc.perform(get("/user"))
////                .andDo(print())
////                .andExpect(status().isNotFound())
////                .andExpect(jsonPath("$.message").value(message));
////    }
//
//    @Test
//    void getUserTest() throws Exception {
//        when(userService.getUser(userDto.getEmail())).thenReturn(userDto);
//
//        mockMvc.perform(get("/user/" + userDto.getEmail()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
//                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()))
//                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
//    }
//
//    @Test
//    void getUser_expectException_onError_NullPointerException() throws Exception {
//        String message = "error message";
//        when(userService.getUser(userDto.getEmail())).thenThrow(new NullPointerException(message));
//
//        mockMvc.perform(get("/user/" + userDto.getEmail()))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(content().string(message));
//    }
//
//    @Test
//    void getUser_expectException_onError_EntityNotFoundException() throws Exception {
//        String message = "entity not found message";
//        when(userService.getUser(userDto.getEmail())).thenThrow(new EntityNotFoundException(message));
//
//        mockMvc.perform(get("/user/" + userDto.getEmail()))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value(message));
//    }
//
//    @Test
//    void createUserTest() throws Exception {
//        when(userService.createUser(userDto, "0000")).thenReturn(userDto);
//
//        mockMvc.perform(post("/user?password=0000")
//                .content(objectMapper.writeValueAsString(userDto))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
//                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()))
//                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
//    }
//
//    @Test
//    void createUser_expectException_onError_UserAlreadyExistsException() throws Exception {
//        String message = "user already exists";
//        when(userService.createUser(userDto, "8888")).thenThrow(new UserAlreadyExistsException(message));
//
//        mockMvc.perform(post("/user/?password=8888")
//                .content(objectMapper.writeValueAsString(userDto))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value(message));
//    }
//
//    @Test
//    void updateUserTest() throws Exception {
//        when(userService.updateUser(userDto.getEmail(), userDto)).thenReturn(userDto);
//
//        mockMvc.perform(put("/user/" + userDto.getEmail())
//                .content(objectMapper.writeValueAsString(userDto))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
//                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()))
//                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
//    }
//
//    @Test
//    void updateUser_expectException_onError_EntityNotFoundException() throws Exception {
//        String message = format("User with email %s is not found", userDto.getEmail());
//        when(userService.updateUser(userDto.getEmail(), userDto)).thenThrow(new EntityNotFoundException(message));
//
//        mockMvc.perform(put("/user/" + userDto.getEmail())
//                .content(objectMapper.writeValueAsString(userDto))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value(message));
//    }
//
//    @Test
//    void addLibraryTest() throws Exception {
//        String libraryName = "TestLibrary";
//        doNothing().when(userService).addLibrary(userDto.getEmail(), libraryName);
//
//        mockMvc.perform(post("/user/" + userDto.getEmail() + "/library/" + libraryName))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        verify(userService, only()).addLibrary(userDto.getEmail(), libraryName);
//    }
//
//    @Test
//    void deleteUserTest() throws Exception {
//        doNothing().when(userService).deleteUser(userDto.getEmail());
//
//        mockMvc.perform(delete("/user/" + userDto.getEmail()))
//                .andDo(print())
//                .andExpect(status().isNoContent());
//
//        verify(userService, only()).deleteUser(userDto.getEmail());
//    }
//
//    private UserDto getUserDto() {
//        return UserDto.builder()
//                .firstName("TESTNAME")
//                .lastName("LASTNAME")
//                .email("test@email.com")
//                .build();
//    }
//}
