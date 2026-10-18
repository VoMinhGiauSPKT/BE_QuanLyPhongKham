package com.mycompany.quanlyphongkham.module.duocsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "NhapKhoServlet", urlPatterns = {"/api/duoc-si/nhap-kho"})
public class NhapKhoServlet extends BaseServlet {

    private final DuocSiDAO duocSiDAO = new DuocSiDAO();

    public static class NhapKhoRequest {
        public String maThuoc; // T001 hoặc 1
        public Integer soLuongNhap;
        public Integer soLuong; // Fallback field
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        NhapKhoRequest body = readJsonBody(req, NhapKhoRequest.class);

        if (body == null || body.maThuoc == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã thuốc (T001) và số lượng cần nhập!", "MISSING_FIELDS"));
            return;
        }

        String thuocId = FormatUtil.normalizeThuoc(body.maThuoc);
        if (thuocId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã thuốc không hợp lệ!", "INVALID_DRUG_ID"));
            return;
        }

        int slNhap = (body.soLuongNhap != null) ? body.soLuongNhap : (body.soLuong != null ? body.soLuong : 0);
        if (slNhap <= 0) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Số lượng nhập kho phải lớn hơn 0!", "INVALID_QUANTITY"));
            return;
        }

        try {
            int tonMoi = duocSiDAO.nhapKhoThuoc(role, thuocId, slNhap);

            if (tonMoi < 0) {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        ApiResponse.error("Không tìm thấy thuốc có mã " + thuocId, "NOT_FOUND"));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("maThuoc", thuocId);
            data.put("maThuocCode", thuocId);
            data.put("soLuongNhapThem", slNhap);
            data.put("soLuongTonHienTai", tonMoi);

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Nhập thêm kho thuốc thành công!"));

        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi nhập kho thuốc!");
        }
    }
}
