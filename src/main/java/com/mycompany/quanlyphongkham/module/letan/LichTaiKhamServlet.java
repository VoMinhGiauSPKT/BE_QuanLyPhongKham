package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "LichTaiKhamServlet", urlPatterns = {"/api/le-tan/lich-tai-kham"})
public class LichTaiKhamServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String patientParam = req.getParameter("maBenhNhan");
        if (patientParam == null) {
            patientParam = req.getParameter("patientId");
        }

        if (patientParam == null || patientParam.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã bệnh nhân!", "MISSING_PATIENT_ID"));
            return;
        }

        String patientId = FormatUtil.normalizeBenhNhan(patientParam);
        if (patientId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã bệnh nhân không hợp lệ!", "INVALID_PATIENT_ID"));
            return;
        }

        try {
            List<Map<String, Object>> list = leTanDAO.getLichTaiKhamCuaBenhNhan(role, patientId);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(list, "Lấy danh sách lịch hẹn tái khám thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi tra cứu lịch hẹn tái khám!");
        }
    }
}
