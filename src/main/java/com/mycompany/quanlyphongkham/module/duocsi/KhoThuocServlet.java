package com.mycompany.quanlyphongkham.module.duocsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.Thuoc;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "KhoThuocServlet", urlPatterns = {"/api/duoc-si/kho"})
public class KhoThuocServlet extends BaseServlet {

    private final DuocSiDAO duocSiDAO = new DuocSiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        int page = getIntParameter(req, "page", 1);
        int size = getIntParameter(req, "size", 10);

        try {
            PageResponse<Thuoc> pageResponse = duocSiDAO.getToanBoKhoThuoc(role, page, size);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(pageResponse, "Lấy danh mục kho thuốc thành công (Trang " + pageResponse.getPage() + "/" + pageResponse.getTotalPages() + ")."));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi lấy danh mục kho thuốc!");
        }
    }
}
