package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.util.Date;

public class LichHen {
    private String maLichHen;
    private String maLichHenCode;
    private String maBenhNhan;
    private String maBenhNhanCode;
    private String tenBenhNhan;
    private String soDienThoai;
    private String maBacSi;
    private String maBacSiCode;
    private String tenBacSi;
    private String phongKham;
    private Date thoiGianHen;
    private Integer soThuTu;
    private String trangThai;
    private String ghiChu;

    public LichHen() {}

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
        this.maLichHenCode = FormatUtil.normalizeLichHen(maLichHen);
    }

    public String getMaLichHenCode() {
        if (maLichHenCode == null && maLichHen != null) {
            maLichHenCode = FormatUtil.normalizeLichHen(maLichHen);
        }
        return maLichHenCode;
    }

    public void setMaLichHenCode(String maLichHenCode) {
        this.maLichHenCode = maLichHenCode;
    }

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

    public String getTenBenhNhan() {
        return tenBenhNhan;
    }

    public void setTenBenhNhan(String tenBenhNhan) {
        this.tenBenhNhan = tenBenhNhan;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

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

    public String getTenBacSi() {
        return tenBacSi;
    }

    public void setTenBacSi(String tenBacSi) {
        this.tenBacSi = tenBacSi;
    }

    public String getPhongKham() {
        return phongKham;
    }

    public void setPhongKham(String phongKham) {
        this.phongKham = phongKham;
    }

    public Date getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(Date thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public Integer getSoThuTu() {
        return soThuTu;
    }

    public void setSoThuTu(Integer soThuTu) {
        this.soThuTu = soThuTu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
