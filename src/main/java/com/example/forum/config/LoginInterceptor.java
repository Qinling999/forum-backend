package com.example.forum.config;

import com.example.forum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        // ⭐ 统一处理 token
        token = token.trim();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            String userId = JwtUtil.getUserId(token);
            String role = JwtUtil.getRole(token);

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

        } catch (Exception e) {

            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write("{\"code\":401,\"message\":\"token无效\"}");

            return false; // ❗必须拦截
        }

        return true;
    }
}
