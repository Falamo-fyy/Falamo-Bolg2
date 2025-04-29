package com.example.mytool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import javax.sql.DataSource;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private DataSource dataSource; // 确保已配置数据源

    // 添加持久化令牌仓库配置
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        tokenRepository.setCreateTableOnStartup(false); // 如果第一次运行可以设置为true自动建表
        return tokenRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(auth -> auth
                .antMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/article/editor").hasAnyRole("USER", "ADMIN")
                .antMatchers("/user/**").authenticated()
                .antMatchers("/user/articles/**").authenticated()
                .antMatchers(HttpMethod.POST, "/user/change-password").authenticated()
                .antMatchers(HttpMethod.POST, "/api/articles").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/articles/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/articles/**").authenticated()
                .antMatchers(HttpMethod.POST, "/user/upload-avatar").authenticated()
                .antMatchers("/user/change-password").authenticated()
                .antMatchers("/api/hot-articles/**").permitAll() // 允许访问热门文章API
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
            )
            .rememberMe(remember -> remember
                .userDetailsService(userDetailsService)
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
            )
            .csrf(csrf -> csrf
                .ignoringAntMatchers("/api/hot-articles/**") // 为热门文章API禁用CSRF保护
            )
            .userDetailsService(userDetailsService);
        return http.build();
    }
}