package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.util.Date;

public class BenhNhan {
    private String maBenhNhan;
    private String maBenhNhanCode;
    private String hoTen;
    private Date ngaySinh;
    private String gioiTinh;
    private String soDienThoai;
    private String soCccd;
    private String diaChi;
    private Date ngayTao;

    public BenhNhan() {}

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        this.maBenhNhan = maBenhNhan;
        this.maBenhNhanCode = FormatUtil.normalizeBenhNhan(maBenhNhan);
    }

    public String getMaBenhNhanCode() {
        if (maBenhNhanCode == null && maBenhNhan != null) {
            maBenhNhanCode = FormatUtil.normalizeBenhNhan(maBenhNhan);
        }
        return maBenhNhanCode;
    }

    public void setMaBenhNhanCode(String maBenhNhanCode) {
        this.maBenhNhanCode = maBenhNhanCode;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getSoCccd() {
        return soCccd;
    }

    public void setSoCccd(String soCccd) {
        this.soCccd = soCccd;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }
}
