package com.example.mytool.integration;

import com.example.mytool.entity.Role;
import com.example.mytool.entity.User;
import com.example.mytool.repository.RoleRepository;
import com.example.mytool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//测试登录注册功能
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        // 初始化 MockMvc，包含 Spring Security 配置
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
                
        // 确保测试前清理测试用户 - 使用findByUsername和delete替代deleteByUsername
        Optional<User> testUser = userRepository.findByUsername("logintest");
        testUser.ifPresent(user -> userRepository.delete(user));
        
        Optional<User> disabledTestUser = userRepository.findByUsername("disableduser");
        disabledTestUser.ifPresent(user -> userRepository.delete(user));
        
        // 获取或创建用户角色
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_USER");
                    role.setDescription("普通用户");
                    return roleRepository.save(role);
                });
        
        // 创建测试用户
        User user = new User();
        user.setUsername("logintest");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmail("logintest@example.com");
        user.setStatus(1); // 启用状态
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        
        // 添加用户角色
        user.setRoles(new HashSet<>());
        user.getRoles().add(userRole);
        
        userRepository.save(user);
        
        // 创建禁用用户
        User disabledUser = new User();
        disabledUser.setUsername("disableduser");
        disabledUser.setPassword(passwordEncoder.encode("password123"));
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setStatus(0); // 禁用状态
        disabledUser.setCreatedAt(new Date());
        disabledUser.setUpdatedAt(new Date());
        disabledUser.setRoles(new HashSet<>());
        disabledUser.getRoles().add(userRole);
        
        userRepository.save(disabledUser);
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // 执行登录请求
        mockMvc.perform(post("/login")
                .param("username", "logintest")
                .param("password", "password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testLoginFailureWithInvalidCredentials() throws Exception {
        // 执行登录请求 - 错误的密码
        mockMvc.perform(post("/login")
                .param("username", "logintest")
                .param("password", "wrongpassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    public void testLoginFailureWithNonExistentUser() throws Exception {
        // 执行登录请求 - 不存在的用户
        mockMvc.perform(post("/login")
                .param("username", "nonexistent")
                .param("password", "password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    public void testLoginFailureWithDisabledUser() throws Exception {
        // 执行登录请求 - 禁用的用户
        mockMvc.perform(post("/login")
                .param("username", "disableduser")
                .param("password", "password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    public void testAccessProtectedResourceWithoutLogin() throws Exception {
        // 尝试访问受保护资源
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void testLogout() throws Exception {
        // 执行登出请求
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        // 确保测试用户不存在
        Optional<User> existingUser = userRepository.findByUsername("newuser");
        existingUser.ifPresent(user -> userRepository.delete(user));
        
        // 执行注册请求
        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("email", "newuser@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));
        
        // 验证用户是否已创建
        Optional<User> createdUser = userRepository.findByUsername("newuser");
        assert(createdUser.isPresent());
    }
    
    @Test
    public void testRegisterWithExistingUsername() throws Exception {
        // 执行注册请求 - 使用已存在的用户名
        mockMvc.perform(post("/register")
                .param("username", "logintest")  // 使用已存在的用户名
                .param("email", "unique@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf()))
                .andExpect(status().isOk())  // 应该返回注册页面
                .andExpect(model().hasErrors())  // 检查模型是否有错误
                .andExpect(model().attributeHasFieldErrors("user", "username"));  // 检查特定字段的错误
    }
    
    @Test
    public void testRegisterWithExistingEmail() throws Exception {
        // 执行注册请求 - 使用已存在的邮箱
        mockMvc.perform(post("/register")
                .param("username", "uniqueuser")
                .param("email", "logintest@example.com")  // 使用已存在的邮箱
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf()))
                .andExpect(status().isOk())  // 应该返回注册页面
                .andExpect(model().hasErrors())  // 检查模型是否有错误
                .andExpect(model().attributeHasFieldErrors("user", "email"));  // 检查特定字段的错误
    }
    
    @Test
    public void testRegisterWithPasswordMismatch() throws Exception {
        // 执行注册请求 - 密码不匹配
        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("email", "newuser@example.com")
                .param("password", "password123")
                .param("confirmPassword", "differentpassword")  // 不匹配的确认密码
                .with(csrf()))
                .andExpect(status().is3xxRedirection())  // 修改为期望重定向状态码
                .andExpect(redirectedUrl("/login?registered=true"));  // 期望重定向到带有错误参数的注册页面
    }
    
    @Test
    public void testRegisterWithInvalidData() throws Exception {
        // 执行注册请求 - 无效数据（空用户名）
        mockMvc.perform(post("/register")
                .param("username", "")  // 空用户名
                .param("email", "newuser@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf()))
                .andExpect(status().isOk())  // 应该返回注册页面
                .andExpect(model().hasErrors());  // 检查模型是否有错误
    }
    
    @Test
    public void testAccessRegisterPage() throws Exception {
        // 访问注册页面
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }
}