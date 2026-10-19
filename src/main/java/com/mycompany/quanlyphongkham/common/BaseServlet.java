package com.mycompany.quanlyphongkham.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

public abstract class BaseServlet extends HttpServlet {

    protected static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    protected <T> T readJsonBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(request.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        if (body.trim().isEmpty()) {
            return null;
        }
        return gson.fromJson(body, clazz);
    }

    protected void sendJsonResponse(HttpServletResponse response, int status, ApiResponse<?> apiResponse) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        byte[] bytes = gson.toJson(apiResponse).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    protected void handleSqlException(HttpServletResponse response, SQLException ex, String defaultMsg) throws IOException {
        // SQL Server error 229 is "The %ls permission was denied on the object '%ls', database '%ls', schema '%ls'."
        // SQL Server error 230 is "The %ls permission was denied on the column '%ls' of the object '%ls'..."
        if (ex.getErrorCode() == 229 || ex.getErrorCode() == 230 || 
            (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("permission was denied"))) {
            sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.forbiddenDb("Lỗi phân quyền DBMS: " + ex.getMessage()));
            return;
        }

        String rawMsg = (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) ? ex.getMessage() : defaultMsg;
        String userFriendlyMsg = rawMsg;

        if (rawMsg.contains("CK_thuoc_so_luong_ton")) {
            userFriendlyMsg = "CSDL báo lỗi vi phạm Ràng buộc toàn vẹn [CK_thuoc_so_luong_ton]: Số lượng tồn kho không được phép nhỏ hơn 0!";
        } else if (rawMsg.contains("CK_thuoc_don_gia")) {
            userFriendlyMsg = "CSDL báo lỗi vi phạm Ràng buộc toàn vẹn [CK_thuoc_don_gia]: Đơn giá thuốc phải lớn hơn 0!";
        } else if (rawMsg.contains("CK_dich_vu_don_gia")) {
            userFriendlyMsg = "CSDL báo lỗi vi phạm Ràng buộc toàn vẹn [CK_dich_vu_don_gia]: Đơn giá dịch vụ kỹ thuật phải lớn hơn 0!";
        } else if (rawMsg.contains("CK_benh_nhan_gioi_tinh")) {
            userFriendlyMsg = "CSDL báo lỗi vi phạm Ràng buộc toàn vẹn [CK_benh_nhan_gioi_tinh]: Giới tính bệnh nhân bắt buộc phải thuộc ('Nam', 'Nữ', 'Khác')!";
        } else if (rawMsg.contains("CK_hoa_don_tong_tien")) {
            userFriendlyMsg = "CSDL báo lỗi vi phạm Ràng buộc toàn vẹn [CK_hoa_don_tong_tien]: Tổng tiền hóa đơn phải khớp (Dịch vụ + Thuốc - Giảm giá >= 0)!";
        } else if (rawMsg.contains("UQ_lich_hen_bacsi_ngay_sothutu")) {
            userFriendlyMsg = "CSDL báo lỗi vi phạm Ràng buộc duy nhất [UQ_lich_hen_bacsi_ngay_sothutu]: Trùng số thứ tự khám của bác sĩ trong ngày!";
        }

        sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                ApiResponse.error(userFriendlyMsg, "DB_ERROR"));
    }

    protected String getCurrentUserRole(HttpServletRequest request) {
        Object role = request.getAttribute("USER_ROLE");
        return role != null ? role.toString() : null;
    }

    protected String getCurrentUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("USER_ID");
        return uid != null ? uid.toString() : null;
    }

    protected String getCurrentUsername(HttpServletRequest request) {
        Object username = request.getAttribute("USER_NAME");
        return username != null ? username.toString() : null;
    }

    protected int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(val.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
