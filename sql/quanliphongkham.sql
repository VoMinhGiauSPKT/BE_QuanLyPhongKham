USE master;
GO

IF DB_ID('QuanLyPhongKhamDB') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyPhongKhamDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyPhongKhamDB;
END
GO

CREATE DATABASE QuanLyPhongKhamDB;
GO
USE QuanLyPhongKhamDB;
GO

-- 0. TẠO CÁC SEQUENCE ĐỂ SINH MÃ TỰ ĐỘNG DẠNG CHUỖI (ND001, BN001, BS001,...)
CREATE SEQUENCE seq_nguoi_dung START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_benh_nhan START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_bac_si START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_lich_hen START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_ho_so START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_dich_vu START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_chi_dinh START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_thuoc START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_don_thuoc START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_hoa_don START WITH 1 INCREMENT BY 1;
GO

-- 1. Bảng Người dùng hệ thống (Mã: ND001, ND002...)
CREATE TABLE nguoi_dung (
    ma_nguoi_dung VARCHAR(20) PRIMARY KEY DEFAULT ('ND' + RIGHT('000' + CAST(NEXT VALUE FOR seq_nguoi_dung AS VARCHAR(10)), 3)),
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau_hash VARCHAR(255) NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(15) NULL,
    vai_tro NVARCHAR(30) NOT NULL, -- 'QUAN_TRI', 'BAC_SI', 'LE_TAN', 'DUOC_SI'
    hoat_dong BIT NOT NULL DEFAULT 1,
    ngay_tao DATETIME NOT NULL DEFAULT GETDATE()
);

-- 2. Bảng Bệnh nhân (Mã: BN001, BN002...)
CREATE TABLE benh_nhan (
    ma_benh_nhan VARCHAR(20) PRIMARY KEY DEFAULT ('BN' + RIGHT('000' + CAST(NEXT VALUE FOR seq_benh_nhan AS VARCHAR(10)), 3)),
    ho_ten NVARCHAR(100) NOT NULL,
    ngay_sinh DATE NOT NULL,
    gioi_tinh NVARCHAR(10) NOT NULL,
    so_dien_thoai VARCHAR(15) NOT NULL UNIQUE,
    so_cccd VARCHAR(20) NULL UNIQUE,
    dia_chi NVARCHAR(255) NULL,
    ngay_tao DATETIME NOT NULL DEFAULT GETDATE()
);

-- 3. Bảng Bác sĩ (Mã: BS001, BS002... Khóa ngoại liên kết nguoi_dung)
CREATE TABLE bac_si (
    ma_bac_si VARCHAR(20) PRIMARY KEY,
    chuyen_khoa NVARCHAR(100) NOT NULL,
    so_chung_chi VARCHAR(50) NOT NULL UNIQUE,
    phong_kham VARCHAR(20) NOT NULL,
    CONSTRAINT FK_bac_si_nguoi_dung FOREIGN KEY (ma_bac_si) REFERENCES nguoi_dung(ma_nguoi_dung) ON DELETE CASCADE
);

-- 4. Bảng Lịch hẹn (Mã: LH001, LH002...)
CREATE TABLE lich_hen (
    ma_lich_hen VARCHAR(20) PRIMARY KEY DEFAULT ('LH' + RIGHT('000' + CAST(NEXT VALUE FOR seq_lich_hen AS VARCHAR(10)), 3)),
    ma_benh_nhan VARCHAR(20) NOT NULL,
    ma_bac_si VARCHAR(20) NOT NULL,
    thoi_gian_hen DATETIME NOT NULL,
    so_thu_tu INT NOT NULL,
    trang_thai NVARCHAR(30) NOT NULL DEFAULT N'Chờ khám', -- 'Chờ khám', 'Đang khám', 'Đã khám', 'Đã hủy'
    loai_lich_hen NVARCHAR(30) NOT NULL CONSTRAINT DF_lich_hen_loai DEFAULT N'Khám mới', -- 'Khám mới', 'Tái khám'
    ma_lich_hen_truoc VARCHAR(20) NULL, -- Khóa ngoại tự tham chiếu liên kết ca tái khám với ca khám trước
    ghi_chu NVARCHAR(255) NULL,
    CONSTRAINT FK_lich_hen_benh_nhan FOREIGN KEY (ma_benh_nhan) REFERENCES benh_nhan(ma_benh_nhan),
    CONSTRAINT FK_lich_hen_bac_si FOREIGN KEY (ma_bac_si) REFERENCES bac_si(ma_bac_si),
    CONSTRAINT FK_lich_hen_truoc FOREIGN KEY (ma_lich_hen_truoc) REFERENCES lich_hen(ma_lich_hen)
);

-- 5. Bảng Hồ sơ khám bệnh (Mã: HS001, HS002...)
CREATE TABLE ho_so_kham (
    ma_ho_so VARCHAR(20) PRIMARY KEY DEFAULT ('HS' + RIGHT('000' + CAST(NEXT VALUE FOR seq_ho_so AS VARCHAR(10)), 3)),
    ma_lich_hen VARCHAR(20) NOT NULL UNIQUE,
    ma_benh_nhan VARCHAR(20) NOT NULL,
    ma_bac_si VARCHAR(20) NOT NULL,
    trieu_chung NVARCHAR(MAX) NOT NULL,
    chan_doan NVARCHAR(MAX) NOT NULL,
    loi_dan NVARCHAR(MAX) NULL,
    ngay_tai_kham DATE NULL, -- Ngày bác sĩ hẹn tái khám (nếu có)
    ngay_kham DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_ho_so_lich_hen FOREIGN KEY (ma_lich_hen) REFERENCES lich_hen(ma_lich_hen),
    CONSTRAINT FK_ho_so_benh_nhan FOREIGN KEY (ma_benh_nhan) REFERENCES benh_nhan(ma_benh_nhan),
    CONSTRAINT FK_ho_so_bac_si FOREIGN KEY (ma_bac_si) REFERENCES bac_si(ma_bac_si)
);

-- 6. Bảng Dịch vụ khám & cận lâm sàng (Mã: DV001, DV002...)
CREATE TABLE dich_vu (
    ma_dich_vu VARCHAR(20) PRIMARY KEY DEFAULT ('DV' + RIGHT('000' + CAST(NEXT VALUE FOR seq_dich_vu AS VARCHAR(10)), 3)),
    ten_dich_vu NVARCHAR(150) NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    mo_ta NVARCHAR(255) NULL,
    trang_thai BIT NOT NULL DEFAULT 1
);

-- 7. Bảng Chỉ định dịch vụ (Mã: CD001, CD002...)
CREATE TABLE chi_dinh_dich_vu (
    ma_chi_dinh VARCHAR(20) PRIMARY KEY DEFAULT ('CD' + RIGHT('000' + CAST(NEXT VALUE FOR seq_chi_dinh AS VARCHAR(10)), 3)),
    ma_ho_so VARCHAR(20) NOT NULL,
    ma_dich_vu VARCHAR(20) NOT NULL,
    so_luong INT NOT NULL DEFAULT 1,
    don_gia DECIMAL(18,2) NOT NULL,
    trang_thai NVARCHAR(30) NOT NULL DEFAULT N'Chưa thực hiện', -- 'Chưa thực hiện', 'Đã có kết quả'
    ket_qua NVARCHAR(MAX) NULL,
    CONSTRAINT FK_chi_dinh_ho_so FOREIGN KEY (ma_ho_so) REFERENCES ho_so_kham(ma_ho_so),
    CONSTRAINT FK_chi_dinh_dich_vu FOREIGN KEY (ma_dich_vu) REFERENCES dich_vu(ma_dich_vu)
);

-- 8. Bảng Thuốc & Vật tư y tế (Mã: T001, T002...)
CREATE TABLE thuoc (
    ma_thuoc VARCHAR(20) PRIMARY KEY DEFAULT ('T' + RIGHT('000' + CAST(NEXT VALUE FOR seq_thuoc AS VARCHAR(10)), 3)),
    ten_thuoc NVARCHAR(150) NOT NULL,
    don_vi_tinh NVARCHAR(30) NOT NULL,
    so_luong_ton INT NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    han_su_dung DATE NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1
);

-- 9. Bảng Đơn thuốc (Mã: DT001, DT002...)
CREATE TABLE don_thuoc (
    ma_don_thuoc VARCHAR(20) PRIMARY KEY DEFAULT ('DT' + RIGHT('000' + CAST(NEXT VALUE FOR seq_don_thuoc AS VARCHAR(10)), 3)),
    ma_ho_so VARCHAR(20) NOT NULL,
    ma_thuoc VARCHAR(20) NOT NULL,
    so_luong INT NOT NULL,
    cach_dung NVARCHAR(100) NOT NULL,
    huong_dan NVARCHAR(255) NULL,
    CONSTRAINT FK_don_thuoc_ho_so FOREIGN KEY (ma_ho_so) REFERENCES ho_so_kham(ma_ho_so),
    CONSTRAINT FK_don_thuoc_thuoc FOREIGN KEY (ma_thuoc) REFERENCES thuoc(ma_thuoc)
);

-- 10. Bảng Hóa đơn (Mã: HD001, HD002...)
CREATE TABLE hoa_don (
    ma_hoa_don VARCHAR(20) PRIMARY KEY DEFAULT ('HD' + RIGHT('000' + CAST(NEXT VALUE FOR seq_hoa_don AS VARCHAR(10)), 3)),
    ma_ho_so VARCHAR(20) NOT NULL UNIQUE,
    tien_dich_vu DECIMAL(18,2) NOT NULL DEFAULT 0,
    tien_thuoc DECIMAL(18,2) NOT NULL DEFAULT 0,
    tien_giam_gia DECIMAL(18,2) NOT NULL DEFAULT 0,
    tong_tien DECIMAL(18,2) NOT NULL,
    hinh_thuc_thanh_toan NVARCHAR(50) NOT NULL, -- N'Tiền mặt', N'Chuyển khoản', N'Thẻ'
    trang_thai NVARCHAR(30) NOT NULL DEFAULT N'Chưa thanh toán', -- N'Chưa thanh toán', N'Đã thanh toán'
    ngay_lap DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_hoa_don_ho_so FOREIGN KEY (ma_ho_so) REFERENCES ho_so_kham(ma_ho_so)
);

-- 11. Bảng Nhật ký biến động giá thuốc (Audit Log cho dược sĩ)
CREATE TABLE nhat_ky_gia_thuoc (
    ma_nhat_ky INT IDENTITY(1,1) PRIMARY KEY,
    ma_thuoc VARCHAR(20) NOT NULL,
    gia_cu DECIMAL(18,2) NOT NULL,
    gia_moi DECIMAL(18,2) NOT NULL,
    thoi_gian DATETIME NOT NULL DEFAULT GETDATE(),
    nguoi_thuc_hien VARCHAR(50) NOT NULL DEFAULT SYSTEM_USER,
    CONSTRAINT FK_nhat_ky_thuoc FOREIGN KEY (ma_thuoc) REFERENCES thuoc(ma_thuoc) ON DELETE CASCADE
);
GO
