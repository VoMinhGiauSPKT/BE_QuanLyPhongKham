USE QuanLyPhongKhamDB;
GO

-- Chỉ mục 1: Tìm kiếm bệnh nhân theo số điện thoại
-- Lý do: Tiếp tân tra cứu thông tin bệnh nhân qua SĐT mỗi khi đón tiếp bệnh nhân đến khám.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_benh_nhan_so_dien_thoai' AND object_id = OBJECT_ID('benh_nhan'))
    CREATE NONCLUSTERED INDEX idx_benh_nhan_so_dien_thoai 
    ON benh_nhan (so_dien_thoai) 
    INCLUDE (ho_ten, ngay_sinh, gioi_tinh);
GO

-- Chỉ mục 2: Lọc danh sách lịch hẹn theo thời gian và trạng thái
-- Lý do: Giúp truy vấn hàng đợi ngày hôm nay (v_hang_cho_kham_hom_nay) lọc cực nhanh mà không phải duyệt toàn bảng.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_lich_hen_ngay_trangthai' AND object_id = OBJECT_ID('lich_hen'))
    CREATE NONCLUSTERED INDEX idx_lich_hen_ngay_trangthai 
    ON lich_hen (thoi_gian_hen, trang_thai) 
    INCLUDE (ma_benh_nhan, ma_bac_si, so_thu_tu);
GO

-- Chỉ mục 3: Tra cứu lịch sử khám bệnh theo mã bệnh nhân sắp xếp theo ngày
-- Lý do: Bác sĩ mở hồ sơ cũ của bệnh nhân liên tục (WHERE ma_benh_nhan = @id ORDER BY ngay_kham DESC).
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_ho_so_kham_benh_nhan_ngay' AND object_id = OBJECT_ID('ho_so_kham'))
    CREATE NONCLUSTERED INDEX idx_ho_so_kham_benh_nhan_ngay 
    ON ho_so_kham (ma_benh_nhan, ngay_kham DESC);
GO

-- Chỉ mục 4: Nối đơn thuốc theo mã hồ sơ khám bệnh
-- Lý do: Tối ưu phép JOIN giữa bảng ho_so_kham và don_thuoc khi in đơn thuốc và kết toán hóa đơn.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_don_thuoc_ma_ho_so' AND object_id = OBJECT_ID('don_thuoc'))
    CREATE NONCLUSTERED INDEX idx_don_thuoc_ma_ho_so 
    ON don_thuoc (ma_ho_so) 
    INCLUDE (ma_thuoc, so_luong);
GO

-- Chỉ mục 5: Lọc hóa đơn theo trạng thái thanh toán và thời gian lập
-- Lý do: Tối ưu cho các báo cáo doanh thu tài chính cuối ngày (WHERE trang_thai = N'Đã thanh toán').
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_hoa_don_trang_thai_ngay' AND object_id = OBJECT_ID('hoa_don'))
    CREATE NONCLUSTERED INDEX idx_hoa_don_trang_thai_ngay 
    ON hoa_don (trang_thai, ngay_lap) 
    INCLUDE (tong_tien);
GO