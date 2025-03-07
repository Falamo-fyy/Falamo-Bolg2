package com.example.mytool.service;

import com.example.mytool.entity.User; // 假设存在 UserEntity 类
import com.example.mytool.security.CustomUserDetails;
import org.springframework.context.annotation.Primary;
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

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 假设角色为 ROLE_USER

        return new CustomUserDetails(user.getUsername(), user.getPassword(), authorities,user.getEmail());
    }
}