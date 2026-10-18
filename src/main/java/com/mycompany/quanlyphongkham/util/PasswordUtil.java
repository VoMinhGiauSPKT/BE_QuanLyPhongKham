package com.mycompany.quanlyphongkham.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String raw = args.length > 0 ? args[0] : "123456";
        String hashed = hashPassword(raw);
        System.out.println("Raw: " + raw);
        System.out.println("Hashed: " + hashed);
        System.out.println("Verify: " + checkPassword(raw, hashed));
    }
}
