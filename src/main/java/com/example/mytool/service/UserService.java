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

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.mytool.exception.EmailExistsException;
import com.example.mytool.exception.UsernameExistsException;
import com.example.mytool.dto.UserProfileDto;

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
        
        user.setUsername(profileDto.getUsername());
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
}