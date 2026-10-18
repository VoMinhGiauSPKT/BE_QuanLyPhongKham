package com.mycompany.quanlyphongkham.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class JwtUtil {

    private static SecretKey getSigningKey() {
        String secret = EnvConfig.get("JWT_SECRET", "super_secret_key_phong_kham_quan_ly_2026_at_least_256_bits_long!");
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                keyBytes = md.digest(keyBytes);
            } catch (NoSuchAlgorithmException ignored) {}
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(String maNguoiDung, String username, String hoTen, String vaiTro) {
        long expirationMs = Long.parseLong(EnvConfig.get("JWT_EXPIRATION_MS", "86400000"));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("ma_nguoi_dung", maNguoiDung)
                .claim("ho_ten", hoTen)
                .claim("vai_tro", vaiTro)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
