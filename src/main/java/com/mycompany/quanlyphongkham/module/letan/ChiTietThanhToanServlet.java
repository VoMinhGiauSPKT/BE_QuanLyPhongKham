package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "ChiTietThanhToanServlet", urlPatterns = {"/api/le-tan/chi-tiet-thanh-toan"})
public class ChiTietThanhToanServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String maHoSoParam = req.getParameter("maHoSo");
        if (maHoSoParam == null || maHoSoParam.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã hồ sơ (Ví dụ: maHoSo=HS001)!", "MISSING_RECORD_ID"));
            return;
        }

        String hoSoId = FormatUtil.normalizeHoSo(maHoSoParam);
        if (hoSoId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã hồ sơ không hợp lệ!", "INVALID_RECORD_ID"));
            return;
        }

        try {
            Map<String, Object> chiTiet = leTanDAO.getChiTietThanhToan(role, hoSoId);
            if (chiTiet == null) {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        ApiResponse.error("Không tìm thấy hồ sơ khám bệnh " + hoSoId, "RECORD_NOT_FOUND"));
                return;
            }
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(chiTiet, "Lấy chi tiết bảng kê viện phí thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy chi tiết bảng kê viện phí: " + e.getMessage());
        }
    }
}
