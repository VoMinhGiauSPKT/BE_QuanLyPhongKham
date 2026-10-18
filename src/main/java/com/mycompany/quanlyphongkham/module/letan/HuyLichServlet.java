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

@WebServlet(name = "HuyLichServlet", urlPatterns = {"/api/le-tan/huy-lich"})
public class HuyLichServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    public static class HuyLichRequest {
        public String maLichHen; // LH001 hoặc 1
        public String lyDo;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        HuyLichRequest body = readJsonBody(req, HuyLichRequest.class);

        if (body == null || body.maLichHen == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã lịch hẹn (Ví dụ: LH001)!", "MISSING_APPOINTMENT_ID"));
            return;
        }

        String lichHenId = FormatUtil.normalizeLichHen(body.maLichHen);
        if (lichHenId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã lịch hẹn không hợp lệ!", "INVALID_APPOINTMENT_ID"));
            return;
        }

        try {
            leTanDAO.huyLichHen(role, lichHenId, body.lyDo);

            Map<String, Object> data = new HashMap<>();
            data.put("maLichHen", lichHenId);
            data.put("maLichHenCode", lichHenId);
            data.put("trangThai", "Đã hủy");

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Hủy lịch hẹn thành công!"));

        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi hủy lịch hẹn!");
        }
    }
}
