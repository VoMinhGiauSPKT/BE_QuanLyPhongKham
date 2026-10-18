package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.math.BigDecimal;

public class DonThuoc {
    private String maDonThuoc;
    private String maHoSo;
    private String maHoSoCode;
    private String maThuoc;
    private String maThuocCode;
    private String tenThuoc;
    private Integer soLuong;
    private BigDecimal donGia;
    private String donViTinh;
    private String cachDung;
    private String huongDan;

    public DonThuoc() {}

    public String getMaDonThuoc() {
        return maDonThuoc;
    }

    public void setMaDonThuoc(String maDonThuoc) {
        this.maDonThuoc = maDonThuoc;
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

    public String getMaThuoc() {
        return maThuoc;
    }

    public void setMaThuoc(String maThuoc) {
        this.maThuoc = maThuoc;
        this.maThuocCode = FormatUtil.normalizeThuoc(maThuoc);
    }

    public String getMaThuocCode() {
        if (maThuocCode == null && maThuoc != null) {
            maThuocCode = FormatUtil.normalizeThuoc(maThuoc);
        }
        return maThuocCode;
    }

    public void setMaThuocCode(String maThuocCode) {
        this.maThuocCode = maThuocCode;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
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

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getCachDung() {
        return cachDung;
    }

    public void setCachDung(String cachDung) {
        this.cachDung = cachDung;
    }

    public String getHuongDan() {
        return huongDan;
    }

    public void setHuongDan(String huongDan) {
        this.huongDan = huongDan;
    }
}
