USE QuanLyPhongKhamDB;
GO

-- Nghiệp vụ 1: Lập hóa đơn và Khóa sổ thanh toán (Atomic Checkout)
DROP PROCEDURE IF EXISTS dbo.sp_gd_thanh_toan_hoa_don;
GO

CREATE PROCEDURE dbo.sp_gd_thanh_toan_hoa_don
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

-- Nghiệp vụ 2: Kê đơn nhiều loại thuốc cùng lúc (All-or-Nothing Prescription)
DROP PROCEDURE IF EXISTS dbo.sp_gd_ke_nhieu_thuoc;
GO

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

CREATE PROCEDURE dbo.sp_gd_ke_nhieu_thuoc
    @ma_ho_so VARCHAR(20),
    @danh_sach_thuoc LoaiDanhSachThuoc READONLY
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- Chèn từng thuốc vào đơn, trigger trg_ke_don_tru_kho sẽ kích hoạt kiểm tra kho
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

-- Nghiệp vụ 3: Hủy lịch hẹn và giải phóng trạng thái hàng đợi
DROP PROCEDURE IF EXISTS dbo.sp_gd_huy_lich_hen;
GO

CREATE PROCEDURE dbo.sp_gd_huy_lich_hen
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

-- Nghiệp vụ 4: Hủy đơn thuốc sai và hoàn kho tự động
DROP PROCEDURE IF EXISTS dbo.sp_gd_huy_don_thuoc;
GO

CREATE PROCEDURE dbo.sp_gd_huy_don_thuoc
    @ma_ho_so VARCHAR(20)
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

        -- Khi xóa, trigger trg_xoa_don_hoan_kho tự động cộng trả số lượng vào bảng thuoc
        DELETE FROM don_thuoc WHERE ma_ho_so = @ma_ho_so;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Nghiệp vụ 5: Hủy toàn bộ hồ sơ khám bệnh sai sót (Cascade Reversal)
DROP PROCEDURE IF EXISTS dbo.sp_gd_xoa_ho_so_sai_sot;
GO

CREATE PROCEDURE dbo.sp_gd_xoa_ho_so_sai_sot
    @ma_ho_so VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @ma_lich_hen VARCHAR(20);
        SELECT @ma_lich_hen = ma_lich_hen FROM ho_so_kham WHERE ma_ho_so = @ma_ho_so;

        -- 1. Xóa hóa đơn chưa thanh toán (nếu có)
        DELETE FROM hoa_don WHERE ma_ho_so = @ma_ho_so AND trang_thai = N'Chưa thanh toán';

        -- 2. Hủy các chỉ định cận lâm sàng
        DELETE FROM chi_dinh_dich_vu WHERE ma_ho_so = @ma_ho_so;

        -- 3. Hủy đơn thuốc (hoàn lại tồn kho)
        DELETE FROM don_thuoc WHERE ma_ho_so = @ma_ho_so;

        -- 4. Xóa hồ sơ bệnh án
        DELETE FROM ho_so_kham WHERE ma_ho_so = @ma_ho_so;

        -- 5. Đưa trạng thái lịch hẹn trở về 'Đang khám' để bác sĩ thao tác lại
        UPDATE lich_hen SET trang_thai = N'Đang khám' WHERE ma_lich_hen = @ma_lich_hen;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Nghiệp vụ 6: Nhập kho nhiều loại thuốc đồng thời (Atomic Batch Restock - Recovery Data)
DROP PROCEDURE IF EXISTS dbo.sp_gd_nhap_nhieu_thuoc;
GO

CREATE PROCEDURE dbo.sp_gd_nhap_nhieu_thuoc
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

GRANT EXECUTE ON dbo.sp_gd_nhap_nhieu_thuoc TO vai_tro_duoc_si;
GRANT EXECUTE ON dbo.sp_gd_nhap_nhieu_thuoc TO vai_tro_quan_tri;
GO