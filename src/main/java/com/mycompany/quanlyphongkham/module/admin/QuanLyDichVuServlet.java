package com.mycompany.quanlyphongkham.module.admin;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "QuanLyDichVuServlet", urlPatterns = {"/api/admin/dich-vu", "/api/admin/dich-vu/*"})
public class QuanLyDichVuServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    public static class CreateServiceRequest {
        public String tenDichVu;
        public BigDecimal donGia;
        public String moTa;
    }

    public static class UpdateServiceRequest {
        public String maDichVu;
        public String tenDichVu;
        public BigDecimal donGia;
        public String moTa;
        public Boolean trangThai;
        public String action; // e.g. "toggle_status"
    }

    /**
     * GET /api/admin/dich-vu : Lấy danh mục dịch vụ khám & cận lâm sàng (hỗ trợ tìm kiếm, lọc trạng thái)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = getCurrentUserRole(req);
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");

        try {
            List<Map<String, Object>> list = adminDAO.getDanhSachDichVu(role, keyword, status);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(list, "Tải danh sách dịch vụ thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy danh sách dịch vụ!");
        }
    }

    /**
     * POST /api/admin/dich-vu : Thêm mới một dịch vụ kỹ thuật / cận lâm sàng
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = getCurrentUserRole(req);
        CreateServiceRequest body = readJsonBody(req, CreateServiceRequest.class);

        if (body == null || body.tenDichVu == null || body.tenDichVu.trim().isEmpty() ||
            body.donGia == null || body.donGia.compareTo(BigDecimal.ZERO) <= 0) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng điền Tên dịch vụ và Đơn giá hợp lệ (lớn hơn 0)!", "INVALID_SERVICE_DATA"));
            return;
        }

        try {
            String newMaDichVu = adminDAO.themDichVu(role, body.tenDichVu, body.donGia, body.moTa);
            if (newMaDichVu != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("maDichVu", newMaDichVu);
                data.put("tenDichVu", body.tenDichVu.trim());
                data.put("donGia", body.donGia);
                data.put("moTa", body.moTa);
                data.put("trangThai", true);

                sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
                        ApiResponse.ok(data, "Thêm mới dịch vụ thành công (Mã: " + newMaDichVu + ")!"));
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("Không thể thêm mới dịch vụ!", "INSERT_FAILED"));
            }
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi thêm mới dịch vụ!");
        }
    }

    /**
     * PUT /api/admin/dich-vu : Cập nhật thông tin dịch vụ hoặc đổi trạng thái
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = getCurrentUserRole(req);
        UpdateServiceRequest body = readJsonBody(req, UpdateServiceRequest.class);

        if (body == null || body.maDichVu == null || body.maDichVu.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã dịch vụ cần thao tác!", "MISSING_SERVICE_ID"));
            return;
        }

        String maDichVu = FormatUtil.normalizeDichVu(body.maDichVu);
        if (maDichVu == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã dịch vụ không hợp lệ!", "INVALID_SERVICE_ID"));
            return;
        }

        try {
            // Xử lý bật/tắt trạng thái riêng biệt nếu có cờ action hoặc chỉ truyền trạng thái
            if ("toggle_status".equalsIgnoreCase(body.action) && body.trangThai != null) {
                boolean updated = adminDAO.doiTrangThaiDichVu(role, maDichVu, body.trangThai);
                if (updated) {
                    sendJsonResponse(resp, HttpServletResponse.SC_OK,
                            ApiResponse.ok(null, "Đã cập nhật trạng thái dịch vụ " + maDichVu + " thành: " + (body.trangThai ? "Đang áp dụng" : "Tạm ngưng") + "!"));
                } else {
                    sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            ApiResponse.error("Không tìm thấy dịch vụ " + maDichVu, "SERVICE_NOT_FOUND"));
                }
                return;
            }

            // Cập nhật đầy đủ thông tin dịch vụ
            if (body.tenDichVu == null || body.tenDichVu.trim().isEmpty() ||
                body.donGia == null || body.donGia.compareTo(BigDecimal.ZERO) <= 0) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error("Tên dịch vụ không được trống và đơn giá phải lớn hơn 0!", "INVALID_DATA"));
                return;
            }

            boolean updated = adminDAO.capNhatDichVu(role, maDichVu, body.tenDichVu, body.donGia, body.moTa, body.trangThai);
            if (updated) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok(null, "Cập nhật dịch vụ " + maDichVu + " thành công!"));
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        ApiResponse.error("Không tìm thấy dịch vụ " + maDichVu, "SERVICE_NOT_FOUND"));
            }
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi cập nhật dịch vụ!");
        }
    }

    /**
     * DELETE /api/admin/dich-vu?id=... : Xóa hoặc chuyển trạng thái ngưng áp dụng dịch vụ
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = getCurrentUserRole(req);
        String paramId = req.getParameter("id");
        if (paramId == null || paramId.trim().isEmpty()) {
            paramId = req.getParameter("maDichVu");
        }

        // Hỗ trợ path param nếu có: /api/admin/dich-vu/DV001
        if (paramId == null || paramId.trim().isEmpty()) {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.length() > 1) {
                paramId = pathInfo.substring(1);
            }
        }

        if (paramId == null || paramId.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã dịch vụ cần xóa!", "MISSING_SERVICE_ID"));
            return;
        }

        String maDichVu = FormatUtil.normalizeDichVu(paramId);
        if (maDichVu == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã dịch vụ không hợp lệ!", "INVALID_SERVICE_ID"));
            return;
        }

        try {
            Map<String, Object> result = adminDAO.xoaHoacKhoaDichVu(role, maDichVu);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(result, (String) result.get("message")));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi xóa dịch vụ!");
        }
    }
}
