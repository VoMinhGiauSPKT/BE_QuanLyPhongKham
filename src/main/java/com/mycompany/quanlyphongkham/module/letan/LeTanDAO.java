package com.mycompany.quanlyphongkham.module.letan;

import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.BenhNhan;
import com.mycompany.quanlyphongkham.entity.LichHen;
import com.mycompany.quanlyphongkham.util.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeTanDAO {

    private static volatile boolean procUpdated = false;
    private static volatile boolean checkoutPermsUpdated = false;

    private static synchronized void ensureCheckoutPermissions() {
        if (checkoutPermsUpdated) return;
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("GRANT SELECT ON bac_si TO vai_tro_le_tan");
                stmt.execute("GRANT SELECT ON nguoi_dung TO vai_tro_le_tan");
                stmt.execute("GRANT SELECT ON thuoc TO vai_tro_le_tan");
                stmt.execute("GRANT SELECT ON ho_so_kham TO vai_tro_le_tan");
                stmt.execute("DENY SELECT ON ho_so_kham(chan_doan, loi_dan) TO vai_tro_le_tan");
                stmt.execute("GRANT EXECUTE ON fn_tinh_tong_tien_dich_vu TO vai_tro_le_tan");
                stmt.execute("GRANT EXECUTE ON fn_tinh_tong_tien_thuoc TO vai_tro_le_tan");
                stmt.execute("GRANT EXECUTE ON fn_tinh_tuoi TO vai_tro_le_tan");
                stmt.execute("GRANT EXECUTE ON sp_gd_huy_don_thuoc TO vai_tro_le_tan");
                stmt.execute("GRANT EXECUTE ON sp_gd_thanh_toan_hoa_don TO vai_tro_le_tan");
                stmt.execute("GRANT EXECUTE ON sp_gd_huy_lich_hen TO vai_tro_le_tan");
            }
            checkoutPermsUpdated = true;
        } catch (Exception e) {
            System.err.println("[LeTanDAO] Cảnh báo cập nhật quyền checkout: " + e.getMessage());
        }
    }

    private static synchronized void ensureSpTiepNhanKhamUpdated() {
        if (procUpdated) return;
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI")) {
            String alterSql = 
                "CREATE OR ALTER PROCEDURE sp_tiep_nhan_kham " +
                "    @ho_ten NVARCHAR(100), " +
                "    @ngay_sinh DATE, " +
                "    @gioi_tinh NVARCHAR(10), " +
                "    @so_dien_thoai VARCHAR(15), " +
                "    @ma_bac_si VARCHAR(20), " +
                "    @thoi_gian_hen DATETIME, " +
                "    @ma_lich_hen VARCHAR(20) OUTPUT, " +
                "    @so_cccd VARCHAR(20) = NULL, " +
                "    @dia_chi NVARCHAR(255) = NULL " +
                "AS " +
                "BEGIN " +
                "    SET NOCOUNT ON; " +
                "    BEGIN TRY " +
                "        BEGIN TRANSACTION; " +
                "            DECLARE @ma_benh_nhan VARCHAR(20); " +
                "            SELECT @ma_benh_nhan = ma_benh_nhan FROM benh_nhan " +
                "            WHERE so_dien_thoai = @so_dien_thoai OR (@so_cccd IS NOT NULL AND so_cccd = @so_cccd); " +
                "            IF @ma_benh_nhan IS NULL " +
                "            BEGIN " +
                "                DECLARE @new_bn TABLE (ma_benh_nhan VARCHAR(20)); " +
                "                INSERT INTO benh_nhan (ho_ten, ngay_sinh, gioi_tinh, so_dien_thoai, so_cccd, dia_chi) " +
                "                OUTPUT INSERTED.ma_benh_nhan INTO @new_bn " +
                "                VALUES (@ho_ten, @ngay_sinh, @gioi_tinh, @so_dien_thoai, @so_cccd, @dia_chi); " +
                "                SELECT @ma_benh_nhan = ma_benh_nhan FROM @new_bn; " +
                "            END " +
                "            ELSE " +
                "            BEGIN " +
                "                UPDATE benh_nhan " +
                "                SET so_cccd = ISNULL(@so_cccd, so_cccd), " +
                "                    dia_chi = ISNULL(@dia_chi, dia_chi) " +
                "                WHERE ma_benh_nhan = @ma_benh_nhan; " +
                "            END; " +
                "            DECLARE @so_thu_tu_tiep_theo INT; " +
                "            SELECT @so_thu_tu_tiep_theo = ISNULL(MAX(so_thu_tu), 0) + 1 " +
                "            FROM lich_hen " +
                "            WHERE ma_bac_si = @ma_bac_si AND CAST(thoi_gian_hen AS DATE) = CAST(@thoi_gian_hen AS DATE); " +
                "            DECLARE @new_lh TABLE (ma_lich_hen VARCHAR(20)); " +
                "            INSERT INTO lich_hen (ma_benh_nhan, ma_bac_si, thoi_gian_hen, so_thu_tu, trang_thai) " +
                "            OUTPUT INSERTED.ma_lich_hen INTO @new_lh " +
                "            VALUES (@ma_benh_nhan, @ma_bac_si, @thoi_gian_hen, @so_thu_tu_tiep_theo, N'Chờ khám'); " +
                "            SELECT @ma_lich_hen = ma_lich_hen FROM @new_lh; " +
                "        COMMIT TRANSACTION; " +
                "    END TRY " +
                "    BEGIN CATCH " +
                "        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION; " +
                "        THROW; " +
                "    END CATCH; " +
                "END;";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(alterSql);
                stmt.execute("GRANT EXECUTE ON sp_tiep_nhan_kham TO vai_tro_le_tan");
            }
            procUpdated = true;
        } catch (Exception e) {
            System.err.println("[LeTanDAO] Cảnh báo cập nhật procedure sp_tiep_nhan_kham: " + e.getMessage());
        }
    }

    /**
     * Tiếp nhận ca khám bệnh mới qua Stored Procedure sp_tiep_nhan_kham (Có số CCCD và Địa chỉ)
     */
    public String tiepNhanKham(String role, String hoTen, java.sql.Date ngaySinh, String gioiTinh,
                               String soDienThoai, String soCccd, String diaChi, String maBacSi, java.sql.Timestamp thoiGianHen) throws SQLException {
        ensureSpTiepNhanKhamUpdated();
        String call = "{call sp_tiep_nhan_kham(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, hoTen);
            cs.setDate(2, ngaySinh);
            cs.setString(3, gioiTinh);
            cs.setString(4, soDienThoai);
            cs.setString(5, maBacSi);
            cs.setTimestamp(6, thoiGianHen);
            cs.registerOutParameter(7, Types.VARCHAR);
            cs.setString(8, soCccd != null && !soCccd.trim().isEmpty() ? soCccd.trim() : null);
            cs.setString(9, diaChi != null && !diaChi.trim().isEmpty() ? diaChi.trim() : null);

            cs.execute();
            return cs.getString(7);
        }
    }

    /**
     * Tiếp nhận ca khám bệnh tương thích có CCCD
     */
    public String tiepNhanKham(String role, String hoTen, java.sql.Date ngaySinh, String gioiTinh,
                               String soDienThoai, String soCccd, String maBacSi, java.sql.Timestamp thoiGianHen) throws SQLException {
        return tiepNhanKham(role, hoTen, ngaySinh, gioiTinh, soDienThoai, soCccd, null, maBacSi, thoiGianHen);
    }

    /**
     * Tiếp nhận ca khám bệnh tương thích phiên bản cũ (Không truyền CCCD và Địa chỉ)
     */
    public String tiepNhanKham(String role, String hoTen, java.sql.Date ngaySinh, String gioiTinh,
                               String soDienThoai, String maBacSi, java.sql.Timestamp thoiGianHen) throws SQLException {
        return tiepNhanKham(role, hoTen, ngaySinh, gioiTinh, soDienThoai, null, null, maBacSi, thoiGianHen);
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
                    bn.setTuoi((Integer) rs.getObject("tuoi"));
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
        String dataSql = "SELECT ma_benh_nhan, ho_ten, ngay_sinh, dbo.fn_tinh_tuoi(ngay_sinh) AS tuoi, gioi_tinh, so_dien_thoai, " +
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
                            bn.setTuoi((Integer) rs.getObject("tuoi"));
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
     * Lấy danh sách các ca khám hoàn thành đang chờ thanh toán viện phí trong ngày
     */
    public List<Map<String, Object>> getDanhSachChoThanhToan(String role, String tuKhoa) throws SQLException {
        ensureCheckoutPermissions();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = 
            "SELECT " +
            "    hs.ma_ho_so, " +
            "    hs.ma_lich_hen, " +
            "    hs.ma_benh_nhan, " +
            "    bn.ho_ten AS ten_benh_nhan, " +
            "    bn.so_dien_thoai, " +
            "    ISNULL(bn.so_cccd, N'Chưa có') AS so_cccd, " +
            "    ISNULL(bn.dia_chi, N'Chưa có') AS dia_chi, " +
            "    bn.gioi_tinh, " +
            "    bn.ngay_sinh, " +
            "    bs_user.ho_ten AS ten_bac_si, " +
            "    bs.phong_kham, " +
            "    hs.ngay_kham, " +
            "    ISNULL(dbo.fn_tinh_tong_tien_dich_vu(hs.ma_ho_so), 0) AS tien_dich_vu, " +
            "    ISNULL(dbo.fn_tinh_tong_tien_thuoc(hs.ma_ho_so), 0) AS tien_thuoc, " +
            "    (ISNULL(dbo.fn_tinh_tong_tien_dich_vu(hs.ma_ho_so), 0) + ISNULL(dbo.fn_tinh_tong_tien_thuoc(hs.ma_ho_so), 0)) AS tong_tam_tinh, " +
            "    (SELECT COUNT(*) FROM chi_dinh_dich_vu cd WHERE cd.ma_ho_so = hs.ma_ho_so) AS so_dich_vu, " +
            "    (SELECT COUNT(*) FROM don_thuoc dt WHERE dt.ma_ho_so = hs.ma_ho_so) AS so_thuoc " +
            "FROM ho_so_kham hs " +
            "JOIN benh_nhan bn ON hs.ma_benh_nhan = bn.ma_benh_nhan " +
            "JOIN bac_si bs ON hs.ma_bac_si = bs.ma_bac_si " +
            "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
            "LEFT JOIN hoa_don hd ON hs.ma_ho_so = hd.ma_ho_so " +
            "WHERE (hd.ma_hoa_don IS NULL OR hd.trang_thai = N'Chưa thanh toán') " +
            "  AND ( " +
            "      ? IS NULL OR ? = '' OR " +
            "      bn.ho_ten LIKE ? OR " +
            "      bn.so_dien_thoai LIKE ? OR " +
            "      bn.so_cccd LIKE ? OR " +
            "      hs.ma_ho_so LIKE ? OR " +
            "      hs.ma_lich_hen LIKE ? " +
            "  ) " +
            "ORDER BY hs.ngay_kham DESC";

        String search = tuKhoa != null ? "%" + tuKhoa.trim() + "%" : "";
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tuKhoa);
            ps.setString(2, tuKhoa != null ? tuKhoa.trim() : "");
            ps.setString(3, search);
            ps.setString(4, search);
            ps.setString(5, search);
            ps.setString(6, search);
            ps.setString(7, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maHoSo", rs.getString("ma_ho_so"));
                    map.put("maHoSoCode", rs.getString("ma_ho_so"));
                    map.put("maLichHen", rs.getString("ma_lich_hen"));
                    map.put("maLichHenCode", rs.getString("ma_lich_hen"));
                    map.put("maBenhNhan", rs.getString("ma_benh_nhan"));
                    map.put("maBenhNhanCode", rs.getString("ma_benh_nhan"));
                    map.put("tenBenhNhan", rs.getString("ten_benh_nhan"));
                    map.put("soDienThoai", rs.getString("so_dien_thoai"));
                    map.put("soCccd", rs.getString("so_cccd"));
                    map.put("diaChi", rs.getString("dia_chi"));
                    map.put("gioiTinh", rs.getString("gioi_tinh"));
                    map.put("ngaySinh", rs.getDate("ngay_sinh"));
                    map.put("tenBacSi", rs.getString("ten_bac_si"));
                    map.put("phongKham", rs.getString("phong_kham"));
                    map.put("ngayKham", rs.getTimestamp("ngay_kham"));
                    map.put("tienDichVu", rs.getBigDecimal("tien_dich_vu"));
                    map.put("tienThuoc", rs.getBigDecimal("tien_thuoc"));
                    map.put("tongTamTinh", rs.getBigDecimal("tong_tam_tinh"));
                    map.put("soDichVu", rs.getInt("so_dich_vu"));
                    map.put("soThuoc", rs.getInt("so_thuoc"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * Lấy chi tiết bảng kê viện phí (dịch vụ cận lâm sàng & thuốc) của một hồ sơ khám
     */
    public Map<String, Object> getChiTietThanhToan(String role, String maHoSo) throws SQLException {
        ensureCheckoutPermissions();
        Map<String, Object> result = new HashMap<>();

        String headerSql = 
            "SELECT " +
            "    hs.ma_ho_so, hs.ma_lich_hen, hs.ngay_kham, " +
            "    bn.ma_benh_nhan, bn.ho_ten AS ten_benh_nhan, bn.so_dien_thoai, " +
            "    ISNULL(bn.so_cccd, N'Chưa có') AS so_cccd, ISNULL(bn.dia_chi, N'Chưa có') AS dia_chi, " +
            "    bn.gioi_tinh, bn.ngay_sinh, " +
            "    bs_user.ho_ten AS ten_bac_si, bs.phong_kham, bs.chuyen_khoa, " +
            "    hd.ma_hoa_don, hd.trang_thai AS trang_thai_hoa_don, hd.hinh_thuc_thanh_toan, " +
            "    hd.tien_giam_gia, hd.tong_tien AS thuc_thu_da_luu, hd.ngay_lap " +
            "FROM ho_so_kham hs " +
            "JOIN benh_nhan bn ON hs.ma_benh_nhan = bn.ma_benh_nhan " +
            "JOIN bac_si bs ON hs.ma_bac_si = bs.ma_bac_si " +
            "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
            "LEFT JOIN hoa_don hd ON hs.ma_ho_so = hd.ma_ho_so " +
            "WHERE hs.ma_ho_so = ?";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setString(1, maHoSo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("maHoSo", rs.getString("ma_ho_so"));
                    result.put("maHoSoCode", rs.getString("ma_ho_so"));
                    result.put("maLichHen", rs.getString("ma_lich_hen"));
                    result.put("maLichHenCode", rs.getString("ma_lich_hen"));
                    result.put("ngayKham", rs.getTimestamp("ngay_kham"));
                    result.put("maBenhNhan", rs.getString("ma_benh_nhan"));
                    result.put("maBenhNhanCode", rs.getString("ma_benh_nhan"));
                    result.put("tenBenhNhan", rs.getString("ten_benh_nhan"));
                    result.put("soDienThoai", rs.getString("so_dien_thoai"));
                    result.put("soCccd", rs.getString("so_cccd"));
                    result.put("diaChi", rs.getString("dia_chi"));
                    result.put("gioiTinh", rs.getString("gioi_tinh"));
                    result.put("ngaySinh", rs.getDate("ngay_sinh"));
                    result.put("tenBacSi", rs.getString("ten_bac_si"));
                    result.put("phongKham", rs.getString("phong_kham"));
                    result.put("chuyenKhoa", rs.getString("chuyen_khoa"));
                    result.put("maHoaDon", rs.getString("ma_hoa_don"));
                    result.put("trangThaiHoaDon", rs.getString("trang_thai_hoa_don"));
                    result.put("hinhThucThanhToan", rs.getString("hinh_thuc_thanh_toan"));
                    result.put("tienGiamGiaDaLuu", rs.getBigDecimal("tien_giam_gia"));
                    result.put("thucThuDaLuu", rs.getBigDecimal("thuc_thu_da_luu"));
                    result.put("ngayLap", rs.getTimestamp("ngay_lap"));
                } else {
                    return null;
                }
            }
        }

        String dvSql = 
            "SELECT cd.ma_chi_dinh, cd.ma_dich_vu, dv.ten_dich_vu, cd.so_luong, cd.don_gia, " +
            "       (cd.so_luong * cd.don_gia) AS thanh_tien, cd.ket_qua, cd.trang_thai " +
            "FROM chi_dinh_dich_vu cd " +
            "JOIN dich_vu dv ON cd.ma_dich_vu = dv.ma_dich_vu " +
            "WHERE cd.ma_ho_so = ? " +
            "ORDER BY cd.ma_chi_dinh ASC";
        List<Map<String, Object>> danhSachDichVu = new ArrayList<>();
        BigDecimal tongTienDichVu = BigDecimal.ZERO;
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(dvSql)) {
            ps.setString(1, maHoSo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dv = new HashMap<>();
                    dv.put("maChiDinh", rs.getString("ma_chi_dinh"));
                    dv.put("maDichVu", rs.getString("ma_dich_vu"));
                    dv.put("tenDichVu", rs.getString("ten_dich_vu"));
                    int sl = rs.getInt("so_luong");
                    BigDecimal dg = rs.getBigDecimal("don_gia");
                    BigDecimal tt = rs.getBigDecimal("thanh_tien");
                    dv.put("soLuong", sl);
                    dv.put("donGia", dg);
                    dv.put("thanhTien", tt);
                    dv.put("ketQua", rs.getString("ket_qua"));
                    dv.put("trangThai", rs.getString("trang_thai"));
                    danhSachDichVu.add(dv);
                    if (tt != null) tongTienDichVu = tongTienDichVu.add(tt);
                }
            }
        }
        result.put("danhSachDichVu", danhSachDichVu);
        result.put("tienDichVu", tongTienDichVu);

        String thSql = 
            "SELECT dt.ma_don_thuoc, dt.ma_thuoc, t.ten_thuoc, t.don_vi_tinh, dt.so_luong, " +
            "       t.don_gia, (dt.so_luong * t.don_gia) AS thanh_tien, dt.cach_dung, dt.huong_dan " +
            "FROM don_thuoc dt " +
            "JOIN thuoc t ON dt.ma_thuoc = t.ma_thuoc " +
            "WHERE dt.ma_ho_so = ? " +
            "ORDER BY dt.ma_don_thuoc ASC";
        List<Map<String, Object>> danhSachThuoc = new ArrayList<>();
        BigDecimal tongTienThuoc = BigDecimal.ZERO;
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(thSql)) {
            ps.setString(1, maHoSo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> th = new HashMap<>();
                    th.put("maDonThuoc", rs.getString("ma_don_thuoc"));
                    th.put("maThuoc", rs.getString("ma_thuoc"));
                    th.put("tenThuoc", rs.getString("ten_thuoc"));
                    th.put("donViTinh", rs.getString("don_vi_tinh"));
                    int sl = rs.getInt("so_luong");
                    BigDecimal dg = rs.getBigDecimal("don_gia");
                    BigDecimal tt = rs.getBigDecimal("thanh_tien");
                    th.put("soLuong", sl);
                    th.put("donGia", dg);
                    th.put("thanhTien", tt);
                    th.put("cachDung", rs.getString("cach_dung"));
                    th.put("huongDan", rs.getString("huong_dan"));
                    danhSachThuoc.add(th);
                    if (tt != null) tongTienThuoc = tongTienThuoc.add(tt);
                }
            }
        }
        result.put("danhSachThuoc", danhSachThuoc);
        result.put("tienThuoc", tongTienThuoc);
        result.put("tongTamTinh", tongTienDichVu.add(tongTienThuoc));

        return result;
    }

    /**
     * Tạo hóa đơn và chốt thanh toán viện phí qua Stored Procedure sp_gd_thanh_toan_hoa_don
     */
    public Map<String, Object> thanhToanHoaDon(String role, String maHoSo, BigDecimal tienGiamGia, String hinhThucThanhToan) throws SQLException {
        ensureCheckoutPermissions();
        String call = "{call sp_gd_thanh_toan_hoa_don(?, ?, ?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, maHoSo);
            cs.setBigDecimal(2, tienGiamGia != null ? tienGiamGia : BigDecimal.ZERO);
            cs.setString(3, hinhThucThanhToan != null ? hinhThucThanhToan : "Tiền mặt");
            cs.execute();
        }

        String selectSql = "SELECT hd.ma_hoa_don, hd.ma_ho_so, hd.tien_dich_vu, hd.tien_thuoc, hd.tien_giam_gia, " +
                           "hd.tong_tien, hd.hinh_thuc_thanh_toan, hd.trang_thai, hd.ngay_lap " +
                           "FROM hoa_don hd WHERE hd.ma_ho_so = ?";
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, maHoSo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("maHoaDon", rs.getString("ma_hoa_don"));
                    result.put("maHoaDonCode", rs.getString("ma_hoa_don"));
                    result.put("maHoSo", rs.getString("ma_ho_so"));
                    result.put("maHoSoCode", rs.getString("ma_ho_so"));
                    result.put("tienDichVu", rs.getBigDecimal("tien_dich_vu"));
                    result.put("tienThuoc", rs.getBigDecimal("tien_thuoc"));
                    result.put("tienGiamGia", rs.getBigDecimal("tien_giam_gia"));
                    result.put("tongTien", rs.getBigDecimal("tong_tien"));
                    result.put("thucThu", rs.getBigDecimal("tong_tien"));
                    result.put("hinhThucThanhToan", rs.getString("hinh_thuc_thanh_toan"));
                    result.put("trangThai", rs.getString("trang_thai"));
                    result.put("ngayLap", rs.getTimestamp("ngay_lap"));
                }
            }
        }
        return result;
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

    /**
     * Hủy đơn thuốc qua Stored Procedure sp_gd_huy_don_thuoc (kích hoạt trigger trg_xoa_don_hoan_kho)
     */
    public void huyDonThuoc(String role, String maHoSo) throws SQLException {
        ensureCheckoutPermissions();
        String call = "{call sp_gd_huy_don_thuoc(?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, maHoSo);
            cs.execute();
        }
    }

    /**
     * Tra cứu các lịch hẹn tái khám đang chờ tiếp nhận của một bệnh nhân
     */
    public List<Map<String, Object>> getLichTaiKhamCuaBenhNhan(String role, String maBenhNhan) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT lh.ma_lich_hen, lh.ma_benh_nhan, bn.ho_ten AS ten_benh_nhan, " +
                     "       bn.so_cccd, bn.so_dien_thoai, lh.ma_bac_si, bs_user.ho_ten AS ten_bac_si, " +
                     "       bs.phong_kham, lh.thoi_gian_hen, lh.trang_thai, " +
                     "       ISNULL(lh.loai_lich_hen, N'Tái khám') AS loai_lich_hen, lh.ghi_chu, " +
                     "       CAST(lh.thoi_gian_hen AS DATE) AS ngay_hen, " +
                     "       CAST(GETDATE() AS DATE) AS ngay_hien_tai " +
                     "FROM lich_hen lh " +
                     "JOIN benh_nhan bn ON lh.ma_benh_nhan = bn.ma_benh_nhan " +
                     "JOIN bac_si bs ON lh.ma_bac_si = bs.ma_bac_si " +
                     "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
                     "WHERE lh.ma_benh_nhan = ? AND lh.trang_thai = N'Hẹn tái khám' " +
                     "ORDER BY lh.thoi_gian_hen ASC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBenhNhan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maLichHen", rs.getString("ma_lich_hen"));
                    map.put("maLichHenCode", rs.getString("ma_lich_hen"));
                    map.put("maBenhNhan", rs.getString("ma_benh_nhan"));
                    map.put("tenBenhNhan", rs.getString("ten_benh_nhan"));
                    map.put("soCccd", rs.getString("so_cccd"));
                    map.put("soDienThoai", rs.getString("so_dien_thoai"));
                    map.put("maBacSi", rs.getString("ma_bac_si"));
                    map.put("tenBacSi", rs.getString("ten_bac_si"));
                    map.put("phongKham", rs.getString("phong_kham"));
                    map.put("thoiGianHen", rs.getTimestamp("thoi_gian_hen"));
                    map.put("trangThai", rs.getString("trang_thai"));
                    map.put("loaiLichHen", rs.getString("loai_lich_hen"));
                    map.put("ghiChu", rs.getString("ghi_chu"));
                    map.put("ngayHen", rs.getDate("ngay_hen"));
                    map.put("ngayHienTai", rs.getDate("ngay_hien_tai"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * Tiếp nhận lịch hẹn tái khám (Check-in 1-Click):
     * Đổi trạng thái sang 'Chờ khám', cấp số thứ tự hôm nay và cập nhật thời gian
     */
    public Map<String, Object> tiepNhanTaiKham(String role, String maLichHen) throws SQLException {
        String updateSql = "DECLARE @ma_bac_si VARCHAR(20); " +
                           "SELECT @ma_bac_si = ma_bac_si FROM lich_hen WHERE ma_lich_hen = ?; " +
                           "IF @ma_bac_si IS NULL THROW 50001, N'Không tìm thấy lịch hẹn tái khám!', 1; " +
                           "DECLARE @next_stt INT; " +
                           "SELECT @next_stt = ISNULL(MAX(so_thu_tu), 0) + 1 " +
                           "FROM lich_hen " +
                           "WHERE ma_bac_si = @ma_bac_si " +
                           "  AND CAST(thoi_gian_hen AS DATE) = CAST(GETDATE() AS DATE) " +
                           "  AND trang_thai IN (N'Chờ khám', N'Đang khám', N'Đã khám'); " +
                           "UPDATE lich_hen " +
                           "SET trang_thai = N'Chờ khám', " +
                           "    so_thu_tu = @next_stt, " +
                           "    thoi_gian_hen = GETDATE() " +
                           "WHERE ma_lich_hen = ?; " +
                           "SELECT lh.ma_lich_hen, lh.so_thu_tu, lh.trang_thai, lh.thoi_gian_hen, " +
                           "       bn.ho_ten AS ten_benh_nhan, bs_user.ho_ten AS ten_bac_si, bs.phong_kham " +
                           "FROM lich_hen lh " +
                           "JOIN benh_nhan bn ON lh.ma_benh_nhan = bn.ma_benh_nhan " +
                           "JOIN bac_si bs ON lh.ma_bac_si = bs.ma_bac_si " +
                           "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
                           "WHERE lh.ma_lich_hen = ?;";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, maLichHen);
            ps.setString(2, maLichHen);
            ps.setString(3, maLichHen);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> res = new HashMap<>();
                    res.put("maLichHen", rs.getString("ma_lich_hen"));
                    res.put("soThuTu", rs.getInt("so_thu_tu"));
                    res.put("trangThai", rs.getString("trang_thai"));
                    res.put("thoiGianHen", rs.getTimestamp("thoi_gian_hen"));
                    res.put("tenBenhNhan", rs.getString("ten_benh_nhan"));
                    res.put("tenBacSi", rs.getString("ten_bac_si"));
                    res.put("phongKham", rs.getString("phong_kham"));
                    return res;
                }
            }
        }
        throw new SQLException("Không thể tiếp nhận lịch hẹn tái khám!");
    }
}
