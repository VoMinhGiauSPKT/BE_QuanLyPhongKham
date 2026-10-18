package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.math.BigDecimal;
import java.util.Date;

public class Thuoc {
    private String maThuoc;
    private String maThuocCode;
    private String tenThuoc;
    private String donViTinh;
    private Integer soLuongTon;
    private BigDecimal donGia;
    private Date hanSuDung;
    private Boolean trangThai;
    private Integer soNgayConHan;
    private String trangThaiCanhBao;

    public Thuoc() {}

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

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public Date getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(Date hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getSoNgayConHan() {
        return soNgayConHan;
    }

    public void setSoNgayConHan(Integer soNgayConHan) {
        this.soNgayConHan = soNgayConHan;
    }

    public String getTrangThaiCanhBao() {
        return trangThaiCanhBao;
    }

    public void setTrangThaiCanhBao(String trangThaiCanhBao) {
        this.trangThaiCanhBao = trangThaiCanhBao;
    }
}
