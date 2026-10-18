package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.math.BigDecimal;
import java.util.Date;

public class HoaDon {
    private String maHoaDon;
    private String maHoaDonCode;
    private String maHoSo;
    private String maHoSoCode;
    private BigDecimal tienDichVu;
    private BigDecimal tienThuoc;
    private BigDecimal tienGiamGia;
    private BigDecimal tongTien;
    private String hinhThucThanhToan;
    private String trangThai;
    private Date ngayLap;

    public HoaDon() {}

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
        this.maHoaDonCode = FormatUtil.normalizeHoaDon(maHoaDon);
    }

    public String getMaHoaDonCode() {
        if (maHoaDonCode == null && maHoaDon != null) {
            maHoaDonCode = FormatUtil.normalizeHoaDon(maHoaDon);
        }
        return maHoaDonCode;
    }

    public void setMaHoaDonCode(String maHoaDonCode) {
        this.maHoaDonCode = maHoaDonCode;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        this.maHoSo = maHoSo;
        this.maHoSoCode = FormatUtil.normalizeHoSo(maHoSo);
    }

    public String getMaHoSoCode() {
        if (maHoSoCode == null && maHoSo != null) {
            maHoSoCode = FormatUtil.normalizeHoSo(maHoSo);
        }
        return maHoSoCode;
    }

    public void setMaHoSoCode(String maHoSoCode) {
        this.maHoSoCode = maHoSoCode;
    }

    public BigDecimal getTienDichVu() {
        return tienDichVu;
    }

    public void setTienDichVu(BigDecimal tienDichVu) {
        this.tienDichVu = tienDichVu;
    }

    public BigDecimal getTienThuoc() {
        return tienThuoc;
    }

    public void setTienThuoc(BigDecimal tienThuoc) {
        this.tienThuoc = tienThuoc;
    }

    public BigDecimal getTienGiamGia() {
        return tienGiamGia;
    }

    public void setTienGiamGia(BigDecimal tienGiamGia) {
        this.tienGiamGia = tienGiamGia;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Date getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Date ngayLap) {
        this.ngayLap = ngayLap;
    }
}
