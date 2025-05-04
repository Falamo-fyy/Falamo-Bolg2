package com.example.mytool.service;

import com.example.mytool.dto.UserRegistrationDto;
import com.example.mytool.entity.Role;
import com.example.mytool.entity.User;
import com.example.mytool.repository.RoleRepository;
import com.example.mytool.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

import com.example.mytool.exception.EmailExistsException;
import com.example.mytool.exception.UsernameExistsException;
import com.example.mytool.dto.UserProfileDto;
import com.example.mytool.repository.ArticleRepository;
import com.example.mytool.exception.InvalidPasswordException;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ArticleRepository articleRepository;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UsernameExistsException("用户名已存在");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailExistsException("邮箱已被注册");
        }
        
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        
        // 添加默认角色（带自动创建逻辑）
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_USER");
            newRole.setDescription("普通用户");
            return roleRepository.save(newRole);
        });
        
        user.getRoles().add(userRole);
        
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("尝试登录用户: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("用户不存在: {}", username);
                return new UsernameNotFoundException("用户不存在");
            });

        log.debug("找到用户: {}", user.getUsername());
        log.debug("用户状态: {}", user.getStatus());
        log.debug("用户角色: {}", user.getRoles());

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getStatus() == 1, // 账户启用状态
            true,  // 账户未过期
            true,  // 凭证未过期
            true,  // 账户未锁定
            getAuthorities(user.getRoles())
        );
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
            .map(role -> {
                // 确保角色名称包含ROLE_前缀
                String roleName = role.getName().startsWith("ROLE_") ? 
                    role.getName() : "ROLE_" + role.getName();
                return new SimpleGrantedAuthority(roleName);
            })
            .collect(Collectors.toList());
    }

    public void updateUserProfile(String username, UserProfileDto profileDto) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        if (!user.getEmail().equals(profileDto.getEmail()) && 
            userRepository.existsByEmail(profileDto.getEmail())) {
            throw new EmailExistsException("邮箱已被使用");
        }
        
        user.setEmail(profileDto.getEmail());
        user.setBio(profileDto.getBio());
        user.setAvatarUrl(profileDto.getAvatarUrl());
        user.setUpdatedAt(new Date());
        
        userRepository.save(user);
    }

    public UserProfileDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        
        return dto;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null); // 如果未找到用户，则返回null
    }

    @Cacheable(value = "userStats", key = "#userId")
    public Map<String, Long> getUserStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("postCount", articleRepository.countByUserId(userId));
        stats.put("likeCount", 0L); // 暂时返回0，需实现点赞功能
        return stats;
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        log.debug("Attempting password change for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("用户不存在");
                });

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Current password mismatch for user: {}", username);
            throw new InvalidPasswordException("当前密码不正确");
        }
        
        if (newPassword.equals(currentPassword)) {
            log.warn("New password same as old for user: {}", username);
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }
        
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
            log.warn("Weak password policy violation for user: {}", username);
            throw new IllegalArgumentException("密码必须包含字母和数字");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(new Date());
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", username);
    }
    
    /**
     * 删除用户
     * @param userId 要删除的用户ID
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        // 检查用户是否为管理员，防止删除管理员账户
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            throw new IllegalStateException("不能删除管理员账户");
        }
        
        // 删除用户
        userRepository.delete(user);
        log.info("用户已删除: {}", user.getUsername());
    }
    
    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}