package com.mycompany.quanlyphongkham.module.bacsi;

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

@WebServlet(name = "LichSuServlet", urlPatterns = {"/api/bac-si/lich-su"})
public class LichSuServlet extends BaseServlet {

    private final BacSiDAO bacSiDAO = new BacSiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String patientParam = req.getParameter("patientId");
        if (patientParam == null) {
            patientParam = req.getParameter("maBenhNhan");
        }

        if (patientParam == null || patientParam.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã bệnh nhân (Ví dụ: patientId=BN001)!", "MISSING_PATIENT_ID"));
            return;
        }

        String patientId = FormatUtil.normalizeBenhNhan(patientParam);
        if (patientId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã bệnh nhân không hợp lệ!", "INVALID_PATIENT_ID"));
            return;
        }

        try {
            List<Map<String, Object>> lichSu = bacSiDAO.getLichSuKhamBenh(role, patientId);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(lichSu, "Lấy tiền sử bệnh án của bệnh nhân " + patientId + " thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy tiền sử bệnh án!");
        }
    }
}
