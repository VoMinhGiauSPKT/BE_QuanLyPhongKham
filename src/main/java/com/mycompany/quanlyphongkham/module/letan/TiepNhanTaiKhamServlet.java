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

@WebServlet(name = "TiepNhanTaiKhamServlet", urlPatterns = {"/api/le-tan/tiep-nhan-tai-kham"})
public class TiepNhanTaiKhamServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    public static class CheckinRequest {
        public String maLichHen;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        CheckinRequest body = readJsonBody(req, CheckinRequest.class);

        if (body == null || body.maLichHen == null || body.maLichHen.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã lịch hẹn cần tiếp nhận!", "MISSING_APPOINTMENT_ID"));
            return;
        }

        String lichHenId = FormatUtil.normalizeLichHen(body.maLichHen);
        if (lichHenId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã lịch hẹn không hợp lệ!", "INVALID_APPOINTMENT_ID"));
            return;
        }

        try {
            Map<String, Object> result = leTanDAO.tiepNhanTaiKham(role, lichHenId);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(result, "Tiếp nhận bệnh nhân tái khám thành công! Số thứ tự hôm nay: #" + result.get("soThuTu")));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi tiếp nhận tái khám: " + e.getMessage());
        }
    }
}
