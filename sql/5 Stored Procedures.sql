USE QuanLyPhongKhamDB;
GO

-- 1. THÊM: Tiếp nhận bệnh nhân & Đăng ký khám (Tự động cấp STT theo bác sĩ trong ngày)
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
            
            -- Kiểm tra xem bệnh nhân đã từng khám chưa qua số điện thoại hoặc CCCD
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

            -- Tính số thứ tự tiếp theo cho phòng của bác sĩ trong ngày
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

-- 2. SỬA: Cập nhật thông tin hành chính của bệnh nhân
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
    BEGIN TRY
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
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH;
END;
GO

-- 3. XÓA: Hủy một chỉ định dịch vụ cận lâm sàng chưa thực hiện
CREATE OR ALTER PROCEDURE sp_xoa_chi_dinh_dich_vu
    @ma_chi_dinh VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
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
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH;
END;
GO

-- 4. TÌM KIẾM: Tra cứu danh sách bệnh nhân theo họ tên hoặc CCCD/SĐT (hỗ trợ phân trang)
CREATE OR ALTER PROCEDURE sp_tim_kiem_benh_nhan
    @tu_khoa NVARCHAR(100) = '',
    @page INT = 1,
    @size INT = 10
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
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
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH;
END;
GO

-- 5. THỐNG KÊ: Báo cáo doanh thu và lưu lượng khám bệnh theo khoảng ngày
CREATE OR ALTER PROCEDURE sp_thong_ke_doanh_thu_khoang_ngay
    @tu_ngay DATE,
    @den_ngay DATE
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
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
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH;
END;
GO