package com.mycompany.quanlyphongkham.test;

import com.mycompany.quanlyphongkham.util.DBContext;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class BackupDatabase {
    public static void main(String[] args) {
        System.out.println("=== BAT DAU SAO LUU CO SO DU LIEU QUANLYPHONGKHAMDB ===");
        
        String targetBackupPath = "d:\\ghichumonhoc\\hequantricsdl\\doancuoiki\\backup.bak";
        
        try (Connection conn = DBContext.getConnectionByRole("QUAN_TRI");
             Statement stmt = conn.createStatement()) {
            
            // Thử 1: Backup trực tiếp vào đường dẫn đích
            try {
                System.out.println("Dang thu sao luu truc tiep vao: " + targetBackupPath);
                String sqlDirect = "BACKUP DATABASE QuanLyPhongKhamDB TO DISK = '" + targetBackupPath + "' WITH FORMAT, INIT, NAME = 'QuanLyPhongKhamDB-Full Backup';";
                stmt.execute(sqlDirect);
                System.out.println(">>> SAO LUU THANH CONG TRUC TIEP VAO: " + targetBackupPath + " <<<");
                return;
            } catch (Exception ex) {
                System.out.println("Khong the ghi truc tiep (co the do phan quyen thu muc cua SQL Server Service): " + ex.getMessage());
            }

            // Thử 2: Lấy thư mục Default Backup của SQL Server rồi copy sang target
            try {
                System.out.println("Dang truy van thu muc Backup mac dinh cua SQL Server...");
                String defaultBackupDir = null;
                try (ResultSet rs = stmt.executeQuery("SELECT CAST(SERVERPROPERTY('InstanceDefaultBackupPath') AS VARCHAR(500)) AS DefaultBackupPath;")) {
                    if (rs.next()) {
                        defaultBackupDir = rs.getString("DefaultBackupPath");
                    }
                }
                
                if (defaultBackupDir == null || defaultBackupDir.isEmpty()) {
                    defaultBackupDir = "C:\\backup\\";
                    new File(defaultBackupDir).mkdirs();
                }
                
                File tempBak = new File(defaultBackupDir, "QuanLyPhongKhamDB_temp.bak");
                System.out.println("Ghi vao thu muc mac dinh: " + tempBak.getAbsolutePath());
                
                String sqlTemp = "BACKUP DATABASE QuanLyPhongKhamDB TO DISK = '" + tempBak.getAbsolutePath() + "' WITH FORMAT, INIT, NAME = 'QuanLyPhongKhamDB-Full Backup';";
                stmt.execute(sqlTemp);
                System.out.println("Da tao file backup tam tai: " + tempBak.getAbsolutePath());
                
                // Copy file tempBak sang targetBackupPath
                File destFile = new File(targetBackupPath);
                Files.copy(tempBak.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println(">>> DA COPY VA HOAN TAT FILE BACKUP TAI: " + destFile.getAbsolutePath() + " (Kich thuoc: " + destFile.length() + " bytes) <<<");
                
            } catch (Exception ex2) {
                System.err.println("Loi khi sao luu qua thu muc mac dinh: " + ex2.getMessage());
                ex2.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("Loi ket noi CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
