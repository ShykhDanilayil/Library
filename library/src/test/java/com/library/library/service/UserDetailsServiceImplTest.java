package com.library.library.service;

import com.library.library.controller.dto.Role;
import com.library.library.service.impl.UserDetailsServiceImpl;
import com.library.library.service.model.MyUserPrincipal;
import com.library.library.service.model.User;
import com.library.library.service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;


    @Test
    void loadUserByUsernameTest() {
        UserDetails expectedDetails = new MyUserPrincipal(getUser());
        //given
        when(userRepository.findUserByEmail(getUser().getEmail())).thenReturn(getUser());
        //when
        UserDetails actualDetails = userDetailsService.loadUserByUsername(getUser().getEmail());
        //then
        assertEquals(expectedDetails.getUsername(), actualDetails.getUsername());
    }

    @Test
    void loadUserByUsernameNullTest() {
        when(userRepository.findUserByEmail(getUser().getEmail())).thenReturn(null);
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(getUser().getEmail()));
    }

    private User getUser() {
        return User.builder()
                .email("test@email.com")
                .role(Role.USER)
                .build();
    }
}
