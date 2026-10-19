package com.mycompany.quanlyphongkham.test;

import com.mycompany.quanlyphongkham.util.DBContext;
import java.sql.Connection;
import java.sql.Statement;

public class UpgradePharmacyPermissions {
    public static void main(String[] args) {
        System.out.println("=== CAP NHAT BANG VA QUYEN CHO VAI TRO DUOC SI ===");
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI");
             Statement stmt = conn.createStatement()) {

            // 1. Tạo bảng nhật ký giá thuốc nếu chưa có
            String sqlTable = "IF OBJECT_ID('nhat_ky_gia_thuoc') IS NULL " +
                              "BEGIN " +
                              "    CREATE TABLE nhat_ky_gia_thuoc ( " +
                              "        ma_nhat_ky INT IDENTITY(1,1) PRIMARY KEY, " +
                              "        ma_thuoc VARCHAR(20) NOT NULL, " +
                              "        gia_cu DECIMAL(18,2) NOT NULL, " +
                              "        gia_moi DECIMAL(18,2) NOT NULL, " +
                              "        thoi_gian DATETIME NOT NULL DEFAULT GETDATE(), " +
                              "        nguoi_thuc_hien VARCHAR(50) NOT NULL DEFAULT SYSTEM_USER " +
                              "    ); " +
                              "    PRINT 'Da tao bang nhat_ky_gia_thuoc'; " +
                              "END;";
            stmt.execute(sqlTable);

            // 2. Đảm bảo Trigger ghi nhật ký giá hoạt động
            String sqlTrig = "CREATE OR ALTER TRIGGER trg_ghi_nhat_ky_gia_thuoc " +
                             "ON thuoc " +
                             "AFTER UPDATE " +
                             "AS " +
                             "BEGIN " +
                             "    SET NOCOUNT ON; " +
                             "    IF UPDATE(don_gia) " +
                             "    BEGIN " +
                             "        INSERT INTO nhat_ky_gia_thuoc (ma_thuoc, gia_cu, gia_moi) " +
                             "        SELECT d.ma_thuoc, d.don_gia, i.don_gia " +
                             "        FROM deleted d " +
                             "        JOIN inserted i ON d.ma_thuoc = i.ma_thuoc " +
                             "        WHERE d.don_gia <> i.don_gia; " +
                             "    END; " +
                             "END;";
            stmt.execute(sqlTrig);

            // 3. Cấp quyền chi tiết cho vai_tro_duoc_si
            stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON thuoc TO vai_tro_duoc_si;");
            stmt.execute("GRANT SELECT, INSERT ON nhat_ky_gia_thuoc TO vai_tro_duoc_si;");
            System.out.println("Da cap quyen SELECT, INSERT, UPDATE, DELETE tren thuoc va nhat_ky_gia_thuoc cho vai_tro_duoc_si!");

            System.out.println("=== CAP NHAT THANH CONG! ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
