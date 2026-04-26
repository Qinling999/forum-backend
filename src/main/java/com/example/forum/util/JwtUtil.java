package com.example.forum.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // 密钥（正式环境建议放配置文件）
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 过期时间（1天）
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    // 生成 Token
    public static String generateToken(String userId, String username, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    // 解析 Token
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 获取 userId
    public static String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    // 校验 Token
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
}