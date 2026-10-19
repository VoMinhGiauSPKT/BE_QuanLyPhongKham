package com.mycompany.quanlyphongkham.module.letan;

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

@WebServlet(name = "HuyDonThuocServlet", urlPatterns = {"/api/le-tan/huy-don-thuoc"})
public class HuyDonThuocServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    public static class HuyDonThuocRequest {
        public String maHoSo; // HS001 hoặc 1
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        HuyDonThuocRequest body = readJsonBody(req, HuyDonThuocRequest.class);

        if (body == null || body.maHoSo == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã hồ sơ khám bệnh!", "MISSING_RECORD_ID"));
            return;
        }

        String hoSoId = FormatUtil.normalizeHoSo(body.maHoSo);
        if (hoSoId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã hồ sơ không hợp lệ!", "INVALID_RECORD_ID"));
            return;
        }

        try {
            leTanDAO.huyDonThuoc(role, hoSoId);

            Map<String, Object> data = new HashMap<>();
            data.put("maHoSo", hoSoId);
            data.put("maHoSoCode", hoSoId);

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Hủy đơn thuốc thành công! Toàn bộ số lượng đã được CSDL (Trigger trg_xoa_don_hoan_kho) tự động cộng hoàn trả lại tồn kho."));

        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi hủy đơn thuốc!");
        }
    }
}
