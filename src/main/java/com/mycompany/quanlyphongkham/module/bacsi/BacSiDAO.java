package com.mycompany.quanlyphongkham.module.bacsi;

import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.ChiDinhDichVu;
import com.mycompany.quanlyphongkham.entity.DichVu;
import com.mycompany.quanlyphongkham.entity.DonThuoc;
import com.mycompany.quanlyphongkham.entity.LichHen;
import com.mycompany.quanlyphongkham.entity.Thuoc;
import com.mycompany.quanlyphongkham.util.DBContext;
import com.mycompany.quanlyphongkham.util.FormatUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacSiDAO {

    /**
     * Lấy danh sách bệnh nhân đang đợi phòng khám của bác sĩ hiện tại hôm nay
     */
    public List<LichHen> getDanhSachChoCuaBacSi(String role, String maBacSi) throws SQLException {
        List<LichHen> list = new ArrayList<>();
        String sql = "SELECT lh.ma_lich_hen, lh.so_thu_tu, bn.ma_benh_nhan, bn.ho_ten AS ten_benh_nhan, " +
                     "bn.so_dien_thoai, bs_user.ho_ten AS ten_bac_si, bs.phong_kham, " +
                     "lh.thoi_gian_hen, lh.trang_thai, ISNULL(lh.loai_lich_hen, N'Khám mới') AS loai_lich_hen, lh.ghi_chu " +
                     "FROM lich_hen lh " +
                     "JOIN benh_nhan bn ON lh.ma_benh_nhan = bn.ma_benh_nhan " +
                     "JOIN bac_si bs ON lh.ma_bac_si = bs.ma_bac_si " +
                     "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
                     "WHERE (CAST(lh.thoi_gian_hen AS DATE) >= CAST(DATEADD(day, -1, GETDATE()) AS DATE) " +
                     "    AND CAST(lh.thoi_gian_hen AS DATE) <= CAST(DATEADD(day, 1, GETDATE()) AS DATE)) " +
                     "  AND (UPPER(LTRIM(RTRIM(lh.ma_bac_si))) = UPPER(LTRIM(RTRIM(?))) OR ? IS NULL OR ? = '') " +
                     "  AND (lh.trang_thai LIKE N'%Chờ%' OR lh.trang_thai LIKE N'%Đang%' OR (lh.trang_thai NOT LIKE N'%Đã%' AND lh.trang_thai NOT LIKE N'%Ä%')) " +
                     "ORDER BY lh.so_thu_tu ASC, lh.thoi_gian_hen ASC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBacSi);
            ps.setString(2, maBacSi);
            ps.setString(3, maBacSi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichHen lh = new LichHen();
                    lh.setMaLichHen(rs.getString("ma_lich_hen"));
                    lh.setSoThuTu(rs.getInt("so_thu_tu"));
                    lh.setMaBenhNhan(rs.getString("ma_benh_nhan"));
                    lh.setTenBenhNhan(rs.getString("ten_benh_nhan"));
                    lh.setSoDienThoai(rs.getString("so_dien_thoai"));
                    lh.setTenBacSi(rs.getString("ten_bac_si"));
                    lh.setPhongKham(rs.getString("phong_kham"));
                    lh.setThoiGianHen(rs.getTimestamp("thoi_gian_hen"));
                    lh.setTrangThai(rs.getString("trang_thai"));
                    lh.setLoaiLichHen(rs.getString("loai_lich_hen"));
                    lh.setGhiChu(rs.getString("ghi_chu"));
                    list.add(lh);
                }
            }
        }
        return list;
    }

    /**
     * Tra cứu tiền sử bệnh án qua Table-Valued Function fn_lich_su_kham_benh(@ma_benh_nhan)
     */
    public List<Map<String, Object>> getLichSuKhamBenh(String role, String maBenhNhan) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT ma_ho_so, ngay_kham, ten_bac_si, trieu_chung, chan_doan, tong_tien, trang_thai_thanh_toan " +
                     "FROM dbo.fn_lich_su_kham_benh(?) ORDER BY ngay_kham DESC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBenhNhan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    String maHoSo = rs.getString("ma_ho_so");
                    item.put("maHoSo", maHoSo);
                    item.put("maHoSoCode", maHoSo);
                    item.put("ngayKham", rs.getTimestamp("ngay_kham"));
                    item.put("tenBacSi", rs.getString("ten_bac_si"));
                    item.put("trieuChung", rs.getString("trieu_chung"));
                    item.put("chanDoan", rs.getString("chan_doan"));
                    item.put("tongTien", rs.getBigDecimal("tong_tien"));
                    item.put("trangThaiThanhToan", rs.getString("trang_thai_thanh_toan"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * Tra cứu danh mục thuốc khả dụng qua Table-Valued Function fn_tim_thuoc_kha_dung(@tu_khoa)
     */
    public List<Thuoc> timThuocKhaDung(String role, String tuKhoa) throws SQLException {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT ma_thuoc, ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung " +
                     "FROM dbo.fn_tim_thuoc_kha_dung(?) ORDER BY ten_thuoc ASC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tuKhoa != null ? tuKhoa.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Thuoc t = new Thuoc();
                    t.setMaThuoc(rs.getString("ma_thuoc"));
                    t.setTenThuoc(rs.getString("ten_thuoc"));
                    t.setDonViTinh(rs.getString("don_vi_tinh"));
                    t.setSoLuongTon(rs.getInt("so_luong_ton"));
                    t.setDonGia(rs.getBigDecimal("don_gia"));
                    t.setHanSuDung(rs.getDate("han_su_dung"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    /**
     * Tra cứu danh mục thuốc khả dụng có phân trang (OFFSET ... FETCH NEXT)
     */
    public PageResponse<Thuoc> timThuocKhaDung(String role, String tuKhoa, int page, int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        int offset = (page - 1) * size;

        List<Thuoc> list = new ArrayList<>();
        long totalElements = 0;
        String searchParam = tuKhoa != null ? tuKhoa.trim() : "";

        String countSql = "SELECT COUNT(*) FROM dbo.fn_tim_thuoc_kha_dung(?)";
        String dataSql = "SELECT ma_thuoc, ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung " +
                         "FROM dbo.fn_tim_thuoc_kha_dung(?) " +
                         "ORDER BY ten_thuoc ASC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                psCount.setString(1, searchParam);
                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next()) {
                        totalElements = rs.getLong(1);
                    }
                }
            }

            if (totalElements > 0) {
                try (PreparedStatement psData = conn.prepareStatement(dataSql)) {
                    psData.setString(1, searchParam);
                    psData.setInt(2, offset);
                    psData.setInt(3, size);
                    try (ResultSet rs = psData.executeQuery()) {
                        while (rs.next()) {
                            Thuoc t = new Thuoc();
                            t.setMaThuoc(rs.getString("ma_thuoc"));
                            t.setTenThuoc(rs.getString("ten_thuoc"));
                            t.setDonViTinh(rs.getString("don_vi_tinh"));
                            t.setSoLuongTon(rs.getInt("so_luong_ton"));
                            t.setDonGia(rs.getBigDecimal("don_gia"));
                            t.setHanSuDung(rs.getDate("han_su_dung"));
                            list.add(t);
                        }
                    }
                }
            }
        }
        return new PageResponse<>(list, page, size, totalElements);
    }

    /**
     * Thêm chỉ định dịch vụ cận lâm sàng cho hồ sơ khám
     */
    public String themChiDinhDichVu(String role, String maHoSo, String maDichVu, int soLuong, BigDecimal donGia) throws SQLException {
        String sql = "DECLARE @out TABLE (id VARCHAR(20)); " +
                     "INSERT INTO chi_dinh_dich_vu (ma_ho_so, ma_dich_vu, so_luong, don_gia, trang_thai) " +
                     "OUTPUT INSERTED.ma_chi_dinh INTO @out " +
                     "VALUES (?, ?, ?, ISNULL(?, (SELECT don_gia FROM dich_vu WHERE ma_dich_vu = ?)), N'Chưa thực hiện'); " +
                     "SELECT id FROM @out;";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoSo);
            ps.setString(2, maDichVu);
            ps.setInt(3, soLuong > 0 ? soLuong : 1);
            if (donGia != null) {
                ps.setBigDecimal(4, donGia);
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.setString(5, maDichVu);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    /**
     * Lấy danh mục dịch vụ kỹ thuật cận lâm sàng đang hoạt động
     */
    public List<DichVu> getDanhSachDichVu(String role) throws SQLException {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT ma_dich_vu, ten_dich_vu, don_gia, mo_ta, trang_thai FROM dich_vu WHERE trang_thai = 1 ORDER BY ten_dich_vu ASC";
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DichVu dv = new DichVu();
                dv.setMaDichVu(rs.getString("ma_dich_vu"));
                dv.setTenDichVu(rs.getString("ten_dich_vu"));
                dv.setDonGia(rs.getBigDecimal("don_gia"));
                dv.setMoTa(rs.getString("mo_ta"));
                dv.setTrangThai(rs.getBoolean("trang_thai"));
                list.add(dv);
            }
        }
        return list;
    }

    /**
     * Hoàn tất khám: Lưu hồ sơ khám bệnh, kích hoạt trigger đổi trạng thái lịch hẹn, kê đơn thuốc (trừ kho),
     * lưu chỉ định dịch vụ và tự động lập lịch hẹn tái khám (nếu có chỉ định)
     */
    public String hoanTatKham(String role, String maLichHen, String maBenhNhan, String maBacSi,
                              String trieuChung, String chanDoan, String loiDan,
                              List<DonThuoc> danhSachThuoc,
                              List<ChiDinhDichVu> danhSachDichVu,
                              java.sql.Date ngayHenTaiKham,
                              String ghiChuTaiKham) throws SQLException {
        String insertHoSoSql = "DECLARE @out TABLE (id VARCHAR(20)); " +
                               "INSERT INTO ho_so_kham (ma_lich_hen, ma_benh_nhan, ma_bac_si, trieu_chung, chan_doan, loi_dan, ngay_tai_kham) " +
                               "OUTPUT INSERTED.ma_ho_so INTO @out " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?); " +
                               "SELECT id FROM @out;";

        String insertDonThuocSql = "INSERT INTO don_thuoc (ma_ho_so, ma_thuoc, so_luong, cach_dung, huong_dan) " +
                                   "VALUES (?, ?, ?, ?, ?)";

        String insertChiDinhSql = "INSERT INTO chi_dinh_dich_vu (ma_ho_so, ma_dich_vu, so_luong, don_gia, trang_thai) " +
                                  "VALUES (?, ?, ?, ISNULL(?, (SELECT don_gia FROM dich_vu WHERE ma_dich_vu = ?)), N'Chưa thực hiện')";

        String insertLichTaiKhamSql = "DECLARE @cnt INT; " +
                                      "SELECT @cnt = COUNT(*) FROM lich_hen WHERE ma_bac_si = ? AND CAST(thoi_gian_hen AS DATE) = CAST(? AS DATE); " +
                                      "DECLARE @target_time DATETIME; " +
                                      "SET @target_time = DATEADD(minute, @cnt * 15, CAST(? AS DATETIME) + CAST('08:00:00' AS DATETIME)); " +
                                      "INSERT INTO lich_hen (ma_benh_nhan, ma_bac_si, thoi_gian_hen, so_thu_tu, trang_thai, loai_lich_hen, ma_lich_hen_truoc, ghi_chu) " +
                                      "VALUES (?, ?, @target_time, 0, N'Hẹn tái khám', N'Tái khám', ?, ?);";

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            conn.setAutoCommit(false);
            String maHoSo;
            try {
                // 1. Tạo hồ sơ khám
                try (PreparedStatement psHoSo = conn.prepareStatement(insertHoSoSql)) {
                    psHoSo.setString(1, maLichHen);
                    psHoSo.setString(2, maBenhNhan);
                    psHoSo.setString(3, maBacSi);
                    psHoSo.setString(4, trieuChung != null ? trieuChung : "");
                    psHoSo.setString(5, chanDoan != null ? chanDoan : "");
                    psHoSo.setString(6, loiDan != null ? loiDan : "");
                    if (ngayHenTaiKham != null) {
                        psHoSo.setDate(7, ngayHenTaiKham);
                    } else {
                        psHoSo.setNull(7, Types.DATE);
                    }

                    try (ResultSet rs = psHoSo.executeQuery()) {
                        if (rs.next()) {
                            maHoSo = rs.getString(1);
                        } else {
                            throw new SQLException("Không thể tạo hồ sơ khám bệnh!");
                        }
                    }
                }

                // 2. Kê đơn thuốc (Trigger trg_ke_don_tru_kho và trg_kiem_tra_han_thuoc sẽ tự động kiểm tra và trừ kho)
                if (danhSachThuoc != null && !danhSachThuoc.isEmpty()) {
                    try (PreparedStatement psThuoc = conn.prepareStatement(insertDonThuocSql)) {
                        for (DonThuoc dt : danhSachThuoc) {
                            psThuoc.setString(1, maHoSo);
                            psThuoc.setString(2, dt.getMaThuoc());
                            psThuoc.setInt(3, dt.getSoLuong() != null ? dt.getSoLuong() : 1);
                            psThuoc.setString(4, dt.getCachDung() != null ? dt.getCachDung() : "Theo chỉ định");
                            psThuoc.setString(5, dt.getHuongDan() != null ? dt.getHuongDan() : "");
                            psThuoc.addBatch();
                        }
                        psThuoc.executeBatch();
                    }
                }

                // 3. Chỉ định dịch vụ cận lâm sàng / kỹ thuật
                if (danhSachDichVu != null && !danhSachDichVu.isEmpty()) {
                    try (PreparedStatement psDv = conn.prepareStatement(insertChiDinhSql)) {
                        for (ChiDinhDichVu cd : danhSachDichVu) {
                            psDv.setString(1, maHoSo);
                            psDv.setString(2, cd.getMaDichVu());
                            psDv.setInt(3, cd.getSoLuong() != null && cd.getSoLuong() > 0 ? cd.getSoLuong() : 1);
                            if (cd.getDonGia() != null) {
                                psDv.setBigDecimal(4, cd.getDonGia());
                            } else {
                                psDv.setNull(4, Types.DECIMAL);
                            }
                            psDv.setString(5, cd.getMaDichVu());
                            psDv.addBatch();
                        }
                        psDv.executeBatch();
                    }
                }

                // 4. Tự động lập lịch hẹn tái khám nếu có chỉ định ngày tái khám
                if (ngayHenTaiKham != null) {
                    try (PreparedStatement psTaiKham = conn.prepareStatement(insertLichTaiKhamSql)) {
                        psTaiKham.setString(1, maBacSi);
                        psTaiKham.setDate(2, ngayHenTaiKham);
                        psTaiKham.setDate(3, ngayHenTaiKham);
                        psTaiKham.setString(4, maBenhNhan);
                        psTaiKham.setString(5, maBacSi);
                        psTaiKham.setString(6, maLichHen);
                        String note = (ghiChuTaiKham != null && !ghiChuTaiKham.trim().isEmpty())
                                ? ghiChuTaiKham.trim()
                                : ("Tái khám theo chỉ định - " + (chanDoan != null ? chanDoan : ""));
                        psTaiKham.setString(7, note);
                        psTaiKham.executeUpdate();
                    }
                }

                conn.commit();
                return maHoSo;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Tương thích phiên bản: Hoàn tất khám có đơn thuốc và chỉ định dịch vụ (không hẹn tái khám)
     */
    public String hoanTatKham(String role, String maLichHen, String maBenhNhan, String maBacSi,
                              String trieuChung, String chanDoan, String loiDan,
                              List<DonThuoc> danhSachThuoc,
                              List<ChiDinhDichVu> danhSachDichVu) throws SQLException {
        return hoanTatKham(role, maLichHen, maBenhNhan, maBacSi, trieuChung, chanDoan, loiDan, danhSachThuoc, danhSachDichVu, null, null);
    }

    /**
     * Tương thích phiên bản cũ: Hoàn tất khám chỉ có đơn thuốc
     */
    public String hoanTatKham(String role, String maLichHen, String maBenhNhan, String maBacSi,
                              String trieuChung, String chanDoan, String loiDan,
                              List<DonThuoc> danhSachThuoc) throws SQLException {
        return hoanTatKham(role, maLichHen, maBenhNhan, maBacSi, trieuChung, chanDoan, loiDan, danhSachThuoc, null, null, null);
    }
}
