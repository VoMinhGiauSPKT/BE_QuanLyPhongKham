package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "TiepNhanServlet", urlPatterns = {"/api/le-tan/tiep-nhan"})
public class TiepNhanServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    public static class TiepNhanRequest {
        public String hoTen;
        public String ngaySinh;    // yyyy-MM-dd
        public String gioiTinh;    // Nam / Nữ / Khác
        public String soDienThoai;
        public String maBacSi;     // BS001 hoặc 1
        public String thoiGianHen; // Optional, yyyy-MM-dd HH:mm:ss
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        TiepNhanRequest body = readJsonBody(req, TiepNhanRequest.class);

        if (body == null || body.hoTen == null || body.soDienThoai == null || body.maBacSi == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng điền đủ thông tin: họ tên, SĐT và mã bác sĩ (BS001)!", "MISSING_FIELDS"));
            return;
        }

        String bacSiId = FormatUtil.normalizeBacSi(body.maBacSi);
        if (bacSiId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã bác sĩ không hợp lệ (Ví dụ: BS001)!", "INVALID_DOCTOR_ID"));
            return;
        }

        try {
            Date ns = (body.ngaySinh != null && !body.ngaySinh.isEmpty()) 
                    ? Date.valueOf(body.ngaySinh) 
                    : Date.valueOf("1990-01-01");
            
            Timestamp tgHen = (body.thoiGianHen != null && !body.thoiGianHen.isEmpty())
                    ? Timestamp.valueOf(body.thoiGianHen)
                    : new Timestamp(System.currentTimeMillis());

            String maLichHen = leTanDAO.tiepNhanKham(
                    role,
                    body.hoTen.trim(),
                    ns,
                    body.gioiTinh != null ? body.gioiTinh : "Khác",
                    body.soDienThoai.trim(),
                    bacSiId,
                    tgHen
            );

            Map<String, Object> data = new HashMap<>();
            data.put("maLichHen", maLichHen);
            data.put("maLichHenCode", maLichHen);
            data.put("maBacSiCode", bacSiId);

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Tiếp nhận bệnh nhân thành công!"));

        } catch (IllegalArgumentException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Định dạng ngày sinh hoặc thời gian hẹn không hợp lệ!", "INVALID_FORMAT"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi cơ sở dữ liệu khi tiếp nhận bệnh nhân!");
        }
    }
}
