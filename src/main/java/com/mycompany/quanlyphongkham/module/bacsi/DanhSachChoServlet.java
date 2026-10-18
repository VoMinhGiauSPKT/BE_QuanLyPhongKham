package com.mycompany.quanlyphongkham.module.bacsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.entity.LichHen;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "DanhSachChoServlet", urlPatterns = {"/api/bac-si/danh-sach-cho"})
public class DanhSachChoServlet extends BaseServlet {

    private final BacSiDAO bacSiDAO = new BacSiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String userId = getCurrentUserId(req);

        // Allow doctor ID override via query param ?doctorId=BS001 if provided (e.g. for testing/admin)
        String paramDoctorId = req.getParameter("doctorId");
        if (paramDoctorId != null && !paramDoctorId.trim().isEmpty()) {
            userId = FormatUtil.normalizeBacSi(paramDoctorId);
        }

        try {
            List<LichHen> danhSach = bacSiDAO.getDanhSachChoCuaBacSi(role, userId);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(danhSach, "Lấy danh sách bệnh nhân chờ khám thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy danh sách bệnh nhân chờ khám!");
        }
    }
}
