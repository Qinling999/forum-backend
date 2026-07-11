package com.example.forum.interceptor;

import com.example.forum.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // ⭐ 放行 OPTIONS（跨域预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // ⭐ 放行帖子详情（游客可访问）
        if (uri.matches("/post/[0-9a-fA-F]{24}")) {
            return true;
        }

        String token = request.getHeader("Authorization");

        response.setContentType("application/json;charset=UTF-8");

        // ⭐ 未登录
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
            return false;
        }

        try {
            // ⭐ 统一处理 Bearer
            token = token.trim();
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = JwtUtil.parseToken(token);

            request.setAttribute("userId", claims.getSubject());
            request.setAttribute("role", claims.get("role", String.class));

            return true;

        } catch (Exception e) {

            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}");
            return false;
        }
    }
}