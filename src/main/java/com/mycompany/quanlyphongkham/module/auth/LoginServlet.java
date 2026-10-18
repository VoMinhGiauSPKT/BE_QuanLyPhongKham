package com.mycompany.quanlyphongkham.module.auth;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.entity.NguoiDung;
import com.mycompany.quanlyphongkham.util.JwtUtil;
import com.mycompany.quanlyphongkham.util.PasswordUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "LoginServlet", urlPatterns = {"/api/auth/login"})
public class LoginServlet extends BaseServlet {

    private final AuthDAO authDAO = new AuthDAO();

    public static class LoginRequest {
        public String username;
        public String password;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest requestBody = readJsonBody(req, LoginRequest.class);

        if (requestBody == null || requestBody.username == null || requestBody.password == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp username và password!", "BAD_REQUEST"));
            return;
        }

        try {
            NguoiDung user = authDAO.findByUsername(requestBody.username.trim());

            if (user == null) {
                sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("Tên đăng nhập hoặc mật khẩu không chính xác!", "INVALID_CREDENTIALS"));
                return;
            }

            if (user.getHoatDong() != null && !user.getHoatDong()) {
                sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                        ApiResponse.error("Tài khoản đã bị khóa!", "ACCOUNT_LOCKED"));
                return;
            }

            // Verify password using BCrypt (or fallback if plaintext in initial test database)
            boolean passwordMatches = PasswordUtil.checkPassword(requestBody.password, user.getMatKhauHash());
            if (!passwordMatches && requestBody.password.equals(user.getMatKhauHash())) {
                passwordMatches = true;
            }

            if (!passwordMatches) {
                sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("Tên đăng nhập hoặc mật khẩu không chính xác!", "INVALID_CREDENTIALS"));
                return;
            }

            String token = JwtUtil.generateToken(
                    user.getMaNguoiDung(),
                    user.getTenDangNhap(),
                    user.getHoTen(),
                    user.getVaiTro()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("maNguoiDung", user.getMaNguoiDung());
            data.put("tenDangNhap", user.getTenDangNhap());
            data.put("hoTen", user.getHoTen());
            data.put("vaiTro", user.getVaiTro());

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Đăng nhập thành công!"));

        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi kết nối cơ sở dữ liệu khi đăng nhập!");
        }
    }
}
