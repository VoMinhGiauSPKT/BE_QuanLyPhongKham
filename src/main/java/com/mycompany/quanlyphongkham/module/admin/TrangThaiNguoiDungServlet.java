package com.mycompany.quanlyphongkham.module.admin;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "TrangThaiNguoiDungServlet", urlPatterns = {"/api/admin/nguoi-dung/status", "/api/admin/nguoi-dung/reset-password"})
public class TrangThaiNguoiDungServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    public static class ToggleStatusRequest {
        public String maNguoiDung;
        public Boolean hoatDong;
    }

    public static class ResetPasswordRequest {
        public String maNguoiDung;
        public String matKhauMoi;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String path = req.getServletPath();

        if (path.endsWith("/reset-password")) {
            ResetPasswordRequest body = readJsonBody(req, ResetPasswordRequest.class);
            if (body == null || body.maNguoiDung == null || body.maNguoiDung.trim().isEmpty() ||
                body.matKhauMoi == null || body.matKhauMoi.trim().isEmpty()) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error("Vui lòng cung cấp Mã người dùng và Mật khẩu mới!", "MISSING_REQUIRED_FIELDS"));
                return;
            }

            try {
                adminDAO.datLaiMatKhau(role, body.maNguoiDung, body.matKhauMoi);
                sendJsonResponse(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok(null, "Đặt lại mật khẩu thành công!"));
            } catch (IllegalArgumentException e) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
            } catch (SQLException e) {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("Lỗi khi đặt lại mật khẩu: " + e.getMessage(), "DB_ERROR"));
            }
        } else {
            // Status toggle via POST or PUT
            handleToggleStatus(req, resp, role);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        handleToggleStatus(req, resp, role);
    }

    private void handleToggleStatus(HttpServletRequest req, HttpServletResponse resp, String role) throws IOException {
        ToggleStatusRequest body = readJsonBody(req, ToggleStatusRequest.class);
        if (body == null || body.maNguoiDung == null || body.maNguoiDung.trim().isEmpty() || body.hoatDong == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp Mã người dùng và Trạng thái hoạt động (hoatDong: true/false)!", "MISSING_REQUIRED_FIELDS"));
            return;
        }

        try {
            adminDAO.doiTrangThaiNguoiDung(role, body.maNguoiDung, body.hoatDong);
            String msg = body.hoatDong ? "Đã kích hoạt lại tài khoản nhân viên!" : "Đã khóa tài khoản nhân viên!";
            sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.ok(null, msg));
        } catch (IllegalArgumentException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (SQLException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("Lỗi khi đổi trạng thái người dùng: " + e.getMessage(), "DB_ERROR"));
        }
    }
}
