package com.library.library.service;

import com.library.library.controller.dto.Role;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.exception.UserAlreadyExistsException;
import com.library.library.service.impl.UserServiceImpl;
import com.library.library.service.mapper.UserMapper;
import com.library.library.service.model.User;
import com.library.library.service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final User user = getUser();
    private final UserDto userDto = getUserDto();

    public UserServiceImplTest() throws ParseException {
    }

    @Test
    void isEmailAlreadyInUseTest() {
        when(userRepository.existsUserByEmail(userDto.getEmail())).thenReturn(true);
        assertTrue(userService.isEmailAlreadyInUse(userDto.getEmail()));
    }

    @Test
    void getUserTest() {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(UserMapper.INSTANCE.mapUser(userDto));

        //when
        UserDto actualUserDto = userService.getUser(userDto.getEmail());

        //then
        assertEquals(userDto, actualUserDto);
    }


    @Test
    public void pageUsersTest() {
        Pageable pageable = PageRequest.of(0, 12);

        List<User> users = Collections.singletonList(user);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        //given
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        //when
        Page<UserDto> actualPage = userService.pageUsers(pageable);

        //then
        List<UserDto> userDtos = Collections.singletonList(userDto);
        Page<UserDto> expectedPage = new PageImpl<>(userDtos, pageable, userDtos.size());
        assertEquals(expectedPage, actualPage);
    }

    @Test()
    public void createUserTest() {
        //given
        when(userRepository.existsUserByEmail(userDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn(userDto.getPassword());
        when(userRepository.save(isA(User.class))).thenReturn(user);

        //when
        UserDto actual = userService.createUser(userDto);

        //then
        assertEquals(userDto, actual);
    }

    @Test
    public void createUserAlreadyExistsExceptionTest() {
        //given
        when(userRepository.existsUserByEmail(userDto.getEmail())).thenReturn(true);

        //when
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(userDto));

        //then
        verify(userRepository, only()).existsUserByEmail(userDto.getEmail());
        verify(userRepository, never()).save(isA(User.class));
    }

    @Test
    public void updateUserTest() {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn(userDto.getPassword());
        when(userRepository.save(isA(User.class))).thenReturn(user);

        //when
        UserDto actual = userService.updateUser(userDto.getEmail(), userDto);

        //then
        assertEquals(userDto, actual);
        verify(userRepository).findUserByEmail(user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    public void updateUserFirstNameAndLastNameAndEmailAndPasswordNullTest() {
        UserDto updateUserDto = userDto;
        updateUserDto.setFirstName(null);
        updateUserDto.setLastName(null);
        updateUserDto.setEmail(null);
        updateUserDto.setPassword(null);
        User updateUser = UserMapper.INSTANCE.mapUser(updateUserDto);
        //given
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(updateUser);
        when(userRepository.save(isA(User.class))).thenReturn(updateUser);

        //when
        UserDto actual = userService.updateUser(user.getEmail(), updateUserDto);

        //then
        assertEquals(updateUserDto, actual);
        verify(userRepository).findUserByEmail(user.getEmail());
        verify(userRepository).save(updateUser);
    }

    @Test
    public void updateUserRoleAndPhoneAndBirthdayAndCountryNullTest() {
        UserDto updateUserDto = userDto;
        updateUserDto.setRole(null);
        updateUserDto.setPhone(null);
        updateUserDto.setBirthday(null);
        updateUserDto.setCountry(null);
        User updateUser = UserMapper.INSTANCE.mapUser(updateUserDto);
        //given
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(updateUser);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn(userDto.getPassword());
        when(userRepository.save(isA(User.class))).thenReturn(updateUser);

        //when
        UserDto actual = userService.updateUser(user.getEmail(), updateUserDto);

        //then
        assertEquals(updateUserDto, actual);
        verify(userRepository).findUserByEmail(user.getEmail());
        verify(userRepository).save(updateUser);
    }

    @Test
    public void updateUserCityAndAddressAndPostalCodeNullTest() {
        UserDto updateUserDto = userDto;
        updateUserDto.setCity(null);
        updateUserDto.setAddress(null);
        updateUserDto.setPostalCode(null);
        User updateUser = UserMapper.INSTANCE.mapUser(updateUserDto);
        //given
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(updateUser);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn(userDto.getPassword());
        when(userRepository.save(isA(User.class))).thenReturn(updateUser);

        //when
        UserDto actual = userService.updateUser(user.getEmail(), updateUserDto);

        //then
        assertEquals(updateUserDto, actual);
        verify(userRepository).findUserByEmail(user.getEmail());
        verify(userRepository).save(updateUser);
    }

    @Test
    void deleteUserTest() {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        doNothing().when(userRepository).delete(user);

        //when
        userService.deleteUser(userDto.getEmail());

        //then
        verify(userRepository).findUserByEmail(userDto.getEmail());
        verify(userRepository).delete(user);
    }

    private User getUser() throws ParseException {
        return UserMapper.INSTANCE.mapUser(getUserDto());
    }

    private UserDto getUserDto() throws ParseException {
        return UserDto.builder()
                .firstName("test name")
                .lastName("last name")
                .email("test@email.com")
                .password("12345q")
                .role(Role.USER)
                .phone("0986555423")
                .birthday(new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).parse("7-Jun-1987"))
                .country("English")
                .city("London")
                .address("St. She 2")
                .postalCode("12345")
                .build();
    }
}
