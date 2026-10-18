package com.mycompany.quanlyphongkham.middleware;

import com.google.gson.Gson;
import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/api/*")
public class JwtAuthenticationFilter implements Filter {

    private static final Gson gson = new Gson();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Skip pre-flight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String uri = request.getRequestURI();

        // Whitelisted public endpoints
        if (uri.endsWith("/api/auth/login")) {
            chain.doFilter(req, res);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Chưa đăng nhập! Vui lòng cung cấp Bearer token.");
            return;
        }

        String token = authHeader.substring(7).trim();
        try {
            Claims claims = JwtUtil.parseToken(token);

            String role = claims.get("vai_tro", String.class);
            Object userIdObj = claims.get("ma_nguoi_dung");
            String userId = userIdObj != null ? userIdObj.toString() : null;

            String username = claims.getSubject();
            String fullName = claims.get("ho_ten", String.class);

            request.setAttribute("USER_ROLE", role);
            request.setAttribute("USER_ID", userId);
            request.setAttribute("USER_NAME", username);
            request.setAttribute("USER_FULL_NAME", fullName);

            chain.doFilter(req, res);
        } catch (JwtException | IllegalArgumentException e) {
            sendUnauthorized(response, "Token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(gson.toJson(ApiResponse.error(message, "UNAUTHORIZED")));
    }
}
