package com.mycompany.quanlyphongkham.module.admin;

import com.mycompany.quanlyphongkham.util.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDAO {

    /**
     * Báo cáo tổng hợp doanh thu theo từng ngày từ View v_doanh_thu_theo_ngay
     */
    public List<Map<String, Object>> getDoanhThuTheoNgay(String role) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT ngay_thanh_toan, so_luong_hoa_don, tong_tien_dich_vu, " +
                     "tong_tien_thuoc, tong_tien_giam_gia, thuc_thu " +
                     "FROM v_doanh_thu_theo_ngay ORDER BY ngay_thanh_toan DESC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("ngayThanhToan", rs.getDate("ngay_thanh_toan"));
                map.put("soLuongHoaDon", rs.getInt("so_luong_hoa_don"));
                map.put("tongTienDichVu", rs.getBigDecimal("tong_tien_dich_vu"));
                map.put("tongTienThuoc", rs.getBigDecimal("tong_tien_thuoc"));
                map.put("tongTienGiamGia", rs.getBigDecimal("tong_tien_giam_gia"));
                map.put("thucThu", rs.getBigDecimal("thuc_thu"));
                list.add(map);
            }
        }
        return list;
    }

    /**
     * Báo cáo số ca khám và doanh số từng bác sĩ từ View v_doanh_thu_theo_bac_si
     */
    public List<Map<String, Object>> getNangSuatBacSi(String role) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT ma_bac_si, ten_bac_si, chuyen_khoa, so_ca_kham, tong_doanh_thu " +
                     "FROM v_doanh_thu_theo_bac_si ORDER BY tong_doanh_thu DESC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                String maBacSi = rs.getString("ma_bac_si");
                map.put("maBacSi", maBacSi);
                map.put("maBacSiCode", maBacSi);
                map.put("tenBacSi", rs.getString("ten_bac_si"));
                map.put("chuyenKhoa", rs.getString("chuyen_khoa"));
                map.put("soCaKham", rs.getInt("so_ca_kham"));
                map.put("tongDoanhThu", rs.getBigDecimal("tong_doanh_thu"));
                list.add(map);
            }
        }
        return list;
    }

    /**
     * Báo cáo doanh thu theo khoảng ngày qua Stored Procedure sp_thong_ke_doanh_thu_khoang_ngay
     */
    public List<Map<String, Object>> getThongKeKhoangNgay(String role, Date tuNgay, Date denNgay) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String call = "{call sp_thong_ke_doanh_thu_khoang_ngay(?, ?)}";

        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setDate(1, tuNgay);
            cs.setDate(2, denNgay);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ngayGiaoDich", rs.getDate("ngay_giao_dich"));
                    map.put("hinhThucThanhToan", rs.getString("hinh_thuc_thanh_toan"));
                    map.put("soHoaDon", rs.getInt("so_hoa_don"));
                    map.put("doanhThuDichVu", rs.getBigDecimal("doanh_thu_dich_vu"));
                    map.put("doanhThuThuoc", rs.getBigDecimal("doanh_thu_thuoc"));
                    map.put("tongGiamGia", rs.getBigDecimal("tong_giam_gia"));
                    map.put("thucThu", rs.getBigDecimal("thuc_thu"));
                    list.add(map);
                }
            }
        }
        return list;
    }
}
