USE QuanLyPhongKhamDB;
GO

-- 1. Tạo 4 Roles trong Database
IF DATABASE_PRINCIPAL_ID('vai_tro_quan_tri') IS NULL CREATE ROLE vai_tro_quan_tri;
IF DATABASE_PRINCIPAL_ID('vai_tro_bac_si') IS NULL CREATE ROLE vai_tro_bac_si;
IF DATABASE_PRINCIPAL_ID('vai_tro_le_tan') IS NULL CREATE ROLE vai_tro_le_tan;
IF DATABASE_PRINCIPAL_ID('vai_tro_duoc_si') IS NULL CREATE ROLE vai_tro_duoc_si;
GO

-- 2. Tạo Logins ở cấp máy chủ SQL Server (Server Principals)
IF SUSER_ID('tk_quan_tri') IS NULL CREATE LOGIN tk_quan_tri WITH PASSWORD = 'QuanTriMatKhau123!', CHECK_POLICY = OFF;
IF SUSER_ID('tk_bac_si1') IS NULL CREATE LOGIN tk_bac_si1 WITH PASSWORD = 'BacSiMatKhau123!', CHECK_POLICY = OFF;
IF SUSER_ID('tk_le_tan1') IS NULL CREATE LOGIN tk_le_tan1 WITH PASSWORD = 'LeTanMatKhau123!', CHECK_POLICY = OFF;
IF SUSER_ID('tk_duoc_si1') IS NULL CREATE LOGIN tk_duoc_si1 WITH PASSWORD = 'DuocSiMatKhau123!', CHECK_POLICY = OFF;
GO

-- 3. Tạo Database Users liên kết với Logins
IF USER_ID('nguoi_dung_quan_tri') IS NULL CREATE USER nguoi_dung_quan_tri FOR LOGIN tk_quan_tri;
IF USER_ID('nguoi_dung_bac_si1') IS NULL CREATE USER nguoi_dung_bac_si1 FOR LOGIN tk_bac_si1;
IF USER_ID('nguoi_dung_le_tan1') IS NULL CREATE USER nguoi_dung_le_tan1 FOR LOGIN tk_le_tan1;
IF USER_ID('nguoi_dung_duoc_si1') IS NULL CREATE USER nguoi_dung_duoc_si1 FOR LOGIN tk_duoc_si1;
GO

-- 4. Gán User vào Roles tương ứng
ALTER ROLE vai_tro_quan_tri ADD MEMBER nguoi_dung_quan_tri;
ALTER ROLE vai_tro_bac_si ADD MEMBER nguoi_dung_bac_si1;
ALTER ROLE vai_tro_le_tan ADD MEMBER nguoi_dung_le_tan1;
ALTER ROLE vai_tro_duoc_si ADD MEMBER nguoi_dung_duoc_si1;
GO