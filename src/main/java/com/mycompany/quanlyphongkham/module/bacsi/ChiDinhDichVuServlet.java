package com.mycompany.quanlyphongkham.module.bacsi;

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

@WebServlet(name = "ChiDinhDichVuServlet", urlPatterns = {"/api/bac-si/chi-dinh-dich-vu"})
public class ChiDinhDichVuServlet extends BaseServlet {

    private final BacSiDAO bacSiDAO = new BacSiDAO();

    public static class ChiDinhRequest {
        public String maHoSo;   // HS001 hoặc 1
        public String maDichVu; // DV001 hoặc 1
        public Integer soLuong;
        public BigDecimal donGia;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        ChiDinhRequest body = readJsonBody(req, ChiDinhRequest.class);

        if (body == null || body.maHoSo == null || body.maDichVu == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã hồ sơ (HS001) và mã dịch vụ (DV001)!", "MISSING_FIELDS"));
            return;
        }

        String hoSoId = FormatUtil.normalizeHoSo(body.maHoSo);
        String dichVuId = FormatUtil.normalizeDichVu(body.maDichVu);

        if (hoSoId == null || dichVuId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã hồ sơ hoặc mã dịch vụ không hợp lệ!", "INVALID_ID_FORMAT"));
            return;
        }

        try {
            String maChiDinh = bacSiDAO.themChiDinhDichVu(
                    role,
                    hoSoId,
                    dichVuId,
                    body.soLuong != null ? body.soLuong : 1,
                    body.donGia
            );

            Map<String, Object> data = new HashMap<>();
            data.put("maChiDinh", maChiDinh);
            data.put("maChiDinhCode", maChiDinh);
            data.put("maHoSoCode", hoSoId);
            data.put("maDichVuCode", dichVuId);

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Gán chỉ định dịch vụ cận lâm sàng thành công!"));

        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi gán chỉ định dịch vụ!");
        }
    }
}
