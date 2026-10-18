package com.mycompany.quanlyphongkham.module.duocsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.entity.Thuoc;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "CanhBaoThuocServlet", urlPatterns = {"/api/duoc-si/canh-bao"})
public class CanhBaoThuocServlet extends BaseServlet {

    private final DuocSiDAO duocSiDAO = new DuocSiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        try {
            List<Thuoc> canhBaoList = duocSiDAO.getCanhBaoTonKho(role);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(canhBaoList, "Lấy danh sách cảnh báo tồn kho và cận hạn sử dụng thành công!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy cảnh báo kho dược!");
        }
    }
}
