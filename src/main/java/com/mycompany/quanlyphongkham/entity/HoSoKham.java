package com.mycompany.quanlyphongkham.entity;

import com.mycompany.quanlyphongkham.util.FormatUtil;
import java.util.Date;
import java.util.List;

public class HoSoKham {
    private String maHoSo;
    private String maHoSoCode;
    private String maLichHen;
    private String maLichHenCode;
    private String maBenhNhan;
    private String maBenhNhanCode;
    private String tenBenhNhan;
    private String maBacSi;
    private String maBacSiCode;
    private String tenBacSi;
    private String trieuChung;
    private String chanDoan;
    private String loiDan;
    private Date ngayKham;
    private List<DonThuoc> danhSachThuoc;
    private List<ChiDinhDichVu> danhSachDichVu;

    public HoSoKham() {}

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

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public String getChanDoan() {
        return chanDoan;
    }

    public void setChanDoan(String chanDoan) {
        this.chanDoan = chanDoan;
    }

    public String getLoiDan() {
        return loiDan;
    }

    public void setLoiDan(String loiDan) {
        this.loiDan = loiDan;
    }

    public Date getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(Date ngayKham) {
        this.ngayKham = ngayKham;
    }

    public List<DonThuoc> getDanhSachThuoc() {
        return danhSachThuoc;
    }

    public void setDanhSachThuoc(List<DonThuoc> danhSachThuoc) {
        this.danhSachThuoc = danhSachThuoc;
    }

    public List<ChiDinhDichVu> getDanhSachDichVu() {
        return danhSachDichVu;
    }

    public void setDanhSachDichVu(List<ChiDinhDichVu> danhSachDichVu) {
        this.danhSachDichVu = danhSachDichVu;
    }
}
