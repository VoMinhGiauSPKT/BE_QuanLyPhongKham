package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.entity.LichHen;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "HangChoServlet", urlPatterns = {"/api/le-tan/hang-cho"})
public class HangChoServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        try {
            List<LichHen> hangCho = leTanDAO.getHangChoKhamHomNay(role);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(hangCho, "Lấy danh sách hàng chờ khám hôm nay thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy hàng chờ khám hôm nay!");
        }
    }
}
