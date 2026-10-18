package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.math.BigDecimal;

public class DichVu {
    private String maDichVu;
    private String maDichVuCode;
    private String tenDichVu;
    private BigDecimal donGia;
    private String moTa;
    private Boolean trangThai;

    public DichVu() {}

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

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
