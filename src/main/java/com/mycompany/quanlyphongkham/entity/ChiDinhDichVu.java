package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.math.BigDecimal;

public class ChiDinhDichVu {
    private String maChiDinh;
    private String maChiDinhCode;
    private String maHoSo;
    private String maHoSoCode;
    private String maDichVu;
    private String maDichVuCode;
    private String tenDichVu;
    private Integer soLuong;
    private BigDecimal donGia;
    private String trangThai;
    private String ketQua;

    public ChiDinhDichVu() {}

    public String getMaChiDinh() {
        return maChiDinh;
    }

    public void setMaChiDinh(String maChiDinh) {
        this.maChiDinh = maChiDinh;
        this.maChiDinhCode = FormatUtil.normalizeChiDinh(maChiDinh);
    }

    public String getMaChiDinhCode() {
        if (maChiDinhCode == null && maChiDinh != null) {
            maChiDinhCode = FormatUtil.normalizeChiDinh(maChiDinh);
        }
        return maChiDinhCode;
    }

    public void setMaChiDinhCode(String maChiDinhCode) {
        this.maChiDinhCode = maChiDinhCode;
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

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
        this.maDichVuCode = FormatUtil.normalizeDichVu(maDichVu);
    }

    public String getMaDichVuCode() {
        if (maDichVuCode == null && maDichVu != null) {
            maDichVuCode = FormatUtil.normalizeDichVu(maDichVu);
        }
        return maDichVuCode;
    }

    public void setMaDichVuCode(String maDichVuCode) {
        this.maDichVuCode = maDichVuCode;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getKetQua() {
        return ketQua;
    }

    public void setKetQua(String ketQua) {
        this.ketQua = ketQua;
    }
}
