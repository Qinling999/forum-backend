package com.example.forum.interceptor;

import com.example.forum.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();

        // ======================================
        // 放行：帖子详情页 /post/123 /post/xxx （纯ID路径）
        // ======================================
        if (uri.matches("/post/[0-9a-fA-F]{24}")) { // MongoDB ID 格式
            return true;
        }

        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        if (path.contains("/login") || path.contains("/register")) {
            return true;
        }

        String token = request.getHeader("Authorization");

        // ⭐ 设置编码（必须有）
        response.setContentType("application/json;charset=UTF-8");

        if (token == null || token.isEmpty()) {

            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
            return false;
        }

        try {
            Claims claims = JwtUtil.parseToken(token);

            request.setAttribute("userId", claims.getSubject());

            return true;

        } catch (Exception e) {

            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}");
            return false;
        }
    }
}