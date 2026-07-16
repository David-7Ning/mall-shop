package com.shopmall.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * JWT 工具类
 * 用于生成和验证 Token
 */
public class JwtUtil {

    /** 密钥（实际项目中应该从配置文件读取） */
    private static final String SECRET_KEY = "shopmall-secret-key-2024";

    /** Token 有效期：7 天（毫秒） */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))  // 主题：用户ID
                .claim("username", username)          // 自定义声明：用户名
                .setIssuedAt(new Date())              // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // 签名算法和密钥
                .compact();
    }

    /**
     * 解析 Token
     * @param token JWT Token
     * @return Claims（包含用户信息）
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}