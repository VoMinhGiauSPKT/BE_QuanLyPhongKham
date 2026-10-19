package com.mycompany.quanlyphongkham.test;

import com.mycompany.quanlyphongkham.util.DBContext;
import java.sql.Connection;
import java.sql.Statement;

public class ExecuteScriptRDS {
    public static void main(String[] args) {
        System.out.println("=== BAT DAU CAP NHAT PROCEDURE sp_gd_nhap_nhieu_thuoc TREN AWS RDS ===");
        
        String sqlCreateProcedure = 
            "CREATE OR ALTER PROCEDURE dbo.sp_gd_nhap_nhieu_thuoc\n" +
            "    @danh_sach_json NVARCHAR(MAX)\n" +
            "AS\n" +
            "BEGIN\n" +
            "    SET NOCOUNT ON;\n" +
            "    SET XACT_ABORT ON;\n" +
            "    BEGIN TRANSACTION;\n" +
            "    BEGIN TRY\n" +
            "        IF ISJSON(@danh_sach_json) <> 1\n" +
            "        BEGIN\n" +
            "            RAISERROR(N'Dữ liệu danh sách thuốc không đúng định dạng JSON!', 16, 1);\n" +
            "        END;\n" +
            "\n" +
            "        DECLARE @tbl TABLE (\n" +
            "            id INT IDENTITY(1,1),\n" +
            "            ma_thuoc VARCHAR(20),\n" +
            "            ten_thuoc NVARCHAR(100),\n" +
            "            so_luong_str NVARCHAR(100)\n" +
            "        );\n" +
            "\n" +
            "        INSERT INTO @tbl (ma_thuoc, ten_thuoc, so_luong_str)\n" +
            "        SELECT \n" +
            "            j.maThuoc,\n" +
            "            ISNULL(t.ten_thuoc, j.maThuoc),\n" +
            "            j.soLuong\n" +
            "        FROM OPENJSON(@danh_sach_json)\n" +
            "        WITH (\n" +
            "            maThuoc VARCHAR(20) '$.maThuoc',\n" +
            "            soLuong NVARCHAR(100) '$.soLuong'\n" +
            "        ) j\n" +
            "        LEFT JOIN thuoc t ON j.maThuoc = t.ma_thuoc;\n" +
            "\n" +
            "        IF NOT EXISTS (SELECT 1 FROM @tbl)\n" +
            "        BEGIN\n" +
            "            RAISERROR(N'Danh sách thuốc nhập vào không được để trống!', 16, 1);\n" +
            "        END;\n" +
            "\n" +
            "        -- 1. Kiem tra ma thuoc ton tai\n" +
            "        IF EXISTS (SELECT 1 FROM @tbl WHERE ma_thuoc NOT IN (SELECT ma_thuoc FROM thuoc))\n" +
            "        BEGIN\n" +
            "            DECLARE @ma_khong_ton_tai VARCHAR(20);\n" +
            "            SELECT TOP 1 @ma_khong_ton_tai = ma_thuoc FROM @tbl WHERE ma_thuoc NOT IN (SELECT ma_thuoc FROM thuoc);\n" +
            "            RAISERROR(N'Mã thuốc [%s] không tồn tại trong hệ thống! Toàn bộ giao dịch bị hủy bỏ (Rollback).', 16, 1, @ma_khong_ton_tai);\n" +
            "        END;\n" +
            "\n" +
            "        -- 2. Kiem tra neu co thuoc nhap chu (khong the cast thanh so nguyen)\n" +
            "        DECLARE @ma_loi_chu VARCHAR(20), @ten_loi_chu NVARCHAR(100), @chu_loi NVARCHAR(100);\n" +
            "        SELECT TOP 1 @ma_loi_chu = ma_thuoc, @ten_loi_chu = ten_thuoc, @chu_loi = so_luong_str\n" +
            "        FROM @tbl\n" +
            "        WHERE TRY_CAST(so_luong_str AS INT) IS NULL;\n" +
            "\n" +
            "        IF @ma_loi_chu IS NOT NULL\n" +
            "        BEGIN\n" +
            "            RAISERROR(N'Lỗi dữ liệu đầu vào: Thuốc [%s - %s] có số lượng nhập là ký tự chữ (\"%s\") không hợp lệ! Giao dịch đã bị ROLLBACK hoàn toàn để bảo vệ dữ liệu.', 16, 1, @ma_loi_chu, @ten_loi_chu, @chu_loi);\n" +
            "        END;\n" +
            "\n" +
            "        -- 3. Kiem tra neu co thuoc nhap so am hoac bang 0\n" +
            "        DECLARE @ma_loi_am VARCHAR(20), @ten_loi_am NVARCHAR(100), @sl_am INT;\n" +
            "        SELECT TOP 1 @ma_loi_am = ma_thuoc, @ten_loi_am = ten_thuoc, @sl_am = CAST(so_luong_str AS INT)\n" +
            "        FROM @tbl\n" +
            "        WHERE CAST(so_luong_str AS INT) <= 0;\n" +
            "\n" +
            "        IF @ma_loi_am IS NOT NULL\n" +
            "        BEGIN\n" +
            "            DECLARE @sl_am_str NVARCHAR(20) = CAST(@sl_am AS NVARCHAR(20));\n" +
            "            RAISERROR(N'Lỗi nghiệp vụ nhập kho: Thuốc [%s - %s] có số lượng nhập là số âm hoặc bằng 0 (\"%s\")! Giao dịch đã bị ROLLBACK hoàn toàn để bảo vệ dữ liệu.', 16, 1, @ma_loi_am, @ten_loi_am, @sl_am_str);\n" +
            "        END;\n" +
            "\n" +
            "        -- 4. Tat ca hop le -> Cap nhat ton kho\n" +
            "        UPDATE t\n" +
            "        SET t.so_luong_ton = t.so_luong_ton + CAST(b.so_luong_str AS INT)\n" +
            "        FROM thuoc t\n" +
            "        JOIN @tbl b ON t.ma_thuoc = b.ma_thuoc;\n" +
            "\n" +
            "        COMMIT TRANSACTION;\n" +
            "    END TRY\n" +
            "    BEGIN CATCH\n" +
            "        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;\n" +
            "        THROW;\n" +
            "    END CATCH;\n" +
            "END;";

        String sqlGrantDuocSi = "GRANT EXECUTE ON dbo.sp_gd_nhap_nhieu_thuoc TO vai_tro_duoc_si;";
        String sqlGrantQuanTri = "GRANT EXECUTE ON dbo.sp_gd_nhap_nhieu_thuoc TO vai_tro_quan_tri;";

        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI");
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Dang tao Procedure sp_gd_nhap_nhieu_thuoc...");
            stmt.execute(sqlCreateProcedure);
            System.out.println(">>> TAO PROCEDURE THANH CONG! <<<");

            System.out.println("Dang phan quyen EXECUTE...");
            stmt.execute(sqlGrantDuocSi);
            stmt.execute(sqlGrantQuanTri);
            System.out.println(">>> PHAN QUYEN THANH CONG! <<<");

        } catch (Exception ex) {
            System.err.println("Loi khi thuc thi script tren RDS: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
