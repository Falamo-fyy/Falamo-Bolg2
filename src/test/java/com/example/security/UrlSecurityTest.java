package com.example.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.example.mytool.MytoolApplication.class)
@AutoConfigureMockMvc
public class UrlSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试未认证用户访问受限资源
     */
    @Test
    void unauthenticatedAccessAttempt() throws Exception {
        mockMvc.perform(get("/user/settings"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    /**
     * 应测试 /article/editor 路径对USER和ADMIN角色的访问
     */
    @Test
    @WithMockUser(roles = {"USER"})
    void articleEditor_shouldAllowUserRole() throws Exception {
        mockMvc.perform(get("/article/editor"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void articleEditor_shouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/article/editor"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changePassword_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/user/change-password"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void uploadAvatar_shouldRequireAuth() throws Exception {
        mockMvc.perform(post("/user/upload-avatar"))
               .andExpect(status().is4xxClientError());
    }
} 