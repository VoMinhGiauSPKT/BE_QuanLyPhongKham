package com.mycompany.quanlyphongkham.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBContext] Microsoft JDBC Driver not found: " + e.getMessage());
        }
    }

    private static String buildJdbcUrl() {
        String host = EnvConfig.get("DB_HOST", "localhost");
        String port = EnvConfig.get("DB_PORT", "1433");
        String instance = EnvConfig.get("DB_INSTANCE", "");
        String dbName = EnvConfig.get("DB_NAME", "QuanLyPhongKhamDB");
        String encrypt = EnvConfig.get("DB_ENCRYPT", "false");
        String trustCert = EnvConfig.get("DB_TRUST_SERVER_CERT", "true");

        if (instance != null && !instance.trim().isEmpty()) {
            return String.format(
                    "jdbc:sqlserver://%s;instanceName=%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;sendStringParametersAsUnicode=true;",
                    host, instance.trim(), dbName, encrypt, trustCert
            );
        } else {
            return String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;sendStringParametersAsUnicode=true;",
                    host, port, dbName, encrypt, trustCert
            );
        }
    }

    /**
     * Connection for Authentication Service (Login) using tk_xac_thuc
     */
    public static Connection getAuthConnection() throws SQLException {
        String url = buildJdbcUrl();
        String user = EnvConfig.get("DB_USER_AUTH", "tk_xac_thuc");
        String pass = EnvConfig.get("DB_PASS_AUTH", "AuthServicePass123!");
        Connection conn = DriverManager.getConnection(url, user, pass);
        System.out.println("[DBContext] >>> Kết nối với database thành công! [Tài khoản: " + user + "] <<<");
        return conn;
    }

    /**
     * Dynamic Connection based on user role extracted from JWT
     * Enforces DBMS-level security via SQL Server Roles
     */
    public static Connection getConnectionByRole(String role) throws SQLException {
        String url = buildJdbcUrl();
        String user;
        String pass;

        if (role == null) {
            return getAuthConnection();
        }

        String normalizedRole = role.trim().toUpperCase();

        switch (normalizedRole) {
            case "QUAN_TRI":
            case "VAI_TRO_QUAN_TRI":
                user = EnvConfig.get("DB_USER_ADMIN", "tk_quan_tri");
                pass = EnvConfig.get("DB_PASS_ADMIN", "QuanTriMatKhau123!");
                break;
            case "BAC_SI":
            case "VAI_TRO_BAC_SI":
                user = EnvConfig.get("DB_USER_BACSI", "tk_bac_si1");
                pass = EnvConfig.get("DB_PASS_BACSI", "BacSiMatKhau123!");
                break;
            case "LE_TAN":
            case "VAI_TRO_LE_TAN":
                user = EnvConfig.get("DB_USER_LETAN", "tk_le_tan1");
                pass = EnvConfig.get("DB_PASS_LETAN", "LeTanMatKhau123!");
                break;
            case "DUOC_SI":
            case "VAI_TRO_DUOC_SI":
                user = EnvConfig.get("DB_USER_DUOCSI", "tk_duoc_si1");
                pass = EnvConfig.get("DB_PASS_DUOCSI", "DuocSiMatKhau123!");
                break;
            default:
                user = EnvConfig.get("DB_USER_AUTH", "tk_xac_thuc");
                pass = EnvConfig.get("DB_PASS_AUTH", "AuthServicePass123!");
                break;
        }

        Connection conn = DriverManager.getConnection(url, user, pass);
        System.out.println("[DBContext] >>> Kết nối với database thành công! [Role: " + normalizedRole + " | User: " + user + "] <<<");
        return conn;
    }
}
