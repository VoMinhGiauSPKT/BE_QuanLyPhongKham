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

@WebServlet(name = "QuanLyHoaDonServlet", urlPatterns = {"/api/admin/hoa-don"})
public class QuanLyHoaDonServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        if (role == null || role.trim().isEmpty()) {
            role = "QUAN_TRI";
        }

        String maHoaDon = req.getParameter("maHoaDon");

        try {
            if (maHoaDon != null && !maHoaDon.trim().isEmpty()) {
                Map<String, Object> detail = adminDAO.getChiTietHoaDon(role, maHoaDon.trim());
                if (detail == null || detail.isEmpty()) {
                    sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            ApiResponse.error("Không tìm thấy thông tin hóa đơn có mã: " + maHoaDon, "INVOICE_NOT_FOUND"));
                    return;
                }
                sendJsonResponse(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok(detail, "Lấy chi tiết hóa đơn viện phí thành công!"));
            } else {
                String keyword = req.getParameter("keyword");
                if (keyword == null) {
                    keyword = req.getParameter("q");
                }
                List<Map<String, Object>> list = adminDAO.getDanhSachHoaDon(role, keyword);
                sendJsonResponse(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok(list, "Lấy danh sách hóa đơn viện phí thành công!"));
            }
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi truy xuất dữ liệu hóa đơn viện phí!");
        }
    }
}
