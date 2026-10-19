USE QuanLyPhongKhamDB;
GO

-- Hàm 1 (Scalar): Tính chính xác số tuổi của bệnh nhân từ ngày sinh
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

-- Hàm 2 (Scalar): Tính tổng giá trị tiền thuốc theo hồ sơ khám
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

-- Hàm 3 (Scalar): Tính tổng chi phí chỉ định dịch vụ kỹ thuật theo hồ sơ
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

-- Hàm 4 (Table-Valued): Tra cứu toàn bộ lịch sử bệnh án của bệnh nhân
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

-- Hàm 5 (Table-Valued): Tra cứu danh mục thuốc còn tồn kho và còn hạn sử dụng
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