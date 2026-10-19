package com.mycompany.quanlyphongkham.module.duocsi;

import com.google.gson.Gson;
import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "NhapNhieuThuocServlet", urlPatterns = {"/api/duoc-si/nhap-nhieu-thuoc"})
public class NhapNhieuThuocServlet extends BaseServlet {

    private final DuocSiDAO duocSiDAO = new DuocSiDAO();
    private final Gson gson = new Gson();

    public static class DrugBatchItem {
        public String maThuoc;
        public Object soLuong; // Cho phép cả số nguyên, số âm, hoặc chuỗi chữ để CSDL kiểm thử Transaction
    }

    public static class NhapNhieuThuocRequest {
        public List<DrugBatchItem> danhSachThuoc;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        NhapNhieuThuocRequest body = readJsonBody(req, NhapNhieuThuocRequest.class);

        if (body == null || body.danhSachThuoc == null || body.danhSachThuoc.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Danh sách thuốc nhập vào không được để trống!", "EMPTY_LIST"));
            return;
        }

        // Chuyển toàn bộ danh sách thuốc thành chuỗi JSON để truyền vào CSDL
        String jsonPayload = gson.toJson(body.danhSachThuoc);

        try {
            // Thực thi Stored Procedure sp_gd_nhap_nhieu_thuoc (Transaction 6)
            duocSiDAO.nhapNhieuThuoc(role, jsonPayload);

            Map<String, Object> data = new HashMap<>();
            data.put("soLuongLoaiThuoc", body.danhSachThuoc.size());
            data.put("status", "SUCCESS");

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Giao dịch nhập kho nhiều loại thuốc thành công 100%! CSDL đã cập nhật số lượng tồn kho."));

        } catch (SQLException e) {
            // Khi có lỗi từ CSDL (số âm, chữ, vi phạm ràng buộc), CSDL đã ROLLBACK toàn bộ
            handleSqlException(resp, e, "Giao dịch nhập nhiều thuốc thất bại (CSDL đã Rollback toàn bộ dữ liệu)!");
        }
    }
}
