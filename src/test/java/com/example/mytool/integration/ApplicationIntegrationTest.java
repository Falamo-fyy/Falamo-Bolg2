package com.example.mytool.integration;

import com.example.mytool.MytoolApplication;
import com.example.mytool.dto.UserRegistrationDto;
import com.example.mytool.entity.Role;
import com.example.mytool.entity.User;
import com.example.mytool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MytoolApplication.class)
@AutoConfigureMockMvc
@Transactional
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        // 初始化测试用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("$2a$10$E9Zvqk5Zq3q3q3q3q3q3quq3q3q3q3q3q3q3q3q3q3q3q3q3");
        user.setEmail("test@example.com");
        user.setStatus(1);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        
        // 添加用户角色
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        user.getRoles().add(userRole);
        
        userRepository.save(user);
    }

    @Test
    void testPublicAccess() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("技术博客")));
    }

    @Test
    void testUserRegistrationFlow() throws Exception {
        UserRegistrationDto registration = new UserRegistrationDto();
        registration.setUsername("falamo1");
        registration.setEmail("itheima@itcast.cn");
        registration.setPassword("falamo1");

        // 测试注册成功
        mockMvc.perform(post("/register")
                .flashAttr("userRegistrationDto", registration)
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login?success"));

        // 测试重复用户名
        mockMvc.perform(post("/register")
                .flashAttr("userRegistrationDto", registration)
                .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("已存在")));
    }

    @Test
    void testArticleLifecycle() throws Exception {
        // 登录测试用户
        mockMvc.perform(post("/login")
                .param("username", "testuser")
                .param("password", "password")
                .with(csrf()))
               .andExpect(status().is3xxRedirection());

        // 创建文章
        mockMvc.perform(post("/api/articles")
                .param("title", "Test Article")
                .param("content", "Test Content")
                .with(csrf()))
               .andExpect(status().isOk());

        // 编辑文章（需要实现文章ID获取逻辑）
        mockMvc.perform(put("/api/articles/1")
                .param("title", "Updated Title")
                .param("content", "Updated Content")
                .with(csrf()))
               .andExpect(status().isOk());

        // 删除文章
        mockMvc.perform(delete("/api/articles/1")
                .with(csrf()))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testAuthenticatedAccess() throws Exception {
        mockMvc.perform(get("/user/profile"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("个人中心")));
    }

    @Test
    void testInactiveUserAccess() throws Exception {
        // 创建未激活用户
        User inactiveUser = new User();
        inactiveUser.setUsername("inactive");
        inactiveUser.setPassword("password");
        inactiveUser.setStatus(0);
        userRepository.save(inactiveUser);

        mockMvc.perform(post("/login")
                .param("username", "inactive")
                .param("password", "password")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithMockUser(username = "userA", roles = "USER")
    void testResourceLevelSecurity() throws Exception {
        // 尝试访问其他用户的资源
        mockMvc.perform(get("/user/userB/profile"))
               .andExpect(status().isForbidden());
    }

    @Test
    void testFileUploadValidation() throws Exception {
        mockMvc.perform(multipart("/user/upload-avatar")
                .file("avatar", new byte[3*1024*1024]) // 3MB文件
                .with(csrf()))
               .andExpect(status().is4xxClientError());
    }
} 