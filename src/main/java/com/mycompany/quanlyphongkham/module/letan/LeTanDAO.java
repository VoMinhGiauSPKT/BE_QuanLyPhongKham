package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.BenhNhan;
import com.mycompany.quanlyphongkham.entity.LichHen;
import com.mycompany.quanlyphongkham.util.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeTanDAO {

    /**
     * Tiếp nhận ca khám bệnh mới qua Stored Procedure sp_tiep_nhan_kham
     */
    public String tiepNhanKham(String role, String hoTen, java.sql.Date ngaySinh, String gioiTinh,
                               String soDienThoai, String maBacSi, java.sql.Timestamp thoiGianHen) throws SQLException {
        String call = "{call sp_tiep_nhan_kham(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, hoTen);
            cs.setDate(2, ngaySinh);
            cs.setString(3, gioiTinh);
            cs.setString(4, soDienThoai);
            cs.setString(5, maBacSi);
            cs.setTimestamp(6, thoiGianHen);
            cs.registerOutParameter(7, Types.VARCHAR);

            cs.execute();
            return cs.getString(7);
        }
    }

    /**
     * Lấy hàng chờ khám trong ngày từ View v_hang_cho_kham_hom_nay
     */
    public List<LichHen> getHangChoKhamHomNay(String role) throws SQLException {
        List<LichHen> list = new ArrayList<>();
        String sql = "SELECT lh.ma_lich_hen, lh.so_thu_tu, bn.ho_ten AS ten_benh_nhan, bn.so_dien_thoai, " +
                     "bs_user.ho_ten AS ten_bac_si, bs.phong_kham, lh.thoi_gian_hen, lh.trang_thai " +
                     "FROM lich_hen lh " +
                     "JOIN benh_nhan bn ON lh.ma_benh_nhan = bn.ma_benh_nhan " +
                     "JOIN bac_si bs ON lh.ma_bac_si = bs.ma_bac_si " +
                     "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
                     "WHERE (CAST(lh.thoi_gian_hen AS DATE) >= CAST(DATEADD(day, -1, GETDATE()) AS DATE) " +
                     "    AND CAST(lh.thoi_gian_hen AS DATE) <= CAST(DATEADD(day, 1, GETDATE()) AS DATE)) " +
                     "ORDER BY lh.thoi_gian_hen DESC, lh.so_thu_tu ASC";
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LichHen lh = new LichHen();
                lh.setMaLichHen(rs.getString("ma_lich_hen"));
                lh.setSoThuTu(rs.getInt("so_thu_tu"));
                lh.setTenBenhNhan(rs.getString("ten_benh_nhan"));
                lh.setSoDienThoai(rs.getString("so_dien_thoai"));
                lh.setTenBacSi(rs.getString("ten_bac_si"));
                lh.setPhongKham(rs.getString("phong_kham"));
                lh.setThoiGianHen(rs.getTimestamp("thoi_gian_hen"));
                lh.setTrangThai(rs.getString("trang_thai"));
                list.add(lh);
            }
        }
        return list;
    }

    /**
     * Tìm kiếm bệnh nhân theo từ khóa (Tên / SĐT / CCCD) qua Stored Procedure sp_tim_kiem_benh_nhan
     */
    public List<BenhNhan> timKiemBenhNhan(String role, String tuKhoa) throws SQLException {
        List<BenhNhan> list = new ArrayList<>();
        String call = "{call sp_tim_kiem_benh_nhan(?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, tuKhoa != null ? tuKhoa : "");
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    BenhNhan bn = new BenhNhan();
                    bn.setMaBenhNhan(rs.getString("ma_benh_nhan"));
                    bn.setHoTen(rs.getString("ho_ten"));
                    bn.setNgaySinh(rs.getDate("ngay_sinh"));
                    bn.setGioiTinh(rs.getString("gioi_tinh"));
                    bn.setSoDienThoai(rs.getString("so_dien_thoai"));
                    bn.setSoCccd(rs.getString("so_cccd"));
                    bn.setDiaChi(rs.getString("dia_chi"));
                    bn.setNgayTao(rs.getTimestamp("ngay_tao"));
                    list.add(bn);
                }
            }
        }
        return list;
    }

    /**
     * Tìm kiếm bệnh nhân theo từ khóa (Tên / SĐT / CCCD) có phân trang (OFFSET ... FETCH NEXT)
     */
    public PageResponse<BenhNhan> timKiemBenhNhan(String role, String tuKhoa, int page, int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        int offset = (page - 1) * size;

        List<BenhNhan> list = new ArrayList<>();
        long totalElements = 0;
        String searchParam = "%" + (tuKhoa != null ? tuKhoa.trim() : "") + "%";

        String countSql = "SELECT COUNT(*) FROM benh_nhan " +
                          "WHERE ho_ten LIKE ? OR so_dien_thoai LIKE ? OR so_cccd LIKE ?";
        String dataSql = "SELECT ma_benh_nhan, ho_ten, ngay_sinh, gioi_tinh, so_dien_thoai, " +
                         "ISNULL(so_cccd, N'Chưa có') AS so_cccd, ISNULL(dia_chi, N'Chưa có') AS dia_chi, ngay_tao " +
                         "FROM benh_nhan " +
                         "WHERE ho_ten LIKE ? OR so_dien_thoai LIKE ? OR so_cccd LIKE ? " +
                         "ORDER BY ngay_tao DESC, ma_benh_nhan DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                psCount.setString(1, searchParam);
                psCount.setString(2, searchParam);
                psCount.setString(3, searchParam);
                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next()) {
                        totalElements = rs.getLong(1);
                    }
                }
            }

            if (totalElements > 0) {
                try (PreparedStatement psData = conn.prepareStatement(dataSql)) {
                    psData.setString(1, searchParam);
                    psData.setString(2, searchParam);
                    psData.setString(3, searchParam);
                    psData.setInt(4, offset);
                    psData.setInt(5, size);
                    try (ResultSet rs = psData.executeQuery()) {
                        while (rs.next()) {
                            BenhNhan bn = new BenhNhan();
                            bn.setMaBenhNhan(rs.getString("ma_benh_nhan"));
                            bn.setHoTen(rs.getString("ho_ten"));
                            bn.setNgaySinh(rs.getDate("ngay_sinh"));
                            bn.setGioiTinh(rs.getString("gioi_tinh"));
                            bn.setSoDienThoai(rs.getString("so_dien_thoai"));
                            bn.setSoCccd(rs.getString("so_cccd"));
                            bn.setDiaChi(rs.getString("dia_chi"));
                            bn.setNgayTao(rs.getTimestamp("ngay_tao"));
                            list.add(bn);
                        }
                    }
                }
            }
        }
        return new PageResponse<>(list, page, size, totalElements);
    }

    /**
     * Tạo hóa đơn và chốt thanh toán viện phí qua Stored Procedure sp_gd_thanh_toan_hoa_don
     */
    public void thanhToanHoaDon(String role, String maHoSo, BigDecimal tienGiamGia, String hinhThucThanhToan) throws SQLException {
        String call = "{call sp_gd_thanh_toan_hoa_don(?, ?, ?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, maHoSo);
            cs.setBigDecimal(2, tienGiamGia != null ? tienGiamGia : BigDecimal.ZERO);
            cs.setString(3, hinhThucThanhToan != null ? hinhThucThanhToan : "Tiền mặt");
            cs.execute();
        }
    }

    /**
     * Hủy lịch hẹn khi bệnh nhân bỏ về qua Stored Procedure sp_gd_huy_lich_hen
     */
    public void huyLichHen(String role, String maLichHen, String lyDo) throws SQLException {
        String call = "{call sp_gd_huy_lich_hen(?, ?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, maLichHen);
            cs.setString(2, lyDo != null ? lyDo : "Bệnh nhân yêu cầu hủy");
            cs.execute();
        }
    }
}
