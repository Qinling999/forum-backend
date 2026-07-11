package com.example.forum.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String role = (String) request.getAttribute("role");

        if (role == null) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }

        if (!"admin".equals(role)) {
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"无权限\"}");
            return false;
        }

        return true;
    }
}