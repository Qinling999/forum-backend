package com.example.forum.config;

import com.example.forum.interceptor.AdminInterceptor;
import com.example.forum.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;
    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/post/list",
                        "/post/category/**",
                        "/post/search",
                        "/post/page",
                        "/post/searchPage",
                        "/category/**",
                        "/upload/**",
                        "/static/**",
                        "/favicon.ico",
                        "/error"
                );

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**"); // ⭐ 管理员接口
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173") // ⭐ 必须写死
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}