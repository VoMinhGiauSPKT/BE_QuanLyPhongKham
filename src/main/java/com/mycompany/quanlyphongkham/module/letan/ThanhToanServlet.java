package com.mycompany.quanlyphongkham.module.letan;

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

@WebServlet(name = "ThanhToanServlet", urlPatterns = {"/api/le-tan/thanh-toan"})
public class ThanhToanServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    public static class ThanhToanRequest {
        public String maHoSo;              // HS001 hoặc 1
        public BigDecimal tienGiamGia;
        public String hinhThucThanhToan;   // Tiền mặt / Chuyển khoản / Thẻ
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        ThanhToanRequest body = readJsonBody(req, ThanhToanRequest.class);

        if (body == null || body.maHoSo == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã hồ sơ (Ví dụ: HS001)!", "MISSING_RECORD_ID"));
            return;
        }

        String hoSoId = FormatUtil.normalizeHoSo(body.maHoSo);
        if (hoSoId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã hồ sơ không hợp lệ!", "INVALID_RECORD_ID"));
            return;
        }

        try {
            leTanDAO.thanhToanHoaDon(
                    role,
                    hoSoId,
                    body.tienGiamGia != null ? body.tienGiamGia : BigDecimal.ZERO,
                    body.hinhThucThanhToan != null ? body.hinhThucThanhToan : "Tiền mặt"
            );

            Map<String, Object> data = new HashMap<>();
            data.put("maHoSo", hoSoId);
            data.put("maHoSoCode", hoSoId);
            data.put("trangThai", "Đã thanh toán");

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Thanh toán viện phí và chốt hóa đơn thành công!"));

        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi thanh toán hóa đơn!");
        }
    }
}
