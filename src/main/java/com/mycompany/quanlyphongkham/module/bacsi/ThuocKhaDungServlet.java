package com.mycompany.quanlyphongkham.module.bacsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.Thuoc;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ThuocKhaDungServlet", urlPatterns = {"/api/bac-si/thuoc-kha-dung"})
public class ThuocKhaDungServlet extends BaseServlet {

    private final BacSiDAO bacSiDAO = new BacSiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String keyword = req.getParameter("q");
        if (keyword == null) {
            keyword = req.getParameter("keyword");
        }
        if (keyword == null) {
            keyword = "";
        }

        int page = getIntParameter(req, "page", 1);
        int size = getIntParameter(req, "size", 10);

        try {
            PageResponse<Thuoc> pageResponse = bacSiDAO.timThuocKhaDung(role, keyword.trim(), page, size);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(pageResponse, "Tìm thấy " + pageResponse.getTotalElements() + " loại thuốc khả dụng (Trang " + pageResponse.getPage() + "/" + pageResponse.getTotalPages() + ")."));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi tra cứu thuốc khả dụng!");
        }
    }
}
