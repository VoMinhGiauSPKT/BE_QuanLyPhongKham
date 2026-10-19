package com.mycompany.quanlyphongkham.test;

import com.mycompany.quanlyphongkham.util.DBContext;
import java.sql.Connection;
import java.sql.Statement;

public class UpgradeSchemaFollowUp {
    public static void main(String[] args) {
        System.out.println("=== BAT DAU NANG CAP CSDL CHO TINH NANG HEN TAI KHAM ===");
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI");
             Statement stmt = conn.createStatement()) {

            // 1. Bổ sung loai_lich_hen vào lich_hen nếu chưa có
            String sql1 = "IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('lich_hen') AND name = 'loai_lich_hen') " +
                          "BEGIN " +
                          "    ALTER TABLE lich_hen ADD loai_lich_hen NVARCHAR(30) NOT NULL CONSTRAINT DF_lich_hen_loai DEFAULT N'Khám mới'; " +
                          "    PRINT 'Da them cot loai_lich_hen vao lich_hen'; " +
                          "END";
            stmt.execute(sql1);

            // 2. Bổ sung ma_lich_hen_truoc vào lich_hen nếu chưa có
            String sql2 = "IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('lich_hen') AND name = 'ma_lich_hen_truoc') " +
                          "BEGIN " +
                          "    ALTER TABLE lich_hen ADD ma_lich_hen_truoc VARCHAR(20) NULL CONSTRAINT FK_lich_hen_truoc REFERENCES lich_hen(ma_lich_hen); " +
                          "    PRINT 'Da them cot ma_lich_hen_truoc vao lich_hen'; " +
                          "END";
            stmt.execute(sql2);

            // 3. Bổ sung ngay_tai_kham vào ho_so_kham nếu chưa có
            String sql3 = "IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('ho_so_kham') AND name = 'ngay_tai_kham') " +
                          "BEGIN " +
                          "    ALTER TABLE ho_so_kham ADD ngay_tai_kham DATE NULL; " +
                          "    PRINT 'Da them cot ngay_tai_kham vao ho_so_kham'; " +
                          "END";
            stmt.execute(sql3);

            // 4. Cấp quyền cho vai_tro_bac_si thao tác thêm lịch hẹn tái khám
            String sql4 = "GRANT INSERT, UPDATE ON lich_hen TO vai_tro_bac_si;";
            stmt.execute(sql4);
            System.out.println("Da cap quyen INSERT, UPDATE tren lich_hen cho vai_tro_bac_si.");

            System.out.println("=== NANG CAP CSDL THANH CONG! ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
