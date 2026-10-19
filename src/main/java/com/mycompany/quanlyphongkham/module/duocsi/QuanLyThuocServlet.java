package com.mycompany.quanlyphongkham.module.duocsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "QuanLyThuocServlet", urlPatterns = {"/api/duoc-si/thuoc"})
public class QuanLyThuocServlet extends BaseServlet {

    private final DuocSiDAO duocSiDAO = new DuocSiDAO();

    public static class CreateDrugRequest {
        public String tenThuoc;
        public String donViTinh;
        public Integer soLuongTon;
        public BigDecimal donGia;
        public String hanSuDung; // YYYY-MM-DD
    }

    public static class UpdateDrugRequest {
        public String maThuoc;
        public String tenThuoc;
        public String donViTinh;
        public BigDecimal donGia;
        public String hanSuDung; // YYYY-MM-DD
        public Boolean trangThai;
    }

    /**
     * POST /api/duoc-si/thuoc : Khai báo thuốc mới vào kho
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        CreateDrugRequest body = readJsonBody(req, CreateDrugRequest.class);

        if (body == null || body.tenThuoc == null || body.tenThuoc.trim().isEmpty() ||
            body.donViTinh == null || body.donViTinh.trim().isEmpty() ||
            body.donGia == null || body.donGia.compareTo(BigDecimal.ZERO) <= 0 ||
            body.hanSuDung == null || body.hanSuDung.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng điền đầy đủ Tên thuốc, Đơn vị tính, Đơn giá (> 0) và Hạn sử dụng!", "MISSING_REQUIRED_FIELDS"));
            return;
        }

        java.sql.Date sqlHsd;
        try {
            String cleanDate = body.hanSuDung.trim();
            if (cleanDate.contains("T")) {
                cleanDate = cleanDate.substring(0, cleanDate.indexOf("T"));
            }
            sqlHsd = java.sql.Date.valueOf(cleanDate);
        } catch (Exception e) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Hạn sử dụng không đúng định dạng YYYY-MM-DD!", "INVALID_EXPIRY_DATE"));
            return;
        }

        int qty = (body.soLuongTon != null && body.soLuongTon >= 0) ? body.soLuongTon : 0;

        try {
            String newMaThuoc = duocSiDAO.themThuocMoi(role, body.tenThuoc, body.donViTinh, qty, body.donGia, sqlHsd);
            if (newMaThuoc != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("maThuoc", newMaThuoc);
                data.put("tenThuoc", body.tenThuoc.trim());
                data.put("donViTinh", body.donViTinh.trim());
                data.put("soLuongTon", qty);
                data.put("donGia", body.donGia);
                data.put("hanSuDung", sqlHsd.toString());

                sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
                        ApiResponse.ok(data, "Khai báo thuốc mới thành công (Mã thuốc: " + newMaThuoc + ")!"));
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("Không thể tạo mới thuốc!", "INSERT_FAILED"));
            }
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi thêm mới thuốc!");
        }
    }

    /**
     * PUT /api/duoc-si/thuoc : Cập nhật thông tin thuốc (Khi sửa đơn giá sẽ tự động kích hoạt trigger ghi nhật ký giá)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        UpdateDrugRequest body = readJsonBody(req, UpdateDrugRequest.class);

        if (body == null || body.maThuoc == null || body.maThuoc.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã thuốc cần chỉnh sửa!", "MISSING_DRUG_ID"));
            return;
        }

        String maThuoc = FormatUtil.normalizeThuoc(body.maThuoc);
        if (maThuoc == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã thuốc không hợp lệ!", "INVALID_DRUG_ID"));
            return;
        }

        java.sql.Date sqlHsd = null;
        if (body.hanSuDung != null && !body.hanSuDung.trim().isEmpty()) {
            try {
                String cleanDate = body.hanSuDung.trim();
                if (cleanDate.contains("T")) {
                    cleanDate = cleanDate.substring(0, cleanDate.indexOf("T"));
                }
                sqlHsd = java.sql.Date.valueOf(cleanDate);
            } catch (Exception e) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error("Hạn sử dụng không đúng định dạng YYYY-MM-DD!", "INVALID_EXPIRY_DATE"));
                return;
            }
        }

        try {
            boolean updated = duocSiDAO.capNhatThuoc(
                    role,
                    maThuoc,
                    body.tenThuoc,
                    body.donViTinh,
                    body.donGia,
                    sqlHsd,
                    body.trangThai
            );

            if (updated) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok(null, "Cập nhật thông tin thuốc " + maThuoc + " thành công!"));
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        ApiResponse.error("Không tìm thấy thuốc có mã " + maThuoc, "DRUG_NOT_FOUND"));
            }
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi cập nhật thông tin thuốc!");
        }
    }

    /**
     * DELETE /api/duoc-si/thuoc?maThuoc=... : Xóa hoặc tạm ngưng kinh doanh thuốc
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String paramId = req.getParameter("maThuoc");
        if (paramId == null || paramId.trim().isEmpty()) {
            paramId = req.getParameter("id");
        }

        if (paramId == null || paramId.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã thuốc cần xóa!", "MISSING_DRUG_ID"));
            return;
        }

        String maThuoc = FormatUtil.normalizeThuoc(paramId);
        if (maThuoc == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã thuốc không hợp lệ!", "INVALID_DRUG_ID"));
            return;
        }

        try {
            Map<String, Object> result = duocSiDAO.xoaHoacKhoaThuoc(role, maThuoc);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(result, (String) result.get("message")));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi thao tác xóa/ngừng kinh doanh thuốc!");
        }
    }
}
