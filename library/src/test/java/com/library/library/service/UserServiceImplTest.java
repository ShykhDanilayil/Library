package com.library.library.service;

import com.library.library.controller.dto.UserDto;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.UserAlreadyExistsException;
import com.library.library.service.impl.UserServiceImpl;
import com.library.library.service.mapper.UserMapper;
import com.library.library.service.model.Library;
import com.library.library.service.model.User;
import com.library.library.service.repository.LibraryRepository;
import com.library.library.service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private LibraryRepository libraryRepository;

    private final User user = getUser();
    private final UserDto userDto = getUserDto();
    private final Library library = getLibrary();
    private final String MOCK_EMAIL = "EMAIL";

    @Test
    void getUserByEmailTest() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.of(user));

        //when
        UserDto expectedUserDTO = UserMapper.INSTANCE.mapUserDto(user);
        UserDto actualUserDto = userService.getUser(MOCK_EMAIL);

        //then
        assertEquals(expectedUserDTO, actualUserDto);
    }

    @Test
    void getUserByEmailWithExceptionTest() {
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> userService.getUser(MOCK_EMAIL));
        verify(userRepository,only()).findUserByEmail(MOCK_EMAIL);
    }

    @Test
    public void listUsersTest() {
        //given
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(Page.empty());

        //when
        List<UserDto> users = userService.listUsers();

        //then
        assertThat(users, hasSize(0));
    }

    @Test
    public void createUserTest() {
        String password = "password";
        UserDto expected = UserMapper.INSTANCE.mapUserDto(user);
        //given
        when(userRepository.existsUserByEmail(MOCK_EMAIL)).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        //when
        UserDto actual = userService.createUser(userDto, password);

        //then
        assertEquals(expected, actual);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void createUserWithExceptionTest() {
        String password = "password";
        UserDto expected = UserMapper.INSTANCE.mapUserDto(user);
        //given
        when(userRepository.existsUserByEmail(MOCK_EMAIL)).thenReturn(true);

        //then
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(userDto, password));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void updateUserTest() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.of(user));

        //when
        UserDto actual = userService.updateUser(MOCK_EMAIL, userDto);

        //then
        assertEquals(userDto, actual);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void updateUserTestUpdateOnlyFirstName() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.of(user));

        //when
        String lastName = userDto.getLastName();
        userDto.setEmail(null);
        userDto.setLastName(null);
        UserDto actual = userService.updateUser(MOCK_EMAIL, userDto);

        //then
        User expected = User.builder()
                .email(user.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(user.getLastName())
                .build();
//        userDto.setFirstName(user.getFirstName());
        assertEquals(UserMapper.INSTANCE.mapUserDto(expected), actual);
        verify(userRepository, times(1)).save(user);
        userDto.setEmail(MOCK_EMAIL);
        userDto.setLastName(lastName);
    }

    @Test
    public void updateUserTestUpdateLastNameAndEmail() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.of(user));

        //when
        String firstName = userDto.getFirstName();
        userDto.setFirstName(null);
        UserDto actual = userService.updateUser(MOCK_EMAIL, userDto);

        //then
        User expected = User.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(userDto.getLastName())
                .build();
        assertEquals(UserMapper.INSTANCE.mapUserDto(expected), actual);
        verify(userRepository, times(1)).save(user);
        userDto.setFirstName(firstName);
    }

    @Test
    public void updateUserWithExceptionTest() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.empty());

        //then
        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(MOCK_EMAIL, userDto));
        verify(userRepository, never()).save(user);
    }

    @Test
    public void addLibraryTest() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.of(user));
        when(libraryRepository.getLibraryByLibraryName(library.getLibraryName())).thenReturn(Optional.of(library));

        //when
        userService.addLibrary(MOCK_EMAIL, library.getLibraryName());

        //then
        verify(userRepository, times(1)).findUserByEmail(MOCK_EMAIL);
        verify(libraryRepository, times(1)).getLibraryByLibraryName(library.getLibraryName());
        verify(userRepository, times(1)).save(user);
        verify(libraryRepository, times(1)).save(library);
    }

    @Test
    public void addLibraryWithExceptionUserTest() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.empty());

        //when
        assertThrows(EntityNotFoundException.class,
                () -> userService.addLibrary(MOCK_EMAIL, library.getLibraryName()));

        //then
        verify(userRepository, only()).findUserByEmail(MOCK_EMAIL);
        verify(libraryRepository, never()).getLibraryByLibraryName(library.getLibraryName());
        verify(userRepository, never()).save(user);
        verify(libraryRepository, never()).save(library);
    }

    @Test
    public void addLibraryWithExceptionLibraryTest2() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.of(user));
        when(libraryRepository.getLibraryByLibraryName(library.getLibraryName())).thenReturn(Optional.empty());

        //when
        assertThrows(EntityNotFoundException.class,
                () -> userService.addLibrary(MOCK_EMAIL, library.getLibraryName()));

        //then
        verify(userRepository, only()).findUserByEmail(MOCK_EMAIL);
        verify(libraryRepository, only()).getLibraryByLibraryName(library.getLibraryName());
        verify(userRepository, never()).save(user);
        verify(libraryRepository, never()).save(library);
    }

    @Test
    void deleteUserTest() {
        //given
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        //when
        userService.deleteUser(MOCK_EMAIL);

        //then
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUserWithExceptionTest() {
        when(userRepository.findUserByEmail(MOCK_EMAIL)).thenReturn(Optional.empty());
        //then
        assertThrows(EntityNotFoundException.class,
                () -> userService.deleteUser(MOCK_EMAIL));
        verify(userRepository, never()).delete(user);
    }

    private User getUser() {
        return User.builder()
                .email(MOCK_EMAIL)
                .firstName("TEST")
                .libraries(new ArrayList<>())
                .build();
    }

    private UserDto getUserDto() {
        return UserDto.builder()
                .firstName("TEST")
                .lastName("TEST LASTNAME")
                .email(MOCK_EMAIL)
                .build();
    }

    private Library getLibrary() {
        Library lib = new Library();
        lib.setLibraryName("TEST LIB");
        lib.setAddress("LVIV");
        return lib;
    }
}
