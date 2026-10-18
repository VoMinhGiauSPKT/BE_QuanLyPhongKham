package com.mycompany.quanlyphongkham.util;

/**
 * Utility class for normalizing and formatting entity codes (e.g., BS001, T001, BN001, ND001).
 */
public class FormatUtil {

    public static final String PREFIX_BAC_SI = "BS";
    public static final String PREFIX_NGUOI_DUNG = "ND";
    public static final String PREFIX_THUOC = "T";
    public static final String PREFIX_BENH_NHAN = "BN";
    public static final String PREFIX_DICH_VU = "DV";
    public static final String PREFIX_HOA_DON = "HD";
    public static final String PREFIX_LICH_HEN = "LH";
    public static final String PREFIX_HO_SO = "HS";
    public static final String PREFIX_CHI_DINH = "CD";
    public static final String PREFIX_DON_THUOC = "DT";

    /**
     * Normalizes an ID into standard prefixed code:
     * - "BS001" -> "BS001"
     * - "1" with prefix "BS" -> "BS001"
     * - null / empty -> null
     */
    public static String normalizeCode(String prefix, String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String clean = input.trim();
        if (clean.toUpperCase().startsWith(prefix.toUpperCase())) {
            return clean.toUpperCase();
        }
        try {
            int num = Integer.parseInt(clean.replaceAll("^[A-Za-z]+", ""));
            return String.format("%s%03d", prefix.toUpperCase(), num);
        } catch (NumberFormatException e) {
            return clean;
        }
    }

    public static String normalizeBacSi(String input) {
        return normalizeCode(PREFIX_BAC_SI, input);
    }

    public static String normalizeNguoiDung(String input) {
        return normalizeCode(PREFIX_NGUOI_DUNG, input);
    }

    public static String normalizeThuoc(String input) {
        return normalizeCode(PREFIX_THUOC, input);
    }

    public static String normalizeBenhNhan(String input) {
        return normalizeCode(PREFIX_BENH_NHAN, input);
    }

    public static String normalizeDichVu(String input) {
        return normalizeCode(PREFIX_DICH_VU, input);
    }

    public static String normalizeHoaDon(String input) {
        return normalizeCode(PREFIX_HOA_DON, input);
    }

    public static String normalizeLichHen(String input) {
        return normalizeCode(PREFIX_LICH_HEN, input);
    }

    public static String normalizeHoSo(String input) {
        return normalizeCode(PREFIX_HO_SO, input);
    }

    public static String normalizeChiDinh(String input) {
        return normalizeCode(PREFIX_CHI_DINH, input);
    }

    public static String normalizeDonThuoc(String input) {
        return normalizeCode(PREFIX_DON_THUOC, input);
    }

    // Overloads for Integer if still called anywhere
    public static String formatCode(String prefix, Object id) {
        if (id == null) {
            return null;
        }
        return normalizeCode(prefix, id.toString());
    }

    public static String formatBacSi(Object id) {
        return formatCode(PREFIX_BAC_SI, id);
    }

    public static String formatThuoc(Object id) {
        return formatCode(PREFIX_THUOC, id);
    }

    public static String formatBenhNhan(Object id) {
        return formatCode(PREFIX_BENH_NHAN, id);
    }

    public static String formatDichVu(Object id) {
        return formatCode(PREFIX_DICH_VU, id);
    }

    public static String formatHoaDon(Object id) {
        return formatCode(PREFIX_HOA_DON, id);
    }

    public static String formatLichHen(Object id) {
        return formatCode(PREFIX_LICH_HEN, id);
    }

    public static String formatHoSo(Object id) {
        return formatCode(PREFIX_HO_SO, id);
    }

    public static Integer parseId(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String clean = input.trim().replaceAll("^[A-Za-z]+", "");
        try {
            return Integer.parseInt(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
