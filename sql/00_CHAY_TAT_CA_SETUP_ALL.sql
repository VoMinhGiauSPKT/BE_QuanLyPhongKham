-- =========================================================================
-- HỆ THỐNG QUẢN LÝ PHÒNG KHÁM - MEDICARE CLINIC
-- SCRIPT TỔNG HỢP SETUP TOÀN BỘ CƠ SỞ DỮ LIỆU TỪ ĐẦU (ALL-IN-ONE)
-- Chạy toàn bộ file này bằng SQL Server Management Studio (SSMS) - Nhấn F5
-- =========================================================================

USE master;
GO

IF DB_ID('QuanLyPhongKhamDB') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyPhongKhamDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyPhongKhamDB;
END;
GO

CREATE DATABASE QuanLyPhongKhamDB;
GO

USE QuanLyPhongKhamDB;
GO

-- =========================================================================
-- PHẦN 1: TẠO CÁC SEQUENCE ĐỂ SINH MÃ TỰ ĐỘNG DẠNG CHUỖI (ND001, BN001,...)
-- =========================================================================
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

-- =========================================================================
-- PHẦN 2: TẠO 11 BẢNG DỮ LIỆU
-- =========================================================================

-- 1. Bảng Người dùng hệ thống (ND001, ND002...)
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

-- 2. Bảng Bệnh nhân (BN001, BN002...)
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

-- 3. Bảng Bác sĩ (Khóa ngoại liên kết nguoi_dung)
CREATE TABLE bac_si (
    ma_bac_si VARCHAR(20) PRIMARY KEY,
    chuyen_khoa NVARCHAR(100) NOT NULL,
    so_chung_chi VARCHAR(50) NOT NULL UNIQUE,
    phong_kham VARCHAR(20) NOT NULL,
    CONSTRAINT FK_bac_si_nguoi_dung FOREIGN KEY (ma_bac_si) REFERENCES nguoi_dung(ma_nguoi_dung) ON DELETE CASCADE
);

-- 4. Bảng Lịch hẹn (LH001, LH002...)
CREATE TABLE lich_hen (
    ma_lich_hen VARCHAR(20) PRIMARY KEY DEFAULT ('LH' + RIGHT('000' + CAST(NEXT VALUE FOR seq_lich_hen AS VARCHAR(10)), 3)),
    ma_benh_nhan VARCHAR(20) NOT NULL,
    ma_bac_si VARCHAR(20) NOT NULL,
    thoi_gian_hen DATETIME NOT NULL,
    so_thu_tu INT NOT NULL,
    trang_thai NVARCHAR(30) NOT NULL DEFAULT N'Chờ khám', -- 'Chờ khám', 'Đang khám', 'Đã khám', 'Đã hủy'
    loai_lich_hen NVARCHAR(30) NOT NULL CONSTRAINT DF_lich_hen_loai DEFAULT N'Khám mới', -- 'Khám mới', 'Tái khám'
    ma_lich_hen_truoc VARCHAR(20) NULL,
    ghi_chu NVARCHAR(255) NULL,
    CONSTRAINT FK_lich_hen_benh_nhan FOREIGN KEY (ma_benh_nhan) REFERENCES benh_nhan(ma_benh_nhan),
    CONSTRAINT FK_lich_hen_bac_si FOREIGN KEY (ma_bac_si) REFERENCES bac_si(ma_bac_si),
    CONSTRAINT FK_lich_hen_truoc FOREIGN KEY (ma_lich_hen_truoc) REFERENCES lich_hen(ma_lich_hen)
);

-- 5. Bảng Hồ sơ khám bệnh (HS001, HS002...)
CREATE TABLE ho_so_kham (
    ma_ho_so VARCHAR(20) PRIMARY KEY DEFAULT ('HS' + RIGHT('000' + CAST(NEXT VALUE FOR seq_ho_so AS VARCHAR(10)), 3)),
    ma_lich_hen VARCHAR(20) NOT NULL UNIQUE,
    ma_benh_nhan VARCHAR(20) NOT NULL,
    ma_bac_si VARCHAR(20) NOT NULL,
    trieu_chung NVARCHAR(MAX) NOT NULL,
    chan_doan NVARCHAR(MAX) NOT NULL,
    loi_dan NVARCHAR(MAX) NULL,
    ngay_tai_kham DATE NULL,
    ngay_kham DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_ho_so_lich_hen FOREIGN KEY (ma_lich_hen) REFERENCES lich_hen(ma_lich_hen),
    CONSTRAINT FK_ho_so_benh_nhan FOREIGN KEY (ma_benh_nhan) REFERENCES benh_nhan(ma_benh_nhan),
    CONSTRAINT FK_ho_so_bac_si FOREIGN KEY (ma_bac_si) REFERENCES bac_si(ma_bac_si)
);

-- 6. Bảng Dịch vụ khám & cận lâm sàng (DV001, DV002...)
CREATE TABLE dich_vu (
    ma_dich_vu VARCHAR(20) PRIMARY KEY DEFAULT ('DV' + RIGHT('000' + CAST(NEXT VALUE FOR seq_dich_vu AS VARCHAR(10)), 3)),
    ten_dich_vu NVARCHAR(150) NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    mo_ta NVARCHAR(255) NULL,
    trang_thai BIT NOT NULL DEFAULT 1
);

-- 7. Bảng Chỉ định dịch vụ (CD001, CD002...)
CREATE TABLE chi_dinh_dich_vu (
    ma_chi_dinh VARCHAR(20) PRIMARY KEY DEFAULT ('CD' + RIGHT('000' + CAST(NEXT VALUE FOR seq_chi_dinh AS VARCHAR(10)), 3)),
    ma_ho_so VARCHAR(20) NOT NULL,
    ma_dich_vu VARCHAR(20) NOT NULL,
    so_luong INT NOT NULL DEFAULT 1,
    don_gia DECIMAL(18,2) NOT NULL,
    trang_thai NVARCHAR(30) NOT NULL DEFAULT N'Chưa thực hiện',
    ket_qua NVARCHAR(MAX) NULL,
    CONSTRAINT FK_chi_dinh_ho_so FOREIGN KEY (ma_ho_so) REFERENCES ho_so_kham(ma_ho_so),
    CONSTRAINT FK_chi_dinh_dich_vu FOREIGN KEY (ma_dich_vu) REFERENCES dich_vu(ma_dich_vu)
);

-- 8. Bảng Thuốc & Vật tư y tế (T001, T002...)
CREATE TABLE thuoc (
    ma_thuoc VARCHAR(20) PRIMARY KEY DEFAULT ('T' + RIGHT('000' + CAST(NEXT VALUE FOR seq_thuoc AS VARCHAR(10)), 3)),
    ten_thuoc NVARCHAR(150) NOT NULL,
    don_vi_tinh NVARCHAR(30) NOT NULL,
    so_luong_ton INT NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    han_su_dung DATE NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1
);

-- 9. Bảng Đơn thuốc (DT001, DT002...)
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

-- 10. Bảng Hóa đơn viện phí (HD001, HD002...)
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

-- =========================================================================
-- PHẦN 3: NẠP DỮ LIỆU MẪU BAN ĐẦU (SEED DATA - MẬT KHẨU: 123456 ĐÃ HASH BCRYPT)
-- =========================================================================
INSERT INTO nguoi_dung (ma_nguoi_dung, ten_dang_nhap, mat_khau_hash, ho_ten, so_dien_thoai, vai_tro, hoat_dong) VALUES
('ND001', 'admin', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'Quản Trị Viên', '0901000001', 'QUAN_TRI', 1),
('BS001', 'bacsi1', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'BS. Nguyễn Văn A', '0901000002', 'BAC_SI', 1),
('ND003', 'letan1', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'Lê Thị Lễ Tân', '0901000003', 'LE_TAN', 1),
('ND004', 'duocsi1', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'Trần Văn Dược', '0901000004', 'DUOC_SI', 1);

INSERT INTO bac_si (ma_bac_si, chuyen_khoa, so_chung_chi, phong_kham) VALUES
('BS001', N'Nội tổng quát', 'CCHN-001', 'P.101');

INSERT INTO benh_nhan (ma_benh_nhan, ho_ten, ngay_sinh, gioi_tinh, so_dien_thoai, so_cccd, dia_chi) VALUES
('BN001', N'Nguyễn Thị Lan', '1995-05-15', N'Nữ', '0988111222', '079195000001', N'123 Lê Lợi, Q.1, TP.HCM'),
('BN002', N'Trần Văn Nam', '1988-10-20', N'Nam', '0977333444', '079188000002', N'456 Nguyễn Huệ, Q.1, TP.HCM');

INSERT INTO thuoc (ma_thuoc, ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung, trang_thai) VALUES
('T001', N'Paracetamol 500mg', N'Viên', 100, 2000, '2027-12-31', 1),
('T002', N'Amoxicillin 500mg', N'Viên', 50, 5000, '2027-12-31', 1),
('T003', N'Vitamin C 500mg', N'Viên', 10, 1500, '2027-06-30', 1);

INSERT INTO dich_vu (ma_dich_vu, ten_dich_vu, don_gia, mo_ta, trang_thai) VALUES
('DV001', N'Khám nội tổng quát', 150000, N'Khám lâm sàng', 1),
('DV002', N'Xét nghiệm máu toàn phần', 200000, N'Công thức máu 18 thông số', 1),
('DV003', N'Chụp X-quang lồng ngực', 250000, N'Chẩn đoán hình ảnh phổi và tim', 1);

INSERT INTO lich_hen (ma_lich_hen, ma_benh_nhan, ma_bac_si, thoi_gian_hen, so_thu_tu, trang_thai, loai_lich_hen, ghi_chu) VALUES
('LH001', 'BN001', 'BS001', GETDATE(), 1, N'Chờ khám', N'Khám mới', N'Bệnh nhân đau đầu sốt nhẹ');
GO

-- =========================================================================
-- PHẦN 4: 5 RÀNG BUỘC TOÀN VẸN (CHECK CONSTRAINTS & UNIQUE)
-- =========================================================================
ALTER TABLE benh_nhan ADD CONSTRAINT CK_benh_nhan_gioi_tinh CHECK (gioi_tinh IN (N'Nam', N'Nữ', N'Khác'));
ALTER TABLE thuoc ADD CONSTRAINT CK_thuoc_so_luong_ton CHECK (so_luong_ton >= 0);
ALTER TABLE dich_vu ADD CONSTRAINT CK_dich_vu_don_gia CHECK (don_gia > 0);
ALTER TABLE thuoc ADD CONSTRAINT CK_thuoc_don_gia CHECK (don_gia > 0);
ALTER TABLE hoa_don ADD CONSTRAINT CK_hoa_don_tong_tien CHECK (tong_tien = (tien_dich_vu + tien_thuoc - tien_giam_gia) AND tong_tien >= 0);
ALTER TABLE lich_hen ADD CONSTRAINT UQ_lich_hen_bacsi_ngay_sothutu UNIQUE (ma_bac_si, thoi_gian_hen, so_thu_tu);
GO

-- =========================================================================
-- PHẦN 5: 5 USER-DEFINED FUNCTIONS
-- =========================================================================

-- Hàm 1: Tính tuổi
CREATE OR ALTER FUNCTION fn_tinh_tuoi (@ngay_sinh DATE)
RETURNS INT
AS
BEGIN
    RETURN DATEDIFF(YEAR, @ngay_sinh, GETDATE()) - 
           CASE WHEN (MONTH(@ngay_sinh) > MONTH(GETDATE())) 
                  OR (MONTH(@ngay_sinh) = MONTH(GETDATE()) AND DAY(@ngay_sinh) > DAY(GETDATE())) 
           THEN 1 ELSE 0 END;
END;
GO

-- Hàm 2: Tính tổng tiền thuốc của hồ sơ
CREATE OR ALTER FUNCTION fn_tinh_tong_tien_thuoc (@ma_ho_so VARCHAR(20))
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @tong_tien DECIMAL(18,2);
    SELECT @tong_tien = ISNULL(SUM(dt.so_luong * t.don_gia), 0)
    FROM don_thuoc dt
    JOIN thuoc t ON dt.ma_thuoc = t.ma_thuoc
    WHERE dt.ma_ho_so = @ma_ho_so;
    RETURN @tong_tien;
END;
GO

-- Hàm 3: Tính tổng tiền dịch vụ của hồ sơ
CREATE OR ALTER FUNCTION fn_tinh_tong_tien_dich_vu (@ma_ho_so VARCHAR(20))
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @tong_tien DECIMAL(18,2);
    SELECT @tong_tien = ISNULL(SUM(so_luong * don_gia), 0)
    FROM chi_dinh_dich_vu
    WHERE ma_ho_so = @ma_ho_so;
    RETURN @tong_tien;
END;
GO

-- Hàm 4: Lịch sử khám bệnh của bệnh nhân
CREATE OR ALTER FUNCTION fn_lich_su_kham_benh (@ma_benh_nhan VARCHAR(20))
RETURNS TABLE
AS
RETURN (
    SELECT 
        hs.ma_ho_so,
        hs.ngay_kham,
        bs_user.ho_ten AS ten_bac_si,
        hs.trieu_chung,
        hs.chan_doan,
        hs.loi_dan,
        hs.ngay_tai_kham,
        hd.tong_tien,
        ISNULL(hd.trang_thai, N'Chưa thanh toán') AS trang_thai_thanh_toan
    FROM ho_so_kham hs
    JOIN bac_si bs ON hs.ma_bac_si = bs.ma_bac_si
    JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung
    LEFT JOIN hoa_don hd ON hs.ma_ho_so = hd.ma_ho_so
    WHERE hs.ma_benh_nhan = @ma_benh_nhan
);
GO

-- Hàm 5: Tìm thuốc khả dụng trong kho
CREATE OR ALTER FUNCTION fn_tim_thuoc_kha_dung (@tu_khoa NVARCHAR(100))
RETURNS TABLE
AS
RETURN (
    SELECT 
        ma_thuoc,
        ten_thuoc,
        don_vi_tinh,
        so_luong_ton,
        don_gia,
        han_su_dung
    FROM thuoc
    WHERE trang_thai = 1 
      AND so_luong_ton > 0 
      AND han_su_dung > CAST(GETDATE() AS DATE)
      AND ten_thuoc LIKE N'%' + @tu_khoa + N'%'
);
GO

-- =========================================================================
-- PHẦN 6: 5 VIEWS
-- =========================================================================

-- View 1: Hàng chờ khám hôm nay
CREATE OR ALTER VIEW v_hang_cho_kham_hom_nay AS
SELECT 
    lh.ma_lich_hen,
    lh.so_thu_tu,
    bn.ho_ten AS ten_benh_nhan,
    bn.so_dien_thoai,
    bs_user.ho_ten AS ten_bac_si,
    bs.phong_kham,
    lh.thoi_gian_hen,
    lh.trang_thai
FROM lich_hen lh
JOIN benh_nhan bn ON lh.ma_benh_nhan = bn.ma_benh_nhan
JOIN bac_si bs ON lh.ma_bac_si = bs.ma_bac_si
JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung
WHERE CAST(lh.thoi_gian_hen AS DATE) = CAST(GETDATE() AS DATE);
GO

-- View 2: Chi tiết khám bệnh
CREATE OR ALTER VIEW v_chi_tiet_kham_benh AS
SELECT 
    hs.ma_ho_so,
    bn.ho_ten AS ten_benh_nhan,
    bs_user.ho_ten AS ten_bac_si,
    hs.chan_doan,
    hs.ngay_kham,
    ISNULL(t.ten_thuoc, N'Không kê thuốc') AS ten_thuoc,
    dt.so_luong AS so_luong_thuoc,
    ISNULL(dv.ten_dich_vu, N'Không chỉ định') AS ten_dich_vu
FROM ho_so_kham hs
JOIN benh_nhan bn ON hs.ma_benh_nhan = bn.ma_benh_nhan
JOIN nguoi_dung bs_user ON hs.ma_bac_si = bs_user.ma_nguoi_dung
LEFT JOIN don_thuoc dt ON hs.ma_ho_so = dt.ma_ho_so
LEFT JOIN thuoc t ON dt.ma_thuoc = t.ma_thuoc
LEFT JOIN chi_dinh_dich_vu cd ON hs.ma_ho_so = cd.ma_ho_so
LEFT JOIN dich_vu dv ON cd.ma_dich_vu = dv.ma_dich_vu;
GO

-- View 3: Cảnh báo tồn kho thuốc
CREATE OR ALTER VIEW v_canh_bao_ton_kho_thuoc AS
SELECT 
    ma_thuoc,
    ten_thuoc,
    so_luong_ton,
    don_vi_tinh,
    han_su_dung,
    DATEDIFF(day, GETDATE(), han_su_dung) AS so_ngay_con_han,
    CASE 
        WHEN so_luong_ton < 20 AND han_su_dung < DATEADD(day, 30, GETDATE()) THEN N'Tồn ít & Sắp hết hạn'
        WHEN so_luong_ton < 20 THEN N'Sắp hết hàng'
        WHEN han_su_dung < DATEADD(day, 30, GETDATE()) THEN N'Sắp hết hạn sử dụng'
        ELSE N'Bình thường'
    END AS trang_thai_canh_bao
FROM thuoc
WHERE trang_thai = 1 AND (so_luong_ton < 20 OR han_su_dung < DATEADD(day, 30, GETDATE()));
GO

-- View 4: Doanh thu theo bác sĩ
CREATE OR ALTER VIEW v_doanh_thu_theo_bac_si AS
SELECT 
    bs.ma_bac_si,
    nd.ho_ten AS ten_bac_si,
    bs.chuyen_khoa,
    bs.phong_kham,
    (SELECT COUNT(DISTINCT hs1.ma_ho_so) 
     FROM ho_so_kham hs1 
     WHERE hs1.ma_bac_si = bs.ma_bac_si) AS so_ca_kham,
    (SELECT ISNULL(SUM(hd1.tong_tien), 0) 
     FROM hoa_don hd1 
     JOIN ho_so_kham hs2 ON hd1.ma_ho_so = hs2.ma_ho_so 
     WHERE hs2.ma_bac_si = bs.ma_bac_si) AS tong_doanh_thu
FROM bac_si bs
JOIN nguoi_dung nd ON bs.ma_bac_si = nd.ma_nguoi_dung
WHERE nd.hoat_dong = 1;
GO

-- View 5: Doanh thu theo ngày
CREATE OR ALTER VIEW v_doanh_thu_theo_ngay AS
SELECT 
    CAST(ngay_lap AS DATE) AS ngay_thanh_toan,
    COUNT(ma_hoa_don) AS so_luong_hoa_don,
    SUM(tien_dich_vu) AS tong_tien_dich_vu,
    SUM(tien_thuoc) AS tong_tien_thuoc,
    SUM(tien_giam_gia) AS tong_tien_giam_gia,
    SUM(tong_tien) AS thuc_thu
FROM hoa_don
WHERE trang_thai = N'Đã thanh toán'
GROUP BY CAST(ngay_lap AS DATE);
GO

-- =========================================================================
-- PHẦN 7: 5 TRIGGERS
-- =========================================================================

-- Trigger 1: Tự động trừ kho khi kê đơn thuốc
CREATE OR ALTER TRIGGER trg_ke_don_tru_kho
ON don_thuoc
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 
        FROM thuoc t
        JOIN inserted i ON t.ma_thuoc = i.ma_thuoc
        WHERE t.so_luong_ton < i.so_luong
    )
    BEGIN
        RAISERROR(N'Số lượng tồn kho không đủ để kê đơn!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;

    UPDATE t
    SET t.so_luong_ton = t.so_luong_ton - i.so_luong
    FROM thuoc t
    JOIN inserted i ON t.ma_thuoc = i.ma_thuoc;
END;
GO

-- Trigger 2: Chặn kê đơn thuốc hết hạn
CREATE OR ALTER TRIGGER trg_kiem_tra_han_thuoc
ON don_thuoc
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 
        FROM thuoc t
        JOIN inserted i ON t.ma_thuoc = i.ma_thuoc
        WHERE t.han_su_dung < CAST(GETDATE() AS DATE)
    )
    BEGIN
        RAISERROR(N'Thuốc đã hết hạn sử dụng, không được phép kê đơn!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

-- Trigger 3: Hoàn lại kho khi hủy đơn thuốc
CREATE OR ALTER TRIGGER trg_xoa_don_hoan_kho
ON don_thuoc
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE t
    SET t.so_luong_ton = t.so_luong_ton + d.so_luong
    FROM thuoc t
    JOIN deleted d ON t.ma_thuoc = d.ma_thuoc;
END;
GO

-- Trigger 4: Tự động đổi trạng thái lịch hẹn thành 'Đã khám' khi hoàn tất bệnh án
CREATE OR ALTER TRIGGER trg_hoan_thanh_lich_kham
ON ho_so_kham
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE lh
    SET lh.trang_thai = N'Đã khám'
    FROM lich_hen lh
    JOIN inserted i ON lh.ma_lich_hen = i.ma_lich_hen;
END;
GO

-- Trigger 5: Ghi nhật ký (Audit Log) biến động giá thuốc
CREATE OR ALTER TRIGGER trg_ghi_nhat_ky_gia_thuoc
ON thuoc
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(don_gia)
    BEGIN
        INSERT INTO nhat_ky_gia_thuoc (ma_thuoc, gia_cu, gia_moi, thoi_gian, nguoi_thuc_hien)
        SELECT 
            i.ma_thuoc,
            d.don_gia AS gia_cu,
            i.don_gia AS gia_moi,
            GETDATE(),
            SYSTEM_USER
        FROM inserted i
        JOIN deleted d ON i.ma_thuoc = d.ma_thuoc
        WHERE i.don_gia <> d.don_gia;
    END;
END;
GO

-- =========================================================================
-- PHẦN 8: 5 STORED PROCEDURES NGHIỆP VỤ
-- =========================================================================

-- SP 1: Tiếp nhận khám & Cấp STT trong ngày
CREATE OR ALTER PROCEDURE sp_tiep_nhan_kham
    @ho_ten NVARCHAR(100),
    @ngay_sinh DATE,
    @gioi_tinh NVARCHAR(10),
    @so_dien_thoai VARCHAR(15),
    @ma_bac_si VARCHAR(20),
    @thoi_gian_hen DATETIME,
    @ma_lich_hen VARCHAR(20) OUTPUT,
    @so_cccd VARCHAR(20) = NULL,
    @dia_chi NVARCHAR(255) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
            DECLARE @ma_benh_nhan VARCHAR(20);
            SELECT @ma_benh_nhan = ma_benh_nhan 
            FROM benh_nhan 
            WHERE so_dien_thoai = @so_dien_thoai 
               OR (@so_cccd IS NOT NULL AND so_cccd = @so_cccd);
            
            IF @ma_benh_nhan IS NULL
            BEGIN
                DECLARE @new_bn TABLE (ma_benh_nhan VARCHAR(20));
                INSERT INTO benh_nhan (ho_ten, ngay_sinh, gioi_tinh, so_dien_thoai, so_cccd, dia_chi)
                OUTPUT INSERTED.ma_benh_nhan INTO @new_bn
                VALUES (@ho_ten, @ngay_sinh, @gioi_tinh, @so_dien_thoai, @so_cccd, @dia_chi);
                SELECT @ma_benh_nhan = ma_benh_nhan FROM @new_bn;
            END
            ELSE
            BEGIN
                UPDATE benh_nhan 
                SET so_cccd = ISNULL(@so_cccd, so_cccd),
                    dia_chi = ISNULL(@dia_chi, dia_chi)
                WHERE ma_benh_nhan = @ma_benh_nhan;
            END;

            DECLARE @so_thu_tu_tiep_theo INT;
            SELECT @so_thu_tu_tiep_theo = ISNULL(MAX(so_thu_tu), 0) + 1
            FROM lich_hen
            WHERE ma_bac_si = @ma_bac_si AND CAST(thoi_gian_hen AS DATE) = CAST(@thoi_gian_hen AS DATE);

            DECLARE @new_lh TABLE (ma_lich_hen VARCHAR(20));
            INSERT INTO lich_hen (ma_benh_nhan, ma_bac_si, thoi_gian_hen, so_thu_tu, trang_thai, loai_lich_hen)
            OUTPUT INSERTED.ma_lich_hen INTO @new_lh
            VALUES (@ma_benh_nhan, @ma_bac_si, @thoi_gian_hen, @so_thu_tu_tiep_theo, N'Chờ khám', N'Khám mới');

            SELECT @ma_lich_hen = ma_lich_hen FROM @new_lh;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- SP 2: Cập nhật bệnh nhân
CREATE OR ALTER PROCEDURE sp_cap_nhat_benh_nhan
    @ma_benh_nhan VARCHAR(20),
    @ho_ten NVARCHAR(100),
    @ngay_sinh DATE,
    @gioi_tinh NVARCHAR(10),
    @so_cccd VARCHAR(20),
    @dia_chi NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM benh_nhan WHERE ma_benh_nhan = @ma_benh_nhan)
    BEGIN
        RAISERROR(N'Không tìm thấy bệnh nhân cần cập nhật!', 16, 1);
        RETURN;
    END;

    UPDATE benh_nhan
    SET ho_ten = @ho_ten,
        ngay_sinh = @ngay_sinh,
        gioi_tinh = @gioi_tinh,
        so_cccd = @so_cccd,
        dia_chi = @dia_chi
    WHERE ma_benh_nhan = @ma_benh_nhan;
END;
GO

-- SP 3: Xóa chỉ định dịch vụ
CREATE OR ALTER PROCEDURE sp_xoa_chi_dinh_dich_vu
    @ma_chi_dinh VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @trang_thai NVARCHAR(30);
    SELECT @trang_thai = trang_thai FROM chi_dinh_dich_vu WHERE ma_chi_dinh = @ma_chi_dinh;

    IF @trang_thai IS NULL
    BEGIN
        RAISERROR(N'Chỉ định dịch vụ không tồn tại!', 16, 1);
        RETURN;
    END;

    IF @trang_thai = N'Đã có kết quả'
    BEGIN
        RAISERROR(N'Dịch vụ đã có kết quả cận lâm sàng, không được phép xóa!', 16, 1);
        RETURN;
    END;

    DELETE FROM chi_dinh_dich_vu WHERE ma_chi_dinh = @ma_chi_dinh;
END;
GO

-- SP 4: Tìm kiếm bệnh nhân phân trang
CREATE OR ALTER PROCEDURE sp_tim_kiem_benh_nhan
    @tu_khoa NVARCHAR(100) = '',
    @page INT = 1,
    @size INT = 10
AS
BEGIN
    SET NOCOUNT ON;
    IF @page < 1 SET @page = 1;
    IF @size < 1 SET @size = 10;
    IF @size > 100 SET @size = 100;

    DECLARE @offset INT = (@page - 1) * @size;

    SELECT 
        ma_benh_nhan,
        ho_ten,
        ngay_sinh,
        gioi_tinh,
        so_dien_thoai,
        ISNULL(so_cccd, N'Chưa có') AS so_cccd,
        ISNULL(dia_chi, N'Chưa có') AS dia_chi,
        ngay_tao
    FROM benh_nhan
    WHERE ho_ten LIKE N'%' + @tu_khoa + N'%'
       OR so_dien_thoai LIKE '%' + @tu_khoa + '%'
       OR so_cccd LIKE '%' + @tu_khoa + '%'
    ORDER BY ngay_tao DESC, ma_benh_nhan DESC
    OFFSET @offset ROWS FETCH NEXT @size ROWS ONLY;
END;
GO

-- SP 5: Thống kê doanh thu theo khoảng ngày
CREATE OR ALTER PROCEDURE sp_thong_ke_doanh_thu_khoang_ngay
    @tu_ngay DATE,
    @den_ngay DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        CAST(ngay_lap AS DATE) AS ngay_giao_dich,
        hinh_thuc_thanh_toan,
        COUNT(ma_hoa_don) AS so_hoa_don,
        SUM(tien_dich_vu) AS doanh_thu_dich_vu,
        SUM(tien_thuoc) AS doanh_thu_thuoc,
        SUM(tien_giam_gia) AS tong_giam_gia,
        SUM(tong_tien) AS thuc_thu
    FROM hoa_don
    WHERE trang_thai = N'Đã thanh toán'
      AND CAST(ngay_lap AS DATE) BETWEEN @tu_ngay AND @den_ngay
    GROUP BY CAST(ngay_lap AS DATE), hinh_thuc_thanh_toan
    ORDER BY ngay_giao_dich ASC;
END;
GO

-- =========================================================================
-- PHẦN 9: TYPE & 5 TRANSACTION PROCEDURES
-- =========================================================================

-- Type danh sách thuốc để kê đơn hàng loạt
IF TYPE_ID('LoaiDanhSachThuoc') IS NOT NULL
    DROP TYPE LoaiDanhSachThuoc;
GO

CREATE TYPE LoaiDanhSachThuoc AS TABLE (
    ma_thuoc VARCHAR(20),
    so_luong INT,
    cach_dung NVARCHAR(100),
    huong_dan NVARCHAR(255)
);
GO

-- Transaction 1: Thanh toán viện phí (Atomic Checkout)
CREATE OR ALTER PROCEDURE sp_gd_thanh_toan_hoa_don
    @ma_ho_so VARCHAR(20),
    @tien_giam_gia DECIMAL(18,2),
    @hinh_thuc_thanh_toan NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @tien_dv DECIMAL(18,2) = dbo.fn_tinh_tong_tien_dich_vu(@ma_ho_so);
        DECLARE @tien_th DECIMAL(18,2) = dbo.fn_tinh_tong_tien_thuoc(@ma_ho_so);
        DECLARE @thuc_thu DECIMAL(18,2) = @tien_dv + @tien_th - @tien_giam_gia;

        IF @thuc_thu < 0 
        BEGIN
            RAISERROR(N'Tiền giảm giá vượt quá tổng viện phí của bệnh nhân!', 16, 1);
        END;

        INSERT INTO hoa_don (ma_ho_so, tien_dich_vu, tien_thuoc, tien_giam_gia, tong_tien, hinh_thuc_thanh_toan, trang_thai)
        VALUES (@ma_ho_so, @tien_dv, @tien_th, @tien_giam_gia, @thuc_thu, @hinh_thuc_thanh_toan, N'Đã thanh toán');

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Transaction 2: Kê đơn nhiều thuốc đồng thời (All-or-Nothing Prescription)
CREATE OR ALTER PROCEDURE sp_gd_ke_nhieu_thuoc
    @ma_ho_so VARCHAR(20),
    @danh_sach_thuoc LoaiDanhSachThuoc READONLY
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        INSERT INTO don_thuoc (ma_ho_so, ma_thuoc, so_luong, cach_dung, huong_dan)
        SELECT @ma_ho_so, ma_thuoc, so_luong, cach_dung, huong_dan
        FROM @danh_sach_thuoc;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Transaction 3: Hủy lịch hẹn
CREATE OR ALTER PROCEDURE sp_gd_huy_lich_hen
    @ma_lich_hen VARCHAR(20),
    @ly_do NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @trang_thai NVARCHAR(30);
        SELECT @trang_thai = trang_thai FROM lich_hen WHERE ma_lich_hen = @ma_lich_hen;

        IF @trang_thai = N'Đã khám'
        BEGIN
            RAISERROR(N'Không thể hủy lịch của ca khám đã hoàn thành!', 16, 1);
        END;

        UPDATE lich_hen
        SET trang_thai = N'Đã hủy', ghi_chu = @ly_do
        WHERE ma_lich_hen = @ma_lich_hen;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Transaction 4: Hủy đơn thuốc và hoàn kho
CREATE OR ALTER PROCEDURE sp_gd_huy_don_thuoc
    @ma_ho_so VARCHAR(20)
WITH EXECUTE AS OWNER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        IF EXISTS (SELECT 1 FROM hoa_don WHERE ma_ho_so = @ma_ho_so AND trang_thai = N'Đã thanh toán')
        BEGIN
            RAISERROR(N'Hóa đơn viện phí đã thanh toán, không được phép xóa đơn thuốc!', 16, 1);
        END;

        DELETE FROM don_thuoc WHERE ma_ho_so = @ma_ho_so;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Transaction 5: Hủy toàn bộ hồ sơ khám bệnh sai sót
CREATE OR ALTER PROCEDURE sp_gd_xoa_ho_so_sai_sot
    @ma_ho_so VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @ma_lich_hen VARCHAR(20);
        SELECT @ma_lich_hen = ma_lich_hen FROM ho_so_kham WHERE ma_ho_so = @ma_ho_so;

        DELETE FROM hoa_don WHERE ma_ho_so = @ma_ho_so AND trang_thai = N'Chưa thanh toán';
        DELETE FROM chi_dinh_dich_vu WHERE ma_ho_so = @ma_ho_so;
        DELETE FROM don_thuoc WHERE ma_ho_so = @ma_ho_so;
        DELETE FROM ho_so_kham WHERE ma_ho_so = @ma_ho_so;

        UPDATE lich_hen SET trang_thai = N'Đang khám' WHERE ma_lich_hen = @ma_lich_hen;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Transaction 6: Nhập kho nhiều loại thuốc đồng thời (Atomic Batch Restock - Recovery Data)
CREATE OR ALTER PROCEDURE sp_gd_nhap_nhieu_thuoc
    @danh_sach_json NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        IF ISJSON(@danh_sach_json) <> 1
        BEGIN
            RAISERROR(N'Dữ liệu danh sách thuốc không đúng định dạng JSON!', 16, 1);
        END;

        DECLARE @tbl TABLE (
            id INT IDENTITY(1,1),
            ma_thuoc VARCHAR(20),
            ten_thuoc NVARCHAR(100),
            so_luong_str NVARCHAR(100)
        );

        INSERT INTO @tbl (ma_thuoc, ten_thuoc, so_luong_str)
        SELECT 
            j.maThuoc,
            ISNULL(t.ten_thuoc, j.maThuoc),
            j.soLuong
        FROM OPENJSON(@danh_sach_json)
        WITH (
            maThuoc VARCHAR(20) '$.maThuoc',
            soLuong NVARCHAR(100) '$.soLuong'
        ) j
        LEFT JOIN thuoc t ON j.maThuoc = t.ma_thuoc;

        IF NOT EXISTS (SELECT 1 FROM @tbl)
        BEGIN
            RAISERROR(N'Danh sách thuốc nhập vào không được để trống!', 16, 1);
        END;

        -- 1. Kiểm tra mã thuốc tồn tại
        IF EXISTS (SELECT 1 FROM @tbl WHERE ma_thuoc NOT IN (SELECT ma_thuoc FROM thuoc))
        BEGIN
            DECLARE @ma_khong_ton_tai VARCHAR(20);
            SELECT TOP 1 @ma_khong_ton_tai = ma_thuoc FROM @tbl WHERE ma_thuoc NOT IN (SELECT ma_thuoc FROM thuoc);
            RAISERROR(N'Mã thuốc [%s] không tồn tại trong hệ thống! Toàn bộ giao dịch bị hủy bỏ (Rollback).', 16, 1, @ma_khong_ton_tai);
        END;

        -- 2. Kiểm tra nếu có thuốc nhập chữ (không thể cast thành số nguyên)
        DECLARE @ma_loi_chu VARCHAR(20), @ten_loi_chu NVARCHAR(100), @chu_loi NVARCHAR(100);
        SELECT TOP 1 @ma_loi_chu = ma_thuoc, @ten_loi_chu = ten_thuoc, @chu_loi = so_luong_str
        FROM @tbl
        WHERE TRY_CAST(so_luong_str AS INT) IS NULL;

        IF @ma_loi_chu IS NOT NULL
        BEGIN
            RAISERROR(N'Lỗi dữ liệu đầu vào: Thuốc [%s - %s] có số lượng nhập là ký tự chữ ("%s") không hợp lệ! Giao dịch đã bị ROLLBACK hoàn toàn để bảo vệ dữ liệu.', 16, 1, @ma_loi_chu, @ten_loi_chu, @chu_loi);
        END;

        -- 3. Kiểm tra nếu có thuốc nhập số âm hoặc bằng 0
        DECLARE @ma_loi_am VARCHAR(20), @ten_loi_am NVARCHAR(100), @sl_am INT;
        SELECT TOP 1 @ma_loi_am = ma_thuoc, @ten_loi_am = ten_thuoc, @sl_am = CAST(so_luong_str AS INT)
        FROM @tbl
        WHERE CAST(so_luong_str AS INT) <= 0;

        IF @ma_loi_am IS NOT NULL
        BEGIN
            DECLARE @sl_am_str NVARCHAR(20) = CAST(@sl_am AS NVARCHAR(20));
            RAISERROR(N'Lỗi nghiệp vụ nhập kho: Thuốc [%s - %s] có số lượng nhập là số âm hoặc bằng 0 ("%s")! Giao dịch đã bị ROLLBACK hoàn toàn để bảo vệ dữ liệu.', 16, 1, @ma_loi_am, @ten_loi_am, @sl_am_str);
        END;

        -- 4. Tất cả hợp lệ -> Cập nhật tăng tồn kho từng thuốc
        UPDATE t
        SET t.so_luong_ton = t.so_luong_ton + CAST(b.so_luong_str AS INT)
        FROM thuoc t
        JOIN @tbl b ON t.ma_thuoc = b.ma_thuoc;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO


-- =========================================================================
-- PHẦN 10: 5 NON-CLUSTERED INDEXES
-- =========================================================================
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_benh_nhan_so_dien_thoai' AND object_id = OBJECT_ID('benh_nhan'))
    CREATE NONCLUSTERED INDEX idx_benh_nhan_so_dien_thoai ON benh_nhan (so_dien_thoai) INCLUDE (ho_ten, ngay_sinh, gioi_tinh);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_lich_hen_ngay_trangthai' AND object_id = OBJECT_ID('lich_hen'))
    CREATE NONCLUSTERED INDEX idx_lich_hen_ngay_trangthai ON lich_hen (thoi_gian_hen, trang_thai) INCLUDE (ma_benh_nhan, ma_bac_si, so_thu_tu);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_ho_so_kham_benh_nhan_ngay' AND object_id = OBJECT_ID('ho_so_kham'))
    CREATE NONCLUSTERED INDEX idx_ho_so_kham_benh_nhan_ngay ON ho_so_kham (ma_benh_nhan, ngay_kham DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_don_thuoc_ma_ho_so' AND object_id = OBJECT_ID('don_thuoc'))
    CREATE NONCLUSTERED INDEX idx_don_thuoc_ma_ho_so ON don_thuoc (ma_ho_so) INCLUDE (ma_thuoc, so_luong);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_hoa_don_trang_thai_ngay' AND object_id = OBJECT_ID('hoa_don'))
    CREATE NONCLUSTERED INDEX idx_hoa_don_trang_thai_ngay ON hoa_don (trang_thai, ngay_lap) INCLUDE (tong_tien);
GO

-- =========================================================================
-- PHẦN 11: BẢO MẬT, TẠO LOGINS, USERS & ROLES
-- =========================================================================

-- 1. Tạo 4 Roles
IF DATABASE_PRINCIPAL_ID('vai_tro_quan_tri') IS NULL CREATE ROLE vai_tro_quan_tri;
IF DATABASE_PRINCIPAL_ID('vai_tro_bac_si') IS NULL CREATE ROLE vai_tro_bac_si;
IF DATABASE_PRINCIPAL_ID('vai_tro_le_tan') IS NULL CREATE ROLE vai_tro_le_tan;
IF DATABASE_PRINCIPAL_ID('vai_tro_duoc_si') IS NULL CREATE ROLE vai_tro_duoc_si;
GO

-- 2. Tạo Logins ở cấp máy chủ SQL Server
IF SUSER_ID('tk_quan_tri') IS NULL CREATE LOGIN tk_quan_tri WITH PASSWORD = 'QuanTriMatKhau123!', CHECK_POLICY = OFF;
IF SUSER_ID('tk_bac_si1') IS NULL CREATE LOGIN tk_bac_si1 WITH PASSWORD = 'BacSiMatKhau123!', CHECK_POLICY = OFF;
IF SUSER_ID('tk_le_tan1') IS NULL CREATE LOGIN tk_le_tan1 WITH PASSWORD = 'LeTanMatKhau123!', CHECK_POLICY = OFF;
IF SUSER_ID('tk_duoc_si1') IS NULL CREATE LOGIN tk_duoc_si1 WITH PASSWORD = 'DuocSiMatKhau123!', CHECK_POLICY = OFF;
IF SUSER_ID('tk_xac_thuc') IS NULL CREATE LOGIN tk_xac_thuc WITH PASSWORD = 'AuthServicePass123!', CHECK_POLICY = OFF;
GO

-- 3. Tạo Database Users
IF USER_ID('nguoi_dung_quan_tri') IS NULL CREATE USER nguoi_dung_quan_tri FOR LOGIN tk_quan_tri;
IF USER_ID('nguoi_dung_bac_si1') IS NULL CREATE USER nguoi_dung_bac_si1 FOR LOGIN tk_bac_si1;
IF USER_ID('nguoi_dung_le_tan1') IS NULL CREATE USER nguoi_dung_le_tan1 FOR LOGIN tk_le_tan1;
IF USER_ID('nguoi_dung_duoc_si1') IS NULL CREATE USER nguoi_dung_duoc_si1 FOR LOGIN tk_duoc_si1;
IF USER_ID('nguoi_dung_xac_thuc') IS NULL CREATE USER nguoi_dung_xac_thuc FOR LOGIN tk_xac_thuc;
GO

-- 4. Gán User vào Roles
ALTER ROLE vai_tro_quan_tri ADD MEMBER nguoi_dung_quan_tri;
ALTER ROLE vai_tro_bac_si ADD MEMBER nguoi_dung_bac_si1;
ALTER ROLE vai_tro_le_tan ADD MEMBER nguoi_dung_le_tan1;
ALTER ROLE vai_tro_duoc_si ADD MEMBER nguoi_dung_duoc_si1;
GO

-- =========================================================================
-- PHẦN 12: THIẾT LẬP PHÂN QUYỀN CHI TIẾT (GRANT / REVOKE / DENY)
-- =========================================================================

-- Quản trị viên: Toàn quyền
GRANT CONTROL ON DATABASE::QuanLyPhongKhamDB TO vai_tro_quan_tri;
GRANT SELECT ON v_doanh_thu_theo_ngay TO vai_tro_quan_tri;
GRANT SELECT ON v_doanh_thu_theo_bac_si TO vai_tro_quan_tri;
GRANT EXECUTE ON sp_thong_ke_doanh_thu_khoang_ngay TO vai_tro_quan_tri;

-- Dịch vụ xác thực đăng nhập
GRANT SELECT ON nguoi_dung TO nguoi_dung_xac_thuc;
GRANT SELECT ON bac_si TO nguoi_dung_xac_thuc;

-- Bác sĩ
GRANT SELECT ON benh_nhan TO vai_tro_bac_si;
GRANT SELECT ON dich_vu TO vai_tro_bac_si;
GRANT SELECT ON thuoc TO vai_tro_bac_si;
GRANT SELECT ON lich_hen TO vai_tro_bac_si;
GRANT SELECT ON bac_si TO vai_tro_bac_si;
GRANT SELECT ON nguoi_dung TO vai_tro_bac_si;
GRANT INSERT, UPDATE, SELECT ON ho_so_kham TO vai_tro_bac_si;
GRANT INSERT, UPDATE, DELETE, SELECT ON don_thuoc TO vai_tro_bac_si;
GRANT INSERT, UPDATE, DELETE, SELECT ON chi_dinh_dich_vu TO vai_tro_bac_si;
GRANT INSERT, UPDATE ON lich_hen TO vai_tro_bac_si;
GRANT EXECUTE ON sp_cap_nhat_benh_nhan TO vai_tro_bac_si;
GRANT EXECUTE ON sp_xoa_chi_dinh_dich_vu TO vai_tro_bac_si;
GRANT EXECUTE ON sp_gd_ke_nhieu_thuoc TO vai_tro_bac_si;
GRANT SELECT ON v_chi_tiet_kham_benh TO vai_tro_bac_si;
GRANT SELECT ON fn_tim_thuoc_kha_dung TO vai_tro_bac_si;
GRANT SELECT ON fn_lich_su_kham_benh TO vai_tro_bac_si;
GRANT UPDATE ON SEQUENCE::seq_ho_so TO vai_tro_bac_si;
GRANT UPDATE ON SEQUENCE::seq_chi_dinh TO vai_tro_bac_si;
GRANT UPDATE ON SEQUENCE::seq_don_thuoc TO vai_tro_bac_si;
GRANT UPDATE ON SEQUENCE::seq_lich_hen TO vai_tro_bac_si;
DENY INSERT, UPDATE, DELETE ON hoa_don TO vai_tro_bac_si;
DENY INSERT, UPDATE, DELETE ON nguoi_dung TO vai_tro_bac_si;

-- Lễ tân / Thu ngân
GRANT SELECT, INSERT, UPDATE ON benh_nhan TO vai_tro_le_tan;
GRANT SELECT, INSERT, UPDATE ON lich_hen TO vai_tro_le_tan;
GRANT SELECT, INSERT, UPDATE ON hoa_don TO vai_tro_le_tan;
GRANT SELECT ON dich_vu TO vai_tro_le_tan;
GRANT SELECT ON bac_si TO vai_tro_le_tan;
GRANT SELECT ON nguoi_dung TO vai_tro_le_tan;
GRANT SELECT ON chi_dinh_dich_vu TO vai_tro_le_tan;
GRANT SELECT ON don_thuoc TO vai_tro_le_tan;
GRANT SELECT ON nhat_ky_gia_thuoc TO vai_tro_le_tan;
GRANT SELECT ON v_hang_cho_kham_hom_nay TO vai_tro_le_tan;
GRANT SELECT ON v_doanh_thu_theo_ngay TO vai_tro_le_tan;
GRANT EXECUTE ON sp_tiep_nhan_kham TO vai_tro_le_tan;
GRANT EXECUTE ON sp_tim_kiem_benh_nhan TO vai_tro_le_tan;
GRANT EXECUTE ON sp_gd_thanh_toan_hoa_don TO vai_tro_le_tan;
GRANT EXECUTE ON sp_gd_huy_lich_hen TO vai_tro_le_tan;
GRANT EXECUTE ON sp_gd_huy_don_thuoc TO vai_tro_le_tan;
GRANT EXECUTE ON fn_tinh_tuoi TO vai_tro_le_tan;
GRANT EXECUTE ON fn_tinh_tong_tien_thuoc TO vai_tro_le_tan;
GRANT EXECUTE ON fn_tinh_tong_tien_dich_vu TO vai_tro_le_tan;
GRANT UPDATE ON SEQUENCE::seq_benh_nhan TO vai_tro_le_tan;
GRANT UPDATE ON SEQUENCE::seq_lich_hen TO vai_tro_le_tan;
GRANT UPDATE ON SEQUENCE::seq_hoa_don TO vai_tro_le_tan;
DENY SELECT ON ho_so_kham(chan_doan, loi_dan) TO vai_tro_le_tan;
DENY INSERT, UPDATE, DELETE ON don_thuoc TO vai_tro_le_tan;

-- Dược sĩ
GRANT SELECT, INSERT, UPDATE, DELETE ON thuoc TO vai_tro_duoc_si;
GRANT SELECT, INSERT ON nhat_ky_gia_thuoc TO vai_tro_duoc_si;
GRANT SELECT ON don_thuoc TO vai_tro_duoc_si;
GRANT SELECT ON benh_nhan TO vai_tro_duoc_si;
GRANT SELECT ON bac_si TO vai_tro_duoc_si;
GRANT SELECT ON nguoi_dung TO vai_tro_duoc_si;
GRANT SELECT ON v_canh_bao_ton_kho_thuoc TO vai_tro_duoc_si;
GRANT EXECUTE ON sp_gd_nhap_nhieu_thuoc TO vai_tro_duoc_si;
GRANT UPDATE ON SEQUENCE::seq_thuoc TO vai_tro_duoc_si;
DENY INSERT, UPDATE, DELETE ON lich_hen TO vai_tro_duoc_si;
DENY INSERT, UPDATE, DELETE ON ho_so_kham TO vai_tro_duoc_si;
DENY INSERT, UPDATE, DELETE ON hoa_don TO vai_tro_duoc_si;
GO

PRINT N'>>> CHÚC MỪNG: BẠN ĐÃ KHỞI TẠO TOÀN BỘ CƠ SỞ DỮ LIỆU QUANLYPHONGKHAMDB THÀNH CÔNG 100%! <<<';
GO
