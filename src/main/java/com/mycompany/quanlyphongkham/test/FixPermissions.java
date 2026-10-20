package com.mycompany.quanlyphongkham.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixPermissions {

    private static final String[] SQL_COMMANDS = {
        // fn_tinh_tuoi
        "GRANT EXECUTE ON dbo.fn_tinh_tuoi TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.fn_tinh_tuoi TO vai_tro_bac_si;",
        "GRANT EXECUTE ON dbo.fn_tinh_tuoi TO vai_tro_duoc_si;",
        "GRANT EXECUTE ON dbo.fn_tinh_tuoi TO vai_tro_quan_tri;",
        "GRANT EXECUTE ON dbo.fn_tinh_tuoi TO public;",

        // fn_tinh_tong_tien_thuoc & fn_tinh_tong_tien_dich_vu
        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_thuoc TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_thuoc TO vai_tro_bac_si;",
        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_thuoc TO vai_tro_duoc_si;",
        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_thuoc TO public;",

        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_dich_vu TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_dich_vu TO vai_tro_bac_si;",
        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_dich_vu TO vai_tro_quan_tri;",
        "GRANT EXECUTE ON dbo.fn_tinh_tong_tien_dich_vu TO public;",

        // table-valued functions
        "GRANT SELECT ON dbo.fn_lich_su_kham_benh TO vai_tro_le_tan;",
        "GRANT SELECT ON dbo.fn_lich_su_kham_benh TO vai_tro_bac_si;",
        "GRANT SELECT ON dbo.fn_lich_su_kham_benh TO vai_tro_quan_tri;",
        "GRANT SELECT ON dbo.fn_lich_su_kham_benh TO public;",

        "GRANT SELECT ON dbo.fn_tim_thuoc_kha_dung TO vai_tro_le_tan;",
        "GRANT SELECT ON dbo.fn_tim_thuoc_kha_dung TO vai_tro_bac_si;",
        "GRANT SELECT ON dbo.fn_tim_thuoc_kha_dung TO vai_tro_duoc_si;",
        "GRANT SELECT ON dbo.fn_tim_thuoc_kha_dung TO vai_tro_quan_tri;",
        "GRANT SELECT ON dbo.fn_tim_thuoc_kha_dung TO public;",

        // Stored Procedures
        "GRANT EXECUTE ON dbo.sp_gd_huy_don_thuoc TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.sp_gd_huy_don_thuoc TO vai_tro_quan_tri;",
        "GRANT EXECUTE ON dbo.sp_gd_huy_don_thuoc TO public;",

        "GRANT EXECUTE ON dbo.sp_gd_thanh_toan_hoa_don TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.sp_gd_thanh_toan_hoa_don TO vai_tro_quan_tri;",
        "GRANT EXECUTE ON dbo.sp_gd_thanh_toan_hoa_don TO public;",

        "GRANT EXECUTE ON dbo.sp_gd_huy_lich_hen TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.sp_gd_huy_lich_hen TO vai_tro_quan_tri;",
        "GRANT EXECUTE ON dbo.sp_gd_huy_lich_hen TO public;",

        "GRANT EXECUTE ON dbo.sp_tiep_nhan_kham TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.sp_tiep_nhan_kham TO public;",

        "GRANT EXECUTE ON dbo.sp_tim_kiem_benh_nhan TO vai_tro_le_tan;",
        "GRANT EXECUTE ON dbo.sp_tim_kiem_benh_nhan TO public;",

        "GRANT EXECUTE ON dbo.sp_gd_nhap_nhieu_thuoc TO vai_tro_duoc_si;",
        "GRANT EXECUTE ON dbo.sp_gd_nhap_nhieu_thuoc TO vai_tro_quan_tri;",
        "GRANT EXECUTE ON dbo.sp_gd_nhap_nhieu_thuoc TO public;"
    };

    public static void main(String[] args) {
        System.out.println("=== BAT DAU CAP QUYEN CHO DATABASE ===");

        // 1. AWS RDS
        String rdsUrl = "jdbc:sqlserver://phongkham.cdo8y6i4cjsw.eu-north-1.rds.amazonaws.com:1433;databaseName=QuanLyPhongKhamDB;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true;";
        applyPermissions(rdsUrl, "tk_quan_tri", "QuanTriMatKhau123!", "AWS RDS");

        // 2. Localhost
        String localUrl = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamDB;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true;";
        applyPermissions(localUrl, "tk_quan_tri", "QuanTriMatKhau123!", "Localhost");
    }

    private static void applyPermissions(String url, String user, String pass, String serverName) {
        System.out.println("\n>>> DANG CAP QUYEN TREN: " + serverName);
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            for (String sql : SQL_COMMANDS) {
                try {
                    stmt.execute(sql);
                    System.out.println("  [OK] " + sql);
                } catch (Exception ex) {
                    System.out.println("  [FAIL] " + sql + " -> " + ex.getMessage());
                }
            }
            System.out.println("HOAN TAT CAP QUYEN TREN " + serverName + "!");

        } catch (Exception e) {
            System.err.println("Khong the ket noi den " + serverName + ": " + e.getMessage());
        }
    }
}
