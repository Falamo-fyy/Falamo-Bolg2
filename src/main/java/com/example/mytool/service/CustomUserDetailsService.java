package com.example.mytool.service;

import com.example.mytool.repository.UserRepository;
import com.example.mytool.entity.User; // 假设存在 UserEntity 类
import com.example.mytool.security.CustomUserDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Primary
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;// 假设存在 UserService 类
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        // 检查用户状态，如果禁用则抛出DisabledException
        if (user.getStatus() == 0) {
            throw new DisabledException("用户已被禁用: " + username);
        }
        
        // 构建UserDetails对象
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 假设角色为 ROLE_USER

        return new CustomUserDetails(user.getUsername(), user.getPassword(), authorities,user.getEmail());
    }
}