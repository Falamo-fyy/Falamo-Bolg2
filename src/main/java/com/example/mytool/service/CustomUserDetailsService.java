package com.example.mytool.service;

import com.example.mytool.repository.UserRepository;
import com.example.mytool.entity.User;
import com.example.mytool.entity.Role;
import com.example.mytool.security.CustomUserDetails;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserService userService, UserRepository userRepository) {
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
        
        // 从用户的角色集合中获取权限
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) getAuthorities(user.getRoles());

        return new CustomUserDetails(user.getUsername(), user.getPassword(), authorities, user.getEmail());
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

}