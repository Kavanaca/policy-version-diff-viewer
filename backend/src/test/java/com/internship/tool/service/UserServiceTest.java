package com.internship.tool.service;

import com.internship.tool.entity.User;
import com.internship.tool.exception.InvalidInputException;
import com.internship.tool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        UserDetails result =
            userService.loadUserByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound_Throws() {
        when(userRepository.findByUsername("unknown"))
            .thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("unknown"));
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername("newuser"))
            .thenReturn(false);
        when(userRepository.existsByEmail("new@test.com"))
            .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPass");
        when(userRepository.save(any(User.class)))
            .thenReturn(testUser);
        User result = userService.registerUser(
            "newuser", "new@test.com",
            "password123", "USER");
        assertNotNull(result);
        verify(userRepository, times(1))
            .save(any(User.class));
    }

    @Test
    void registerUser_DuplicateUsername_Throws() {
        when(userRepository.existsByUsername("testuser"))
            .thenReturn(true);
        assertThrows(InvalidInputException.class,
            () -> userService.registerUser(
                "testuser", "new@test.com",
                "password", "USER"));
        verify(userRepository, never())
            .save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_Throws() {
        when(userRepository.existsByUsername("newuser"))
            .thenReturn(false);
        when(userRepository.existsByEmail(
            "test@example.com")).thenReturn(true);
        assertThrows(InvalidInputException.class,
            () -> userService.registerUser(
                "newuser", "test@example.com",
                "password", "USER"));
    }

    @Test
    void registerUser_NullRole_DefaultsToUser() {
        when(userRepository.existsByUsername("newuser"))
            .thenReturn(false);
        when(userRepository.existsByEmail("new@test.com"))
            .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPass");
        when(userRepository.save(any(User.class)))
            .thenReturn(testUser);
        User result = userService.registerUser(
            "newuser", "new@test.com",
            "password", null);
        assertNotNull(result);
    }

    @Test
    void findByUsername_Success() {
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        User result =
            userService.findByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_NotFound_Throws() {
        when(userRepository.findByUsername("unknown"))
            .thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
            () -> userService.findByUsername("unknown"));
    }

    @Test
    void registerUser_PasswordEncoded() {
        when(userRepository.existsByUsername("newuser"))
            .thenReturn(false);
        when(userRepository.existsByEmail("new@test.com"))
            .thenReturn(false);
        when(passwordEncoder.encode("rawpassword"))
            .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(testUser);
        userService.registerUser(
            "newuser", "new@test.com",
            "rawpassword", "USER");
        verify(passwordEncoder, times(1))
            .encode("rawpassword");
    }

    @Test
    void loadUserByUsername_HasAuthorities() {
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        UserDetails result =
            userService.loadUserByUsername("testuser");
        assertNotNull(result.getAuthorities());
        assertFalse(result.getAuthorities().isEmpty());
    }
}