package com.example.mytool.service;

import com.example.mytool.dto.UserRegistrationDto;
import com.example.mytool.entity.Role;
import com.example.mytool.entity.User;
import com.example.mytool.exception.EmailExistsException;
import com.example.mytool.exception.UsernameExistsException;
import com.example.mytool.repository.RoleRepository;
import com.example.mytool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto validRegistrationDto;
    private User mockUser;
    private Role mockRole;

    @BeforeEach
    public void setup() {
        // 不需要显式调用 MockitoAnnotations.openMocks(this)，因为使用了 @ExtendWith(MockitoExtension.class)
        
        // 准备有效的注册DTO
        validRegistrationDto = new UserRegistrationDto();
        validRegistrationDto.setUsername("testuser");
        validRegistrationDto.setEmail("test@example.com");
        validRegistrationDto.setPassword("password123");

        // 准备模拟用户
        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setStatus(1);
        
        // 准备模拟角色
        mockRole = new Role();
        mockRole.setName("ROLE_USER");
        mockRole.setDescription("普通用户");
        
        // 设置用户角色
        mockUser.setRoles(new HashSet<>());
        mockUser.getRoles().add(mockRole);

        // 配置模拟行为
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
    }

    @Test
    public void testRegisterUserSuccess() {
        // 配置模拟行为 - 用户名和邮箱都不存在
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // 执行测试
        User registeredUser = userService.registerUser(validRegistrationDto);

        // 验证结果
        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        assertEquals("test@example.com", registeredUser.getEmail());
        assertEquals("encodedPassword", registeredUser.getPassword());
        
        // 验证方法调用
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUserWithExistingUsername() {
        // 配置模拟行为 - 用户名已存在
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // 执行测试并验证异常
        UsernameExistsException exception = assertThrows(
            UsernameExistsException.class,
            () -> userService.registerUser(validRegistrationDto)
        );
        
        assertEquals("用户名已存在", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUserWithExistingEmail() {
        // 配置模拟行为 - 用户名不存在但邮箱已存在
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // 执行测试并验证异常
        EmailExistsException exception = assertThrows(
            EmailExistsException.class,
            () -> userService.registerUser(validRegistrationDto)
        );
        
        assertEquals("邮箱已被注册", exception.getMessage());
        
        // 验证方法调用
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoadUserByUsernameSuccess() {
        // 配置模拟行为 - 用户存在
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // 执行测试
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // 验证结果
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    public void testLoadUserByUsernameNotFound() {
        // 配置模拟行为 - 用户不存在
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("nonexistent")
        );
        
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    public void testLoadUserByUsernameDisabled() {
        // 准备禁用的用户
        User disabledUser = new User();
        disabledUser.setUsername("disabled");
        disabledUser.setPassword("encodedPassword");
        disabledUser.setStatus(0); // 禁用状态
        disabledUser.setRoles(new HashSet<>());
        disabledUser.getRoles().add(mockRole);

        // 配置模拟行为 - 返回禁用用户
        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(disabledUser));

        // 执行测试
        UserDetails userDetails = userService.loadUserByUsername("disabled");

        // 验证结果 - 用户应该被禁用
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }
}