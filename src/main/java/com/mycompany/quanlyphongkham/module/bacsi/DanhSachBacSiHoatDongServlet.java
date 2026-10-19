package com.mycompany.quanlyphongkham.module.bacsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.module.admin.AdminDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DanhSachBacSiHoatDongServlet", urlPatterns = {"/api/bac-si/danh-sach-hoat-dong"})
public class DanhSachBacSiHoatDongServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        try {
            List<Map<String, Object>> list = adminDAO.getDanhSachBacSiHoatDong(role);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.ok(list));
        } catch (SQLException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("Lỗi khi tải danh sách bác sĩ hoạt động: " + e.getMessage(), "DB_ERROR"));
        }
    }
}
