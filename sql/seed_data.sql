USE QuanLyPhongKhamDB;
GO

-- Xoa du lieu cu neu co (theo thu tu khoa ngoai)
DELETE FROM nhat_ky_gia_thuoc;
DELETE FROM hoa_don;
DELETE FROM don_thuoc;
DELETE FROM chi_dinh_dich_vu;
DELETE FROM ho_so_kham;
DELETE FROM lich_hen;
DELETE FROM thuoc;
DELETE FROM dich_vu;
DELETE FROM bac_si;
DELETE FROM benh_nhan;
DELETE FROM nguoi_dung;
GO

-- 1. Tao nguoi dung voi Ma ND001, BS001, ND003, ND004 (Mat khau '123456' da duoc bam bang BCrypt)
INSERT INTO nguoi_dung (ma_nguoi_dung, ten_dang_nhap, mat_khau_hash, ho_ten, so_dien_thoai, vai_tro, hoat_dong) VALUES
('ND001', 'admin', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'Quản Trị Viên', '0901000001', 'QUAN_TRI', 1),
('BS001', 'bacsi1', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'BS. Nguyễn Văn A', '0901000002', 'BAC_SI', 1),
('ND003', 'letan1', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'Lê Thị Lễ Tân', '0901000003', 'LE_TAN', 1),
('ND004', 'duocsi1', '$2a$10$CbWVqBZFDDI0hQXkqpPzhukEsnnzoWZJdjoE6ozNIAKNZNKcCT5oW', N'Trần Văn Dược', '0901000004', 'DUOC_SI', 1);

-- 2. Tao bac si voi Ma BS001
INSERT INTO bac_si (ma_bac_si, chuyen_khoa, so_chung_chi, phong_kham) VALUES
('BS001', N'Nội tổng quát', 'CCHN-001', 'P.101');

-- 3. Tao benh nhan mau (Chu y dung N'' cho chu co dau Unicode)
INSERT INTO benh_nhan (ma_benh_nhan, ho_ten, ngay_sinh, gioi_tinh, so_dien_thoai, so_cccd, dia_chi) VALUES
('BN001', N'Nguyễn Thị Lan', '1995-05-15', N'Nữ', '0988111222', '079195000001', N'123 Lê Lợi, Q.1, TP.HCM'),
('BN002', N'Trần Văn Nam', '1988-10-20', N'Nam', '0977333444', '079188000002', N'456 Nguyễn Huệ, Q.1, TP.HCM');

-- 4. Tao danh muc thuoc mau
INSERT INTO thuoc (ma_thuoc, ten_thuoc, don_vi_tinh, so_luong_ton, don_gia, han_su_dung, trang_thai) VALUES
('T001', N'Paracetamol 500mg', N'Viên', 100, 2000, '2027-12-31', 1),
('T002', N'Amoxicillin 500mg', N'Viên', 50, 5000, '2027-12-31', 1),
('T003', N'Vitamin C 500mg', N'Viên', 10, 1500, '2027-06-30', 1);

-- 5. Tao danh muc dich vu mau
INSERT INTO dich_vu (ma_dich_vu, ten_dich_vu, don_gia, mo_ta, trang_thai) VALUES
('DV001', N'Khám nội tổng quát', 150000, N'Khám lâm sàng', 1),
('DV002', N'Xét nghiệm máu toàn phần', 200000, N'Công thức máu 18 thông số', 1),
('DV003', N'Chụp X-quang lồng ngực', 250000, N'Chẩn đoán hình ảnh phổi và tim', 1);

-- 6. Tao lich hen cho hom nay
INSERT INTO lich_hen (ma_lich_hen, ma_benh_nhan, ma_bac_si, thoi_gian_hen, so_thu_tu, trang_thai, loai_lich_hen, ghi_chu) VALUES
('LH001', 'BN001', 'BS001', GETDATE(), 1, N'Chờ khám', N'Khám mới', N'Bệnh nhân đau đầu sốt nhẹ');
GO
