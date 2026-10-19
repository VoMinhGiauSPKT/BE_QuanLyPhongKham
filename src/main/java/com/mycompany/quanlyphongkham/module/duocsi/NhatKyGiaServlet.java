package com.mycompany.quanlyphongkham.module.duocsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "NhatKyGiaServlet", urlPatterns = {"/api/duoc-si/nhat-ky-gia"})
public class NhatKyGiaServlet extends BaseServlet {

    private final DuocSiDAO duocSiDAO = new DuocSiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String paramId = req.getParameter("maThuoc");
        if (paramId == null || paramId.trim().isEmpty()) {
            paramId = req.getParameter("id");
        }

        if (paramId == null || paramId.trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã thuốc để tra cứu nhật ký giá!", "MISSING_DRUG_ID"));
            return;
        }

        String maThuoc = FormatUtil.normalizeThuoc(paramId);
        if (maThuoc == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã thuốc không hợp lệ!", "INVALID_DRUG_ID"));
            return;
        }

        try {
            List<Map<String, Object>> list = duocSiDAO.getNhatKyGiaThuoc(role, maThuoc);
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(list, "Lấy lịch sử biến động giá của thuốc " + maThuoc + " thành công (Ghi nhận bởi Trigger trg_ghi_nhat_ky_gia_thuoc)!"));
        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi tra cứu nhật ký giá thuốc!");
        }
    }
}
