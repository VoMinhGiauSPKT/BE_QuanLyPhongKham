package com.mycompany.quanlyphongkham.module.duocsi;

import com.mycompany.quanlyphongkham.common.PageResponse;
import com.mycompany.quanlyphongkham.entity.Thuoc;
import com.mycompany.quanlyphongkham.util.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Xem toàn bộ danh mục kho thuốc có phân trang, tìm kiếm và lọc trạng thái
     */
    public PageResponse<Thuoc> getToanBoKhoThuoc(String role, String keyword, Boolean statusFilter, int page, int size) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        int offset = (page - 1) * size;

        List<Thuoc> list = new ArrayList<>();
        long totalElements = 0;

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            whereClause.append(" AND (ten_thuoc LIKE ? OR ma_thuoc LIKE ?) ");
            String searchPattern = "%" + keyword.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (statusFilter != null) {
            whereClause.append(" AND trang_thai = ? ");
            params.add(statusFilter);
        }

        String countSql = "SELECT COUNT(*) FROM thuoc " + whereClause;
        String dataSql = "SELECT ma_thuoc, ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung, trang_thai " +
                         "FROM thuoc " + whereClause +
                         "ORDER BY ma_thuoc ASC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                for (int i = 0; i < params.size(); i++) {
                    psCount.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next()) {
                        totalElements = rs.getLong(1);
                    }
                }
            }

            if (totalElements > 0) {
                try (PreparedStatement psData = conn.prepareStatement(dataSql)) {
                    int pIndex = 1;
                    for (Object p : params) {
                        psData.setObject(pIndex++, p);
                    }
                    psData.setInt(pIndex++, offset);
                    psData.setInt(pIndex, size);

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
     * Tương thích phiên bản cũ: Không lọc tìm kiếm
     */
    public PageResponse<Thuoc> getToanBoKhoThuoc(String role, int page, int size) throws SQLException {
        return getToanBoKhoThuoc(role, null, null, page, size);
    }

    /**
     * Thêm mới một loại thuốc vào kho (Create Drug)
     */
    public String themThuocMoi(String role, String tenThuoc, String donViTinh, int soLuongTon, BigDecimal donGia, java.sql.Date hanSuDung) throws SQLException {
        String sql = "DECLARE @out TABLE (id VARCHAR(20)); " +
                     "INSERT INTO thuoc (ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung, trang_thai) " +
                     "OUTPUT INSERTED.ma_thuoc INTO @out " +
                     "VALUES (?, ?, ?, ?, ?, 1); " +
                     "SELECT id FROM @out;";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenThuoc.trim());
            ps.setString(2, donViTinh.trim());
            ps.setInt(3, Math.max(0, soLuongTon));
            ps.setBigDecimal(4, donGia);
            ps.setDate(5, hanSuDung);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    /**
     * Chỉnh sửa thông tin thuốc (Update Drug) - Khi đổi đơn giá sẽ kích hoạt trigger trg_ghi_nhat_ky_gia_thuoc
     */
    public boolean capNhatThuoc(String role, String maThuoc, String tenThuoc, String donViTinh, BigDecimal donGia, java.sql.Date hanSuDung, Boolean trangThai) throws SQLException {
        String sql = "UPDATE thuoc SET " +
                     "ten_thuoc = ISNULL(?, ten_thuoc), " +
                     "don_vi_tinh = ISNULL(?, don_vi_tinh), " +
                     "don_gia = ISNULL(?, don_gia), " +
                     "han_su_dung = ISNULL(?, han_su_dung), " +
                     "trang_thai = ISNULL(?, trang_thai) " +
                     "WHERE ma_thuoc = ?;";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenThuoc != null ? tenThuoc.trim() : null);
            ps.setString(2, donViTinh != null ? donViTinh.trim() : null);
            if (donGia != null) {
                ps.setBigDecimal(3, donGia);
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (hanSuDung != null) {
                ps.setDate(4, hanSuDung);
            } else {
                ps.setNull(4, Types.DATE);
            }
            if (trangThai != null) {
                ps.setBoolean(5, trangThai);
            } else {
                ps.setNull(5, Types.BIT);
            }
            ps.setString(6, maThuoc);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa thuốc khỏi kho:
     * - Nếu thuốc đã từng kê đơn: Soft delete (trang_thai = 0) để bảo toàn dữ liệu bệnh án cũ
     * - Nếu thuốc chưa từng kê đơn: Hard delete (DELETE FROM thuoc)
     */
    public Map<String, Object> xoaHoacKhoaThuoc(String role, String maThuoc) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        String checkSql = "SELECT COUNT(*) FROM don_thuoc WHERE ma_thuoc = ?";

        try (Connection conn = DBContext.getConnectionByRole(role)) {
            boolean daKeDon = false;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, maThuoc);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        daKeDon = true;
                    }
                }
            }

            if (daKeDon) {
                String updateSql = "UPDATE thuoc SET trang_thai = 0 WHERE ma_thuoc = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setString(1, maThuoc);
                    psUpdate.executeUpdate();
                }
                result.put("action", "DEACTIVATED");
                result.put("message", "Thuốc đã từng được kê trong đơn thuốc. Hệ thống đã chuyển sang 'Ngừng kinh doanh' để bảo toàn dữ liệu lịch sử.");
            } else {
                String deleteSql = "DELETE FROM thuoc WHERE ma_thuoc = ?";
                try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                    psDel.setString(1, maThuoc);
                    psDel.executeUpdate();
                }
                result.put("action", "DELETED");
                result.put("message", "Đã xóa hoàn toàn thuốc khỏi danh mục.");
            }
        }
        return result;
    }

    /**
     * Tra cứu nhật ký biến động giá thuốc do Trigger trg_ghi_nhat_ky_gia_thuoc tự động ghi
     */
    public List<Map<String, Object>> getNhatKyGiaThuoc(String role, String maThuoc) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT ma_nhat_ky, ma_thuoc, gia_cu, gia_moi, thoi_gian, nguoi_thuc_hien " +
                     "FROM nhat_ky_gia_thuoc WHERE ma_thuoc = ? ORDER BY thoi_gian DESC";

        try (Connection conn = DBContext.getConnectionByRole(role);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maNhatKy", rs.getInt("ma_nhat_ky"));
                    map.put("maThuoc", rs.getString("ma_thuoc"));
                    map.put("giaCu", rs.getBigDecimal("gia_cu"));
                    map.put("giaMoi", rs.getBigDecimal("gia_moi"));
                    map.put("thoiGian", rs.getTimestamp("thoi_gian"));
                    map.put("nguoiThucHien", rs.getString("nguoi_thuc_hien"));
                    list.add(map);
                }
            }
        }
        return list;
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

    /**
     * Transaction 6: Nhập kho nhiều loại thuốc đồng thời (Atomic Batch Restock - Recovery Data)
     * Gọi thủ tục sp_gd_nhap_nhieu_thuoc trong CSDL
     */
    public void nhapNhieuThuoc(String role, String danhSachJson) throws SQLException {
        String sql = "{call dbo.sp_gd_nhap_nhieu_thuoc(?)}";
        try (Connection conn = DBContext.getConnectionByRole(role);
             java.sql.CallableStatement cs = conn.prepareCall(sql)) {
            cs.setNString(1, danhSachJson);
            cs.execute();
        }
    }
}

