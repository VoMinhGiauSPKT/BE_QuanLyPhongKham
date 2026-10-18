package com.mycompany.quanlyphongkham.test;

import com.mycompany.quanlyphongkham.util.DBContext;
import java.sql.*;

public class CheckLichHen {
    public static void main(String[] args) {
        String sql = "SELECT lh.ma_lich_hen, lh.ma_benh_nhan, lh.ma_bac_si, lh.thoi_gian_hen, lh.so_thu_tu, lh.trang_thai, " +
                     "CAST(lh.thoi_gian_hen AS DATE) as ngay_hen, CAST(GETDATE() AS DATE) as ngay_hien_tai " +
                     "FROM lich_hen lh ORDER BY lh.thoi_gian_hen DESC";
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI");
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("=== DANH SACH LICH HEN HIEN TAI ===");
            while (rs.next()) {
                System.out.printf("Mã: %s | BN: %s | BS: %s | TG: %s | STT: %d | Trạng thái: %s | Ngày hẹn: %s | Hôm nay: %s%n",
                        rs.getString("ma_lich_hen"),
                        rs.getString("ma_benh_nhan"),
                        rs.getString("ma_bac_si"),
                        rs.getTimestamp("thoi_gian_hen"),
                        rs.getInt("so_thu_tu"),
                        rs.getString("trang_thai"),
                        rs.getDate("ngay_hen"),
                        rs.getDate("ngay_hien_tai")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
