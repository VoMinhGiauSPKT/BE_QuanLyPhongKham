package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.BenhNhan;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "TimBenhNhanServlet", urlPatterns = {"/api/le-tan/tim-benh-nhan"})
public class TimBenhNhanServlet extends BaseServlet {

    private final LeTanDAO leTanDAO = new LeTanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String keyword = req.getParameter("keyword");
        if (keyword == null) {
            keyword = req.getParameter("q");
        }
        if (keyword == null) {
            keyword = "";
        }

        int page = getIntParameter(req, "page", 1);
        int size = getIntParameter(req, "size", 10);

        try {
            PageResponse<BenhNhan> pageResponse = leTanDAO.timKiemBenhNhan(role, keyword.trim(), page, size);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(pageResponse, "Tìm thấy " + pageResponse.getTotalElements() + " bệnh nhân phù hợp (Trang " + pageResponse.getPage() + "/" + pageResponse.getTotalPages() + ")."));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi tìm kiếm bệnh nhân!");
        }
    }
}
