package com.mycompany.quanlyphongkham.module.auth;

import com.mycompany.quanlyphongkham.entity.NguoiDung;
import com.mycompany.quanlyphongkham.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDAO {

    public NguoiDung findByUsername(String username) throws SQLException {
        String sql = "SELECT ma_nguoi_dung, ten_dang_nhap, mat_khau_hash, ho_ten, so_dien_thoai, vai_tro, hoat_dong, ngay_tao " +
                     "FROM nguoi_dung WHERE ten_dang_nhap = ?";
        try (Connection conn = DBContext.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung user = new NguoiDung();
                    user.setMaNguoiDung(rs.getString("ma_nguoi_dung"));
                    user.setTenDangNhap(rs.getString("ten_dang_nhap"));
                    user.setMatKhauHash(rs.getString("mat_khau_hash"));
                    user.setHoTen(rs.getString("ho_ten"));
                    user.setSoDienThoai(rs.getString("so_dien_thoai"));
                    user.setVaiTro(rs.getString("vai_tro"));
                    user.setHoatDong(rs.getBoolean("hoat_dong"));
                    user.setNgayTao(rs.getTimestamp("ngay_tao"));
                    return user;
                }
            }
        }
        return null;
    }
}
