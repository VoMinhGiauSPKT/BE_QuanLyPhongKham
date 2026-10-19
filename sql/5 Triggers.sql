USE QuanLyPhongKhamDB;
GO

-- Trigger 1: Tự động trừ tồn kho khi bác sĩ kê đơn thuốc
CREATE OR ALTER TRIGGER trg_ke_don_tru_kho
ON don_thuoc
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Kiểm tra số lượng tồn còn đủ không
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

    -- Trừ kho thuốc
    UPDATE t
    SET t.so_luong_ton = t.so_luong_ton - i.so_luong
    FROM thuoc t
    JOIN inserted i ON t.ma_thuoc = i.ma_thuoc;
END;
GO

-- Trigger 2: Chặn kê đơn các loại thuốc đã quá hạn sử dụng
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

-- Trigger 3: Hoàn lại tồn kho khi đơn thuốc bị chỉnh sửa giảm hoặc hủy
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

-- Trigger 4: Tự động đổi trạng thái lịch hẹn thành 'Đã khám' khi bác sĩ hoàn thành chẩn đoán
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

-- Tạo bảng lưu nhật ký biến động giá thuốc (nếu chưa có)
IF OBJECT_ID('nhat_ky_gia_thuoc') IS NULL
BEGIN
    CREATE TABLE nhat_ky_gia_thuoc (
        ma_nhat_ky INT IDENTITY(1,1) PRIMARY KEY,
        ma_thuoc VARCHAR(20) NOT NULL,
        gia_cu DECIMAL(18,2) NOT NULL,
        gia_moi DECIMAL(18,2) NOT NULL,
        thoi_gian DATETIME NOT NULL DEFAULT GETDATE(),
        nguoi_thuc_hien NVARCHAR(100) NOT NULL DEFAULT SYSTEM_USER,
        CONSTRAINT FK_nhat_ky_thuoc FOREIGN KEY (ma_thuoc) REFERENCES thuoc(ma_thuoc) ON DELETE CASCADE
    );
END
GO

-- Trigger 5: Ghi nhật ký (Audit Log) mỗi khi có biến động đơn giá thuốc
CREATE OR ALTER TRIGGER trg_ghi_nhat_ky_gia_thuoc
ON thuoc
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    IF UPDATE(don_gia)
    BEGIN
        INSERT INTO nhat_ky_gia_thuoc (ma_thuoc, gia_cu, gia_moi)
        SELECT d.ma_thuoc, d.don_gia, i.don_gia
        FROM deleted d
        JOIN inserted i ON d.ma_thuoc = i.ma_thuoc
        WHERE d.don_gia <> i.don_gia;
    END;
END;
GO