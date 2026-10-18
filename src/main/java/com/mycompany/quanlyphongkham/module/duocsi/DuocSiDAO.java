package com.mycompany.quanlyphongkham.module.duocsi;

import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.Thuoc;
import com.mycompany.quanlyphongkham.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DuocSiDAO {

    /**
     * Xem toàn bộ danh mục kho thuốc
     */
    public List<Thuoc> getToanBoKhoThuoc(String role) throws SQLException {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT ma_thuoc, ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung, trang_thai " +
                     "FROM thuoc ORDER BY ma_thuoc ASC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("ma_thuoc"));
                t.setTenThuoc(rs.getString("ten_thuoc"));
                t.setDonViTinh(rs.getString("don_vi_tinh"));
                t.setSoLuongTon(rs.getInt("so_luong_ton"));
                t.setDonGia(rs.getBigDecimal("don_gia"));
                t.setHanSuDung(rs.getDate("han_su_dung"));
                t.setTrangThai(rs.getBoolean("trang_thai"));
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Xem toàn bộ danh mục kho thuốc có phân trang (OFFSET ... FETCH NEXT)
     */
    public PageResponse<Thuoc> getToanBoKhoThuoc(String role, int page, int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        int offset = (page - 1) * size;

        List<Thuoc> list = new ArrayList<>();
        long totalElements = 0;

        String countSql = "SELECT COUNT(*) FROM thuoc";
        String dataSql = "SELECT ma_thuoc, ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung, trang_thai " +
                         "FROM thuoc ORDER BY ma_thuoc ASC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            try (PreparedStatement psCount = conn.prepareStatement(countSql);
                 ResultSet rs = psCount.executeQuery()) {
                if (rs.next()) {
                    totalElements = rs.getLong(1);
                }
            }

            if (totalElements > 0) {
                try (PreparedStatement psData = conn.prepareStatement(dataSql)) {
                    psData.setInt(1, offset);
                    psData.setInt(2, size);
                    try (ResultSet rs = psData.executeQuery()) {
                        while (rs.next()) {
                            Thuoc t = new Thuoc();
                            t.setMaThuoc(rs.getString("ma_thuoc"));
                            t.setTenThuoc(rs.getString("ten_thuoc"));
                            t.setDonViTinh(rs.getString("don_vi_tinh"));
                            t.setSoLuongTon(rs.getInt("so_luong_ton"));
                            t.setDonGia(rs.getBigDecimal("don_gia"));
                            t.setHanSuDung(rs.getDate("han_su_dung"));
                            t.setTrangThai(rs.getBoolean("trang_thai"));
                            list.add(t);
                        }
                    }
                }
            }
        }
        return new PageResponse<>(list, page, size, totalElements);
    }

    /**
     * Lấy danh sách thuốc sắp hết date hoặc tồn kho thấp từ View v_canh_bao_ton_kho_thuoc
     */
    public List<Thuoc> getCanhBaoTonKho(String role) throws SQLException {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT ma_thuoc, ten_thuoc, so_luong_ton, don_vi_tinh, han_su_dung, " +
                     "so_ngay_con_han, trang_thai_canh_bao FROM v_canh_bao_ton_kho_thuoc ORDER BY so_luong_ton ASC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("ma_thuoc"));
                t.setTenThuoc(rs.getString("ten_thuoc"));
                int ton = rs.getInt("so_luong_ton");
                t.setSoLuongTon(ton);
                t.setDonViTinh(rs.getString("don_vi_tinh"));
                t.setHanSuDung(rs.getDate("han_su_dung"));
                int days = rs.getInt("so_ngay_con_han");
                t.setSoNgayConHan(days);

                // Compute clean UTF-8 alert status from raw numeric indicators to guarantee encoding safety
                String status;
                if (ton < 20 && days < 30) {
                    status = "Tồn ít & Sắp hết hạn";
                } else if (ton < 20) {
                    status = "Sắp hết hàng";
                } else if (days < 30) {
                    status = "Sắp hết hạn sử dụng";
                } else {
                    status = "Bình thường";
                }
                t.setTrangThaiCanhBao(status);
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Cập nhật thêm số lượng tồn kho cho thuốc
     */
    public int nhapKhoThuoc(String role, String maThuoc, int soLuongNhap) throws SQLException {
        String sql = "DECLARE @out TABLE (ton INT); " +
                     "UPDATE thuoc SET so_luong_ton = so_luong_ton + ? " +
                     "OUTPUT INSERTED.so_luong_ton INTO @out " +
                     "WHERE ma_thuoc = ?; " +
                     "SELECT ton FROM @out;";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongNhap);
            ps.setString(2, maThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }
}
