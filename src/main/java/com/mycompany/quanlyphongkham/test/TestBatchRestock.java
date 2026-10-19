package com.mycompany.quanlyphongkham.test;

import com.mycompany.quanlyphongkham.util.DBContext;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestBatchRestock {
    public static void main(String[] args) {
        System.out.println("=== KIEM TRA TRANSACTION sp_gd_nhap_nhieu_thuoc ===");

        try (Connection conn = DBContext.getConnectionByRole("DUOC_SI")) {
            // Lấy tồn kho trước test
            String maThuoc1 = "T001";
            int tonTruoc = getStock(conn, maThuoc1);
            System.out.println("Tồn kho hiện tại của " + maThuoc1 + ": " + tonTruoc);

            // Test 1: Gửi 1 thuốc hợp lệ và 1 thuốc số âm ("-99")
            String jsonErrorNegative = "[{\"maThuoc\":\"T001\",\"soLuong\":\"10\"},{\"maThuoc\":\"T002\",\"soLuong\":\"-99\"}]";
            System.out.println("\n--- Thử nghiệm 1: Gửi số âm (-99) ---");
            try (CallableStatement cs = conn.prepareCall("{call dbo.sp_gd_nhap_nhieu_thuoc(?)}")) {
                cs.setNString(1, jsonErrorNegative);
                cs.execute();
                System.err.println("FAIL: Đáng lẽ phải báo lỗi rollback!");
            } catch (SQLException ex) {
                System.out.println("SUCCESS (Bắt được lỗi Rollback từ CSDL): " + ex.getMessage());
            }

            int tonSauLoi1 = getStock(conn, maThuoc1);
            System.out.println("Tồn kho " + maThuoc1 + " sau khi Rollback: " + tonSauLoi1 + " (Phải bằng " + tonTruoc + ")");

            // Test 2: Gửi 1 thuốc hợp lệ và 1 thuốc chữ ("abc")
            String jsonErrorText = "[{\"maThuoc\":\"T001\",\"soLuong\":\"10\"},{\"maThuoc\":\"T002\",\"soLuong\":\"abc\"}]";
            System.out.println("\n--- Thử nghiệm 2: Gửi chữ ('abc') ---");
            try (CallableStatement cs = conn.prepareCall("{call dbo.sp_gd_nhap_nhieu_thuoc(?)}")) {
                cs.setNString(1, jsonErrorText);
                cs.execute();
                System.err.println("FAIL: Đáng lẽ phải báo lỗi rollback!");
            } catch (SQLException ex) {
                System.out.println("SUCCESS (Bắt được lỗi Rollback từ CSDL): " + ex.getMessage());
            }

            int tonSauLoi2 = getStock(conn, maThuoc1);
            System.out.println("Tồn kho " + maThuoc1 + " sau khi Rollback chữ: " + tonSauLoi2 + " (Phải bằng " + tonTruoc + ")");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getStock(Connection conn, String maThuoc) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT so_luong_ton FROM thuoc WHERE ma_thuoc = '" + maThuoc + "'")) {
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }
}
