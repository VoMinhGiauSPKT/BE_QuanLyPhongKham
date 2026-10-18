package com.mycompany.quanlyphongkham.middleware;

import com.mycompany.quanlyphongkham.util.DBContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Listener tự động chạy ngay khi Server khởi động (bấm Run source),
 * thực hiện kiểm tra kết nối CSDL và in kết quả ra Console trước khi nhận request.
 */
@WebListener
public class DatabaseCheckListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================================================");
        System.out.println("[KHỞI ĐỘNG HỆ THỐNG] Đang kiểm tra kết nối tới cơ sở dữ liệu...");
        try (Connection conn = DBContext.getAuthConnection()) {
            // DBContext.getAuthConnection() đã tự in:
            // [DBContext] >>> Kết nối với database thành công! [Tài khoản: tk_xac_thuc] <<<
        } catch (SQLException e) {
            System.err.println("[KHỞI ĐỘNG HỆ THỐNG] >>> KẾT NỐI VỚI DATABASE THẤT BẠI! <<<");
            System.err.println("[LÝ DO LỖI] " + e.getMessage());
            System.err.println("[HƯỚNG DẪN] Vui lòng kiểm tra lại SQL Server Service và mở TCP/IP cổng 1433.");
        }
        System.out.println("========================================================================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Dọn dẹp tài nguyên khi dừng ứng dụng nếu cần
    }
}
