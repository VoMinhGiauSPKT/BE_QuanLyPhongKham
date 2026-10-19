package com.mycompany.quanlyphongkham.module.admin;

import com.mycompany.quanlyphongkham.util.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDAO {

    private Connection getConnection(String role) throws SQLException {
        ensureDoctorPermissions();
        if (role == null || role.trim().isEmpty() || role.equalsIgnoreCase("AUTHENTICATED")) {
            return DBContext.getConnectionByRole("QUAN_TRI");
        }
        return DBContext.getConnectionByRole(role);
    }

    /**
     * Báo cáo tổng hợp doanh thu theo từng ngày từ View v_doanh_thu_theo_ngay
     */
    public List<Map<String, Object>> getDoanhThuTheoNgay(String role) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT ngay_thanh_toan, so_luong_hoa_don, tong_tien_dich_vu, " +
                     "tong_tien_thuoc, tong_tien_giam_gia, thuc_thu " +
                     "FROM v_doanh_thu_theo_ngay ORDER BY ngay_thanh_toan DESC";

        try (Connection conn = getConnection(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("ngayThanhToan", rs.getDate("ngay_thanh_toan"));
                map.put("ngayKham", rs.getDate("ngay_thanh_toan"));
                map.put("soLuongHoaDon", rs.getInt("so_luong_hoa_don"));
                map.put("soLuotKham", rs.getInt("so_luong_hoa_don"));
                map.put("tongTienDichVu", rs.getBigDecimal("tong_tien_dich_vu"));
                map.put("tongTienThuoc", rs.getBigDecimal("tong_tien_thuoc"));
                map.put("tongTienGiamGia", rs.getBigDecimal("tong_tien_giam_gia"));
                map.put("thucThu", rs.getBigDecimal("thuc_thu"));
                map.put("tongDoanhThu", rs.getBigDecimal("thuc_thu"));
                map.put("tongTienThu", rs.getBigDecimal("thuc_thu"));
                list.add(map);
            }
        }
        return list;
    }

    /**
     * Báo cáo số ca khám và doanh số từng bác sĩ từ bảng bac_si, ho_so_kham, hoa_don
     */
    public List<Map<String, Object>> getNangSuatBacSi(String role) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT bs.ma_bac_si, nd.ho_ten AS ten_bac_si, bs.chuyen_khoa, bs.phong_kham, " +
                     "       (SELECT COUNT(DISTINCT hs1.ma_ho_so) " +
                     "        FROM ho_so_kham hs1 " +
                     "        WHERE hs1.ma_bac_si = bs.ma_bac_si) AS so_ca_kham, " +
                     "       (SELECT ISNULL(SUM(hd1.tong_tien), 0) " +
                     "        FROM hoa_don hd1 " +
                     "        JOIN ho_so_kham hs2 ON hd1.ma_ho_so = hs2.ma_ho_so " +
                     "        WHERE hs2.ma_bac_si = bs.ma_bac_si) AS tong_doanh_thu " +
                     "FROM bac_si bs " +
                     "JOIN nguoi_dung nd ON bs.ma_bac_si = nd.ma_nguoi_dung " +
                     "WHERE nd.hoat_dong = 1 " +
                     "ORDER BY tong_doanh_thu DESC, so_ca_kham DESC";

        try (Connection conn = getConnection(role);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                String maBacSi = rs.getString("ma_bac_si");
                map.put("maBacSi", maBacSi);
                map.put("maBacSiCode", maBacSi);
                map.put("tenBacSi", rs.getString("ten_bac_si"));
                map.put("chuyenKhoa", rs.getString("chuyen_khoa"));
                map.put("phongKham", rs.getString("phong_kham"));
                map.put("soCaKham", rs.getInt("so_ca_kham"));
                map.put("tongDoanhThu", rs.getBigDecimal("tong_doanh_thu"));
                map.put("tongTienKham", rs.getBigDecimal("tong_doanh_thu"));
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

        try (Connection conn = getConnection(role);
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setDate(1, tuNgay);
            cs.setDate(2, denNgay);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ngayGiaoDich", rs.getDate("ngay_giao_dich"));
                    map.put("ngayKham", rs.getDate("ngay_giao_dich"));
                    map.put("hinhThucThanhToan", rs.getString("hinh_thuc_thanh_toan"));
                    map.put("soHoaDon", rs.getInt("so_hoa_don"));
                    map.put("soLuotKham", rs.getInt("so_hoa_don"));
                    map.put("doanhThuDichVu", rs.getBigDecimal("doanh_thu_dich_vu"));
                    map.put("doanhThuThuoc", rs.getBigDecimal("doanh_thu_thuoc"));
                    map.put("tongGiamGia", rs.getBigDecimal("tong_giam_gia"));
                    map.put("thucThu", rs.getBigDecimal("thuc_thu"));
                    map.put("tongDoanhThu", rs.getBigDecimal("thuc_thu"));
                    map.put("tongTienThu", rs.getBigDecimal("thuc_thu"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * Tra cứu danh sách toàn bộ hóa đơn viện phí cho Quản trị viên
     */
    public List<Map<String, Object>> getDanhSachHoaDon(String role, String keyword) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT hd.ma_hoa_don, hd.ma_ho_so, hd.tien_dich_vu, hd.tien_thuoc, hd.tien_giam_gia, hd.tong_tien, " +
            "       hd.hinh_thuc_thanh_toan, hd.trang_thai, hd.ngay_lap, " +
            "       bn.ma_benh_nhan, bn.ho_ten AS ten_benh_nhan, bn.so_dien_thoai, bn.so_cccd, " +
            "       bs_user.ho_ten AS ten_bac_si, bs.phong_kham " +
            "FROM hoa_don hd " +
            "JOIN ho_so_kham hs ON hd.ma_ho_so = hs.ma_ho_so " +
            "JOIN benh_nhan bn ON hs.ma_benh_nhan = bn.ma_benh_nhan " +
            "JOIN bac_si bs ON hs.ma_bac_si = bs.ma_bac_si " +
            "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (hd.ma_hoa_don LIKE ? OR bn.ho_ten LIKE ? OR bn.so_dien_thoai LIKE ? OR bs_user.ho_ten LIKE ?) ");
            String p = "%" + keyword.trim() + "%";
            params.add(p);
            params.add(p);
            params.add(p);
            params.add(p);
        }

        sql.append(" ORDER BY hd.ngay_lap DESC ");

        try (Connection conn = getConnection(role);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maHoaDon", rs.getString("ma_hoa_don"));
                    map.put("maHoSo", rs.getString("ma_ho_so"));
                    map.put("tienDichVu", rs.getBigDecimal("tien_dich_vu"));
                    map.put("tienThuoc", rs.getBigDecimal("tien_thuoc"));
                    map.put("tienGiamGia", rs.getBigDecimal("tien_giam_gia"));
                    map.put("tongTien", rs.getBigDecimal("tong_tien"));
                    map.put("hinhThucThanhToan", rs.getString("hinh_thuc_thanh_toan"));
                    map.put("trangThai", rs.getString("trang_thai"));
                    map.put("ngayLap", rs.getTimestamp("ngay_lap"));
                    map.put("maBenhNhan", rs.getString("ma_benh_nhan"));
                    map.put("tenBenhNhan", rs.getString("ten_benh_nhan"));
                    map.put("soDienThoai", rs.getString("so_dien_thoai"));
                    map.put("soCccd", rs.getString("so_cccd"));
                    map.put("tenBacSi", rs.getString("ten_bac_si"));
                    map.put("phongKham", rs.getString("phong_kham"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * Tra cứu chi tiết một hóa đơn (kèm danh sách dịch vụ và đơn thuốc đã kê)
     */
    public Map<String, Object> getChiTietHoaDon(String role, String maHoaDon) throws SQLException {
        Map<String, Object> detail = new HashMap<>();
        String headerSql = 
            "SELECT hd.ma_hoa_don, hd.ma_ho_so, hd.tien_dich_vu, hd.tien_thuoc, hd.tien_giam_gia, hd.tong_tien, " +
            "       hd.hinh_thuc_thanh_toan, hd.trang_thai, hd.ngay_lap, " +
            "       bn.ma_benh_nhan, bn.ho_ten AS ten_benh_nhan, bn.so_dien_thoai, bn.dia_chi, bn.ngay_sinh, bn.gioi_tinh, " +
            "       hs.chan_doan, hs.ngay_kham, bs_user.ho_ten AS ten_bac_si, bs.phong_kham, bs.chuyen_khoa " +
            "FROM hoa_don hd " +
            "JOIN ho_so_kham hs ON hd.ma_ho_so = hs.ma_ho_so " +
            "JOIN benh_nhan bn ON hs.ma_benh_nhan = bn.ma_benh_nhan " +
            "JOIN bac_si bs ON hs.ma_bac_si = bs.ma_bac_si " +
            "JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung " +
            "WHERE hd.ma_hoa_don = ?";

        String maHoSo = null;
        try (Connection conn = getConnection(role);
             PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    detail.put("maHoaDon", rs.getString("ma_hoa_don"));
                    maHoSo = rs.getString("ma_ho_so");
                    detail.put("maHoSo", maHoSo);
                    detail.put("tienDichVu", rs.getBigDecimal("tien_dich_vu"));
                    detail.put("tienThuoc", rs.getBigDecimal("tien_thuoc"));
                    detail.put("tienGiamGia", rs.getBigDecimal("tien_giam_gia"));
                    detail.put("tongTien", rs.getBigDecimal("tong_tien"));
                    detail.put("hinhThucThanhToan", rs.getString("hinh_thuc_thanh_toan"));
                    detail.put("trangThai", rs.getString("trang_thai"));
                    detail.put("ngayLap", rs.getTimestamp("ngay_lap"));
                    detail.put("maBenhNhan", rs.getString("ma_benh_nhan"));
                    detail.put("tenBenhNhan", rs.getString("ten_benh_nhan"));
                    detail.put("soDienThoai", rs.getString("so_dien_thoai"));
                    detail.put("diaChi", rs.getString("dia_chi"));
                    detail.put("ngaySinh", rs.getDate("ngay_sinh"));
                    detail.put("gioiTinh", rs.getString("gioi_tinh"));
                    detail.put("chanDoan", rs.getString("chan_doan"));
                    detail.put("ngayKham", rs.getTimestamp("ngay_kham"));
                    detail.put("tenBacSi", rs.getString("ten_bac_si"));
                    detail.put("phongKham", rs.getString("phong_kham"));
                    detail.put("chuyenKhoa", rs.getString("chuyen_khoa"));
                }
            }

            if (maHoSo != null) {
                // Lấy danh sách dịch vụ
                List<Map<String, Object>> dsDichVu = new ArrayList<>();
                String dvSql = "SELECT cd.ma_chi_dinh, dv.ten_dich_vu, cd.so_luong, cd.don_gia, (cd.so_luong * cd.don_gia) AS thanh_tien " +
                               "FROM chi_dinh_dich_vu cd JOIN dich_vu dv ON cd.ma_dich_vu = dv.ma_dich_vu " +
                               "WHERE cd.ma_ho_so = ?";
                try (PreparedStatement psDv = conn.prepareStatement(dvSql)) {
                    psDv.setString(1, maHoSo);
                    try (ResultSet rsDv = psDv.executeQuery()) {
                        while (rsDv.next()) {
                            Map<String, Object> m = new HashMap<>();
                            m.put("maChiDinh", rsDv.getString("ma_chi_dinh"));
                            m.put("tenDichVu", rsDv.getString("ten_dich_vu"));
                            m.put("soLuong", rsDv.getInt("so_luong"));
                            m.put("donGia", rsDv.getBigDecimal("don_gia"));
                            m.put("thanhTien", rsDv.getBigDecimal("thanh_tien"));
                            dsDichVu.add(m);
                        }
                    }
                }
                detail.put("danhSachDichVu", dsDichVu);

                // Lấy danh sách thuốc
                List<Map<String, Object>> dsThuoc = new ArrayList<>();
                String thSql = "SELECT dt.ma_don_thuoc, t.ten_thuoc, t.don_vi_tinh, dt.so_luong, t.don_gia, (dt.so_luong * t.don_gia) AS thanh_tien, " +
                               "       dt.cach_dung, dt.huong_dan " +
                               "FROM don_thuoc dt JOIN thuoc t ON dt.ma_thuoc = t.ma_thuoc " +
                               "WHERE dt.ma_ho_so = ?";
                try (PreparedStatement psTh = conn.prepareStatement(thSql)) {
                    psTh.setString(1, maHoSo);
                    try (ResultSet rsTh = psTh.executeQuery()) {
                        while (rsTh.next()) {
                            Map<String, Object> m = new HashMap<>();
                            m.put("maDonThuoc", rsTh.getString("ma_don_thuoc"));
                            m.put("tenThuoc", rsTh.getString("ten_thuoc"));
                            m.put("donViTinh", rsTh.getString("don_vi_tinh"));
                            m.put("soLuong", rsTh.getInt("so_luong"));
                            m.put("donGia", rsTh.getBigDecimal("don_gia"));
                            m.put("thanhTien", rsTh.getBigDecimal("thanh_tien"));
                            m.put("cachDung", rsTh.getString("cach_dung"));
                            m.put("huongDan", rsTh.getString("huong_dan"));
                            dsThuoc.add(m);
                        }
                    }
                }
                detail.put("danhSachThuoc", dsThuoc);
            }
        }
        return detail;
    }

    /**
     * Tra cứu danh sách toàn bộ người dùng & bác sĩ (Hỗ trợ lọc vai trò và tìm kiếm)
     */
    public List<Map<String, Object>> getDanhSachNguoiDung(String role, String vaiTroFilter, String keyword) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT nd.ma_nguoi_dung, nd.ten_dang_nhap, nd.ho_ten, nd.so_dien_thoai, " +
            "       nd.vai_tro, nd.hoat_dong, nd.ngay_tao, " +
            "       bs.chuyen_khoa, bs.so_chung_chi, bs.phong_kham " +
            "FROM nguoi_dung nd " +
            "LEFT JOIN bac_si bs ON nd.ma_nguoi_dung = bs.ma_bac_si " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (vaiTroFilter != null && !vaiTroFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(vaiTroFilter.trim())) {
            sql.append(" AND UPPER(nd.vai_tro) = ? ");
            params.add(vaiTroFilter.trim().toUpperCase());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (nd.ho_ten LIKE ? OR nd.ten_dang_nhap LIKE ? OR nd.so_dien_thoai LIKE ? OR bs.chuyen_khoa LIKE ? OR nd.ma_nguoi_dung LIKE ?) ");
            String kwPattern = "%" + keyword.trim() + "%";
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
        }

        sql.append(" ORDER BY nd.ngay_tao DESC, nd.ma_nguoi_dung DESC ");

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maNguoiDung", rs.getString("ma_nguoi_dung"));
                    map.put("tenDangNhap", rs.getString("ten_dang_nhap"));
                    map.put("hoTen", rs.getString("ho_ten"));
                    map.put("soDienThoai", rs.getString("so_dien_thoai"));
                    map.put("vaiTro", rs.getString("vai_tro"));
                    map.put("hoatDong", rs.getBoolean("hoat_dong"));
                    map.put("ngayTao", rs.getTimestamp("ngay_tao"));
                    map.put("chuyenKhoa", rs.getString("chuyen_khoa"));
                    map.put("soChungChi", rs.getString("so_chung_chi"));
                    map.put("phongKham", rs.getString("phong_kham"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * Thêm mới nhân sự (Bác sĩ, Lễ tân, Dược sĩ, Quản trị viên) với Transaction và BCrypt
     */
    public Map<String, Object> themNguoiDung(String role, String tenDangNhap, String matKhau, String hoTen,
                                            String soDienThoai, String vaiTro, String chuyenKhoa,
                                            String soChungChi, String phongKham) throws SQLException {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        if (matKhau == null || matKhau.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu khởi tạo không được để trống!");
        }
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống!");
        }
        if (vaiTro == null || vaiTro.trim().isEmpty()) {
            throw new IllegalArgumentException("Vai trò nhân viên không được để trống!");
        }

        String finalUsername = tenDangNhap.trim();
        String finalName = hoTen.trim();
        String finalPhone = soDienThoai != null ? soDienThoai.trim() : "";
        String finalRole = vaiTro.trim().toUpperCase();

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            // Kiểm tra trùng username
            String checkSql = "SELECT COUNT(*) FROM nguoi_dung WHERE ten_dang_nhap = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, finalUsername);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalArgumentException("Tên đăng nhập '" + finalUsername + "' đã tồn tại trong hệ thống!");
                    }
                }
            }

            conn.setAutoCommit(false);
            try {
                String maNguoiDung;
                if ("BAC_SI".equals(finalRole)) {
                    // Mã BSxxx
                    String maxSql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(ma_nguoi_dung, 3, 10) AS INT)), 0) FROM nguoi_dung WHERE ma_nguoi_dung LIKE 'BS%'";
                    int nextNum = 1;
                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(maxSql)) {
                        if (rs.next()) {
                            nextNum = rs.getInt(1) + 1;
                        }
                    }
                    maNguoiDung = String.format("BS%03d", nextNum);
                } else {
                    // Mã NDxxx
                    String maxSql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(ma_nguoi_dung, 3, 10) AS INT)), 0) FROM nguoi_dung WHERE ma_nguoi_dung LIKE 'ND%'";
                    int nextNum = 1;
                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(maxSql)) {
                        if (rs.next()) {
                            nextNum = rs.getInt(1) + 1;
                        }
                    }
                    maNguoiDung = String.format("ND%03d", nextNum);
                }

                String matKhauHash = com.mycompany.quanlyphongkham.util.PasswordUtil.hashPassword(matKhau.trim());

                String insertUserSql = "INSERT INTO nguoi_dung (ma_nguoi_dung, ten_dang_nhap, mat_khau_hash, ho_ten, so_dien_thoai, vai_tro, hoat_dong) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, 1)";
                try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                    ps.setString(1, maNguoiDung);
                    ps.setString(2, finalUsername);
                    ps.setString(3, matKhauHash);
                    ps.setString(4, finalName);
                    ps.setString(5, finalPhone);
                    ps.setString(6, finalRole);
                    ps.executeUpdate();
                }

                if ("BAC_SI".equals(finalRole)) {
                    String finalChuyenKhoa = (chuyenKhoa != null && !chuyenKhoa.trim().isEmpty()) ? chuyenKhoa.trim() : "Đa khoa";
                    String finalPhongKham = (phongKham != null && !phongKham.trim().isEmpty()) ? phongKham.trim() : "P.101";
                    String finalCchn = (soChungChi != null && !soChungChi.trim().isEmpty()) ? soChungChi.trim() : ("CCHN-" + maNguoiDung);

                    String insertDocSql = "INSERT INTO bac_si (ma_bac_si, chuyen_khoa, so_chung_chi, phong_kham) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psDoc = conn.prepareStatement(insertDocSql)) {
                        psDoc.setString(1, maNguoiDung);
                        psDoc.setString(2, finalChuyenKhoa);
                        psDoc.setString(3, finalCchn);
                        psDoc.setString(4, finalPhongKham);
                        psDoc.executeUpdate();
                    }
                }

                conn.commit();

                Map<String, Object> result = new HashMap<>();
                result.put("maNguoiDung", maNguoiDung);
                result.put("tenDangNhap", finalUsername);
                result.put("hoTen", finalName);
                result.put("vaiTro", finalRole);
                result.put("soDienThoai", finalPhone);
                return result;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Cập nhật thông tin nhân viên & chuyên môn bác sĩ
     */
    public void capNhatNguoiDung(String role, String maNguoiDung, String hoTen, String soDienThoai,
                                 String vaiTro, String chuyenKhoa, String soChungChi, String phongKham) throws SQLException {
        if (maNguoiDung == null || maNguoiDung.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ!");
        }
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống!");
        }

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            conn.setAutoCommit(false);
            try {
                String updateUserSql = "UPDATE nguoi_dung SET ho_ten = ?, so_dien_thoai = ? WHERE ma_nguoi_dung = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateUserSql)) {
                    ps.setString(1, hoTen.trim());
                    ps.setString(2, soDienThoai != null ? soDienThoai.trim() : "");
                    ps.setString(3, maNguoiDung.trim());
                    ps.executeUpdate();
                }

                if ("BAC_SI".equalsIgnoreCase(vaiTro) || maNguoiDung.startsWith("BS")) {
                    String finalChuyenKhoa = (chuyenKhoa != null && !chuyenKhoa.trim().isEmpty()) ? chuyenKhoa.trim() : "Đa khoa";
                    String finalPhongKham = (phongKham != null && !phongKham.trim().isEmpty()) ? phongKham.trim() : "P.101";

                    String checkDocSql = "SELECT COUNT(*) FROM bac_si WHERE ma_bac_si = ?";
                    boolean exists = false;
                    try (PreparedStatement psCheck = conn.prepareStatement(checkDocSql)) {
                        psCheck.setString(1, maNguoiDung.trim());
                        try (ResultSet rs = psCheck.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) exists = true;
                        }
                    }

                    if (exists) {
                        String updateDocSql = "UPDATE bac_si SET chuyen_khoa = ?, phong_kham = ?, so_chung_chi = ISNULL(?, so_chung_chi) WHERE ma_bac_si = ?";
                        try (PreparedStatement psDoc = conn.prepareStatement(updateDocSql)) {
                            psDoc.setString(1, finalChuyenKhoa);
                            psDoc.setString(2, finalPhongKham);
                            psDoc.setString(3, (soChungChi != null && !soChungChi.trim().isEmpty()) ? soChungChi.trim() : null);
                            psDoc.setString(4, maNguoiDung.trim());
                            psDoc.executeUpdate();
                        }
                    } else {
                        String finalCchn = (soChungChi != null && !soChungChi.trim().isEmpty()) ? soChungChi.trim() : ("CCHN-" + maNguoiDung);
                        String insertDocSql = "INSERT INTO bac_si (ma_bac_si, chuyen_khoa, so_chung_chi, phong_kham) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement psDoc = conn.prepareStatement(insertDocSql)) {
                            psDoc.setString(1, maNguoiDung.trim());
                            psDoc.setString(2, finalChuyenKhoa);
                            psDoc.setString(3, finalCchn);
                            psDoc.setString(4, finalPhongKham);
                            psDoc.executeUpdate();
                        }
                    }
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Khóa hoặc mở khóa trạng thái tài khoản nhân sự
     */
    public void doiTrangThaiNguoiDung(String role, String maNguoiDung, boolean hoatDong) throws SQLException {
        if ("ND001".equalsIgnoreCase(maNguoiDung)) {
            throw new IllegalArgumentException("Không được phép khóa tài khoản Quản trị viên hệ thống gốc (ND001)!");
        }
        String sql = "UPDATE nguoi_dung SET hoat_dong = ? WHERE ma_nguoi_dung = ?";
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, hoatDong);
            ps.setString(2, maNguoiDung.trim());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Không tìm thấy người dùng có mã: " + maNguoiDung);
            }
        }
    }

    /**
     * Đặt lại mật khẩu (Reset password)
     */
    public void datLaiMatKhau(String role, String maNguoiDung, String matKhauMoi) throws SQLException {
        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống!");
        }
        String hashed = com.mycompany.quanlyphongkham.util.PasswordUtil.hashPassword(matKhauMoi.trim());
        String sql = "UPDATE nguoi_dung SET mat_khau_hash = ? WHERE ma_nguoi_dung = ?";
        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashed);
            ps.setString(2, maNguoiDung.trim());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Không tìm thấy người dùng có mã: " + maNguoiDung);
            }
        }
    }

    private static volatile boolean doctorPermsUpdated = false;

    private static synchronized void ensureDoctorPermissions() {
        if (doctorPermsUpdated) return;
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("GRANT SELECT ON bac_si TO vai_tro_le_tan");
                stmt.execute("GRANT SELECT ON nguoi_dung TO vai_tro_le_tan");
                stmt.execute("GRANT SELECT ON bac_si TO nguoi_dung_xac_thuc");
                stmt.execute("GRANT SELECT ON nguoi_dung TO nguoi_dung_xac_thuc");
                stmt.execute("GRANT SELECT ON bac_si TO vai_tro_bac_si");
                stmt.execute("GRANT SELECT ON bac_si TO vai_tro_duoc_si");
                stmt.execute("GRANT SELECT ON v_doanh_thu_theo_ngay TO vai_tro_quan_tri");
                stmt.execute("GRANT EXECUTE ON sp_thong_ke_doanh_thu_khoang_ngay TO vai_tro_quan_tri");
                stmt.execute("GRANT SELECT ON hoa_don TO vai_tro_quan_tri");
                stmt.execute("GRANT SELECT ON ho_so_kham TO vai_tro_quan_tri");
                stmt.execute("GRANT SELECT ON chi_dinh_dich_vu TO vai_tro_quan_tri");
                stmt.execute("GRANT SELECT ON don_thuoc TO vai_tro_quan_tri");
                stmt.execute("GRANT SELECT ON dich_vu TO vai_tro_quan_tri");
                stmt.execute("GRANT SELECT ON thuoc TO vai_tro_quan_tri");
                stmt.execute("GRANT SELECT ON benh_nhan TO vai_tro_quan_tri");
            }
            doctorPermsUpdated = true;
        } catch (Exception e) {
            System.err.println("[AdminDAO] Cảnh báo cập nhật quyền: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách toàn bộ bác sĩ đang hoạt động (dùng cho tiếp nhận khám bệnh và phân bổ)
     */
    public List<Map<String, Object>> getDanhSachBacSiHoatDong(String role) throws SQLException {
        ensureDoctorPermissions();
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT bs.ma_bac_si, nd.ho_ten, bs.chuyen_khoa, bs.phong_kham, bs.so_chung_chi " +
                     "FROM bac_si bs " +
                     "JOIN nguoi_dung nd ON bs.ma_bac_si = nd.ma_nguoi_dung " +
                     "WHERE nd.hoat_dong = 1 " +
                     "ORDER BY nd.ho_ten ASC";

        Connection conn = null;
        try {
            if (role != null && !role.trim().isEmpty()) {
                conn = DBContext.getConnectionByRole(role);
            } else {
                conn = DBContext.getConnectionByRole("QUAN_TRI");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maBacSi", rs.getString("ma_bac_si"));
                    map.put("hoTen", rs.getString("ho_ten"));
                    map.put("chuyenKhoa", rs.getString("chuyen_khoa"));
                    map.put("phongKham", rs.getString("phong_kham"));
                    map.put("soChungChi", rs.getString("so_chung_chi"));
                    list.add(map);
                }
            }
        } catch (SQLException ex) {
            // Fallback to QUAN_TRI if role connection failed due to permission
            if (ex.getErrorCode() == 229 || (ex.getMessage() != null && ex.getMessage().contains("permission was denied"))) {
                try (Connection adminConn = DBContext.getConnectionByRole("QUAN_TRI");
                     PreparedStatement ps = adminConn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    list.clear();
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("maBacSi", rs.getString("ma_bac_si"));
                        map.put("hoTen", rs.getString("ho_ten"));
                        map.put("chuyenKhoa", rs.getString("chuyen_khoa"));
                        map.put("phongKham", rs.getString("phong_kham"));
                        map.put("soChungChi", rs.getString("so_chung_chi"));
                        list.add(map);
                    }
                }
            } else {
                throw ex;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
        return list;
    }

    public List<Map<String, Object>> getDanhSachBacSiHoatDong() throws SQLException {
        return getDanhSachBacSiHoatDong(null);
    }
}
