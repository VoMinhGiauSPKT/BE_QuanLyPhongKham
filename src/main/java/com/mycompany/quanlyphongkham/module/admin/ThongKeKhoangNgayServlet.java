package com.mycompany.quanlyphongkham.module.admin;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ThongKeKhoangNgayServlet", urlPatterns = {"/api/admin/thong-ke-khoang-ngay"})
public class ThongKeKhoangNgayServlet extends BaseServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);

        String fromParam = req.getParameter("from");
        if (fromParam == null) {
            fromParam = req.getParameter("tuNgay");
        }

        String toParam = req.getParameter("to");
        if (toParam == null) {
            toParam = req.getParameter("denNgay");
        }

        Date fromDate;
        Date toDate;

        try {
            if (fromParam != null && !fromParam.trim().isEmpty()) {
                fromDate = Date.valueOf(fromParam.trim());
            } else {
                // Default: 30 days ago
                fromDate = Date.valueOf(LocalDate.now().minusDays(30));
            }

            if (toParam != null && !toParam.trim().isEmpty()) {
                toDate = Date.valueOf(toParam.trim());
            } else {
                toDate = Date.valueOf(LocalDate.now());
            }
        } catch (IllegalArgumentException e) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Định dạng ngày không hợp lệ! Vui lòng dùng định dạng yyyy-MM-dd.", "INVALID_DATE_FORMAT"));
            return;
        }

        try {
            List<Map<String, Object>> thongKe = adminDAO.getThongKeKhoangNgay(role, fromDate, toDate);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(thongKe, "Thống kê doanh thu từ ngày " + fromDate + " đến ngày " + toDate + " thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi thống kê doanh thu theo khoảng ngày!");
        }
    }
}
