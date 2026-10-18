package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;

public class BacSi {
    private String maBacSi;
    private String maBacSiCode;
    private String hoTen;
    private String chuyenKhoa;
    private String soChungChi;
    private String phongKham;

    public BacSi() {}

    public String getMaBacSi() {
        return maBacSi;
    }

    public void setMaBacSi(String maBacSi) {
        this.maBacSi = maBacSi;
        this.maBacSiCode = FormatUtil.normalizeBacSi(maBacSi);
    }

    public String getMaBacSiCode() {
        if (maBacSiCode == null && maBacSi != null) {
            maBacSiCode = FormatUtil.normalizeBacSi(maBacSi);
        }
        return maBacSiCode;
    }

    public void setMaBacSiCode(String maBacSiCode) {
        this.maBacSiCode = maBacSiCode;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getChuyenKhoa() {
        return chuyenKhoa;
    }

    public void setChuyenKhoa(String chuyenKhoa) {
        this.chuyenKhoa = chuyenKhoa;
    }

    public String getSoChungChi() {
        return soChungChi;
    }

    public void setSoChungChi(String soChungChi) {
        this.soChungChi = soChungChi;
    }

    public String getPhongKham() {
        return phongKham;
    }

    public void setPhongKham(String phongKham) {
        this.phongKham = phongKham;
    }
}
