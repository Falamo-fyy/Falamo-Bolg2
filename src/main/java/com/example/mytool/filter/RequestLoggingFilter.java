package com.example.mytool.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 记录请求信息
        System.out.println("收到请求: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("Content-Type: " + request.getContentType());
        
        // 记录请求头
        Collections.list(request.getHeaderNames()).forEach(headerName -> 
            System.out.println("Header: " + headerName + " = " + request.getHeader(headerName))
        );
        
        // 包装响应以记录状态码
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        
        try {
            // 继续过滤链
            filterChain.doFilter(request, responseWrapper);
        } finally {
            // 记录响应信息
            System.out.println("响应状态: " + responseWrapper.getStatus());
            System.out.println("响应Content-Type: " + responseWrapper.getContentType());
        }
    }
    
    // 响应包装类
    private static class ResponseWrapper extends HttpServletResponseWrapper {
        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
    }
} 