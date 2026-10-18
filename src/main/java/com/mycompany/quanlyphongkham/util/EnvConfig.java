package com.mycompany.quanlyphongkham.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;

public class EnvConfig {

    private static Dotenv dotenv;

    static {
        try {
            // Check current directory
            File envFile = new File(".env");
            if (envFile.exists()) {
                dotenv = Dotenv.configure().load();
            } else {
                // Check parent directories or project path
                dotenv = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
            }
        } catch (Exception e) {
            System.err.println("[EnvConfig] Dotenv could not load .env file directly, falling back to System environment: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        String value = null;
        if (dotenv != null) {
            value = dotenv.get(key);
        }
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(key);
        }
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(key);
        }
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val != null) {
            return Boolean.parseBoolean(val.trim());
        }
        return defaultValue;
    }
}
