USE QuanLyPhongKhamDB;
GO

-- 1. Tạo Login & User riêng cho dịch vụ xác thực
IF SUSER_ID('tk_xac_thuc') IS NULL
    CREATE LOGIN tk_xac_thuc WITH PASSWORD = 'AuthServicePass123!', CHECK_POLICY = OFF;
GO

IF USER_ID('nguoi_dung_xac_thuc') IS NULL
    CREATE USER nguoi_dung_xac_thuc FOR LOGIN tk_xac_thuc;
GO

-- 2. Cấp quyền đọc các bảng cần thiết cho dịch vụ xác thực tài khoản và thông tin bác sĩ
GRANT SELECT ON nguoi_dung TO nguoi_dung_xac_thuc;
GRANT SELECT ON bac_si TO nguoi_dung_xac_thuc;
GO