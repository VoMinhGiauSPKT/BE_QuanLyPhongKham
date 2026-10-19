package com.mycompany.quanlyphongkham.test;

import com.mycompany.quanlyphongkham.util.DBContext;
import java.sql.Connection;
import java.sql.Statement;

public class UpgradeDoctorPermissions {
    public static void main(String[] args) {
        System.out.println("=== CAP NHAT QUYEN SELECT CHO BANG BAC_SI VA NGUOI_DUNG ===");
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI");
             Statement stmt = conn.createStatement()) {

            // 1. Cấp quyền SELECT trên bac_si và nguoi_dung cho tài khoản xác thực
            stmt.execute("GRANT SELECT ON bac_si TO nguoi_dung_xac_thuc;");
            stmt.execute("GRANT SELECT ON nguoi_dung TO nguoi_dung_xac_thuc;");
            System.out.println("Da cap quyen SELECT bac_si & nguoi_dung cho nguoi_dung_xac_thuc!");

            // 2. Cấp quyền SELECT trên bac_si và nguoi_dung cho vai_tro_le_tan
            stmt.execute("GRANT SELECT ON bac_si TO vai_tro_le_tan;");
            stmt.execute("GRANT SELECT ON nguoi_dung TO vai_tro_le_tan;");
            System.out.println("Da cap quyen SELECT bac_si & nguoi_dung cho vai_tro_le_tan!");

            // 3. Cấp quyền cho vai_tro_bac_si và vai_tro_duoc_si
            stmt.execute("GRANT SELECT ON bac_si TO vai_tro_bac_si;");
            stmt.execute("GRANT SELECT ON nguoi_dung TO vai_tro_bac_si;");
            stmt.execute("GRANT SELECT ON bac_si TO vai_tro_duoc_si;");
            stmt.execute("GRANT SELECT ON nguoi_dung TO vai_tro_duoc_si;");
            System.out.println("Da cap quyen SELECT bac_si & nguoi_dung cho tat ca cac role con lai!");

            System.out.println("=== CAP NHAT QUYEN THANH CONG! ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
