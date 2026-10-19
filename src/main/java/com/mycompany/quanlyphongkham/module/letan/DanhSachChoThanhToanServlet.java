package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DanhSachChoThanhToanServlet", urlPatterns = {"/api/le-tan/cho-thanh-toan"})
public class DanhSachChoThanhToanServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String tuKhoa = req.getParameter("keyword");
        if (tuKhoa == null) {
            tuKhoa = req.getParameter("tuKhoa");
        }

        try {
            List<Map<String, Object>> list = leTanDAO.getDanhSachChoThanhToan(role, tuKhoa);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(list, "Lấy danh sách ca chờ thanh toán thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy danh sách ca chờ thanh toán: " + e.getMessage());
        }
    }
}
