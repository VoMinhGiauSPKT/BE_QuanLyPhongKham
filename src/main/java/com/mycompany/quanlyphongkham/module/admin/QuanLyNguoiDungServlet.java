package com.mycompany.quanlyphongkham.module.admin;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "QuanLyNguoiDungServlet", urlPatterns = {"/api/admin/nguoi-dung"})
public class QuanLyNguoiDungServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    public static class CreateUserRequest {
        public String tenDangNhap;
        public String matKhau;
        public String hoTen;
        public String soDienThoai;
        public String vaiTro; // 'BAC_SI', 'LE_TAN', 'DUOC_SI', 'QUAN_TRI'
        public String chuyenKhoa;
        public String soChungChi;
        public String phongKham;
    }

    public static class UpdateUserRequest {
        public String maNguoiDung;
        public String hoTen;
        public String soDienThoai;
        public String vaiTro;
        public String chuyenKhoa;
        public String soChungChi;
        public String phongKham;
    }

    /**
     * GET /api/admin/nguoi-dung : Tra cứu danh sách nhân sự phòng khám
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String vaiTroFilter = req.getParameter("role");
        String keyword = req.getParameter("q");

        try {
            List<Map<String, Object>> list = adminDAO.getDanhSachNguoiDung(role, vaiTroFilter, keyword);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.ok(list));
        } catch (SQLException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("Lỗi khi tải danh sách người dùng: " + e.getMessage(), "DB_ERROR"));
        }
    }

    /**
     * POST /api/admin/nguoi-dung : Khai báo nhân sự / bác sĩ mới
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        CreateUserRequest body = readJsonBody(req, CreateUserRequest.class);

        if (body == null || body.tenDangNhap == null || body.tenDangNhap.trim().isEmpty() ||
            body.matKhau == null || body.matKhau.trim().isEmpty() ||
            body.hoTen == null || body.hoTen.trim().isEmpty() ||
            body.vaiTro == null || body.vaiTro.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng nhập đầy đủ Tên đăng nhập, Mật khẩu, Họ tên và Vai trò!", "MISSING_REQUIRED_FIELDS"));
            return;
        }

        try {
            Map<String, Object> created = adminDAO.themNguoiDung(
                    role,
                    body.tenDangNhap,
                    body.matKhau,
                    body.hoTen,
                    body.soDienThoai,
                    body.vaiTro,
                    body.chuyenKhoa,
                    body.soChungChi,
                    body.phongKham
            );
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
                    ApiResponse.ok(created, "Thêm nhân viên mới thành công!"));
        } catch (IllegalArgumentException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (SQLException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("Lỗi CSDL khi thêm nhân viên: " + e.getMessage(), "DB_ERROR"));
        }
    }

    /**
     * PUT /api/admin/nguoi-dung : Cập nhật thông tin nhân sự / bác sĩ
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        UpdateUserRequest body = readJsonBody(req, UpdateUserRequest.class);

        if (body == null || body.maNguoiDung == null || body.maNguoiDung.trim().isEmpty() ||
            body.hoTen == null || body.hoTen.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp Mã người dùng và Họ tên!", "MISSING_REQUIRED_FIELDS"));
            return;
        }

        try {
            adminDAO.capNhatNguoiDung(
                    role,
                    body.maNguoiDung,
                    body.hoTen,
                    body.soDienThoai,
                    body.vaiTro,
                    body.chuyenKhoa,
                    body.soChungChi,
                    body.phongKham
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(null, "Cập nhật thông tin nhân viên thành công!"));
        } catch (IllegalArgumentException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (SQLException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("Lỗi CSDL khi cập nhật nhân viên: " + e.getMessage(), "DB_ERROR"));
        }
    }
}
