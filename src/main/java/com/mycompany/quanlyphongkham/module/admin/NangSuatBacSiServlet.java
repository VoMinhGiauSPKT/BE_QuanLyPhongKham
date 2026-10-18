package com.mycompany.quanlyphongkham.module.admin;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "NangSuatBacSiServlet", urlPatterns = {"/api/admin/nang-suat-bac-si"})
public class NangSuatBacSiServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        try {
            List<Map<String, Object>> list = adminDAO.getNangSuatBacSi(role);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(list, "Lấy thống kê năng suất và doanh số bác sĩ thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy báo cáo năng suất bác sĩ!");
        }
    }
}
