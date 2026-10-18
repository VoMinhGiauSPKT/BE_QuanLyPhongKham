package com.mycompany.quanlyphongkham.module.bacsi;

import com.mycompany.quanlyphongkham.common.ApiResponse;
import com.mycompany.quanlyphongkham.common.BaseServlet;
import com.mycompany.quanlyphongkham.entity.DonThuoc;
import com.mycompany.quanlyphongkham.util.FormatUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "HoanTatKhamServlet", urlPatterns = {"/api/bac-si/hoan-tat-kham"})
public class HoanTatKhamServlet extends BaseServlet {

    private final BacSiDAO bacSiDAO = new BacSiDAO();

    public static class ThuocItemRequest {
        public String maThuoc; // T001 hoặc 1
        public Integer soLuong;
        public String cachDung;
        public String huongDan;
    }

    public static class HoanTatKhamRequest {
        public String maLichHen;  // LH001 hoặc 1
        public String maBenhNhan; // BN001 hoặc 1
        public String maBacSi;    // BS001 hoặc 1, optional
        public String trieuChung;
        public String chanDoan;
        public String loiDan;
        public List<ThuocItemRequest> danhSachThuoc;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = getCurrentUserRole(req);
        String tokenUserId = getCurrentUserId(req);

        HoanTatKhamRequest body = readJsonBody(req, HoanTatKhamRequest.class);

        if (body == null || body.maLichHen == null || body.maBenhNhan == null || body.chanDoan == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Vui lòng cung cấp mã lịch hẹn, mã bệnh nhân và chẩn đoán!", "MISSING_REQUIRED_FIELDS"));
            return;
        }

        String lichHenId = FormatUtil.normalizeLichHen(body.maLichHen);
        String benhNhanId = FormatUtil.normalizeBenhNhan(body.maBenhNhan);
        String bacSiId = (body.maBacSi != null && !body.maBacSi.trim().isEmpty()) 
                ? FormatUtil.normalizeBacSi(body.maBacSi) 
                : tokenUserId;

        if (lichHenId == null || benhNhanId == null || bacSiId == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("Mã định danh không hợp lệ (LH001, BN001, BS001)!", "INVALID_ID_FORMAT"));
            return;
        }

        List<DonThuoc> listThuoc = new ArrayList<>();
        if (body.danhSachThuoc != null) {
            for (ThuocItemRequest item : body.danhSachThuoc) {
                if (item.maThuoc != null) {
                    String thuocId = FormatUtil.normalizeThuoc(item.maThuoc);
                    if (thuocId != null) {
                        DonThuoc dt = new DonThuoc();
                        dt.setMaThuoc(thuocId);
                        dt.setSoLuong(item.soLuong != null ? item.soLuong : 1);
                        dt.setCachDung(item.cachDung != null ? item.cachDung : "Theo chỉ dẫn");
                        dt.setHuongDan(item.huongDan != null ? item.huongDan : "");
                        listThuoc.add(dt);
                    }
                }
            }
        }

        try {
            String maHoSo = bacSiDAO.hoanTatKham(
                    role,
                    lichHenId,
                    benhNhanId,
                    bacSiId,
                    body.trieuChung,
                    body.chanDoan,
                    body.loiDan,
                    listThuoc
            );

            Map<String, Object> data = new HashMap<>();
            data.put("maHoSo", maHoSo);
            data.put("maHoSoCode", maHoSo);
            data.put("maLichHenCode", lichHenId);
            data.put("soLuongThuocKe", listThuoc.size());

            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok(data, "Hoàn tất khám bệnh, lưu hồ sơ và kê đơn thuốc thành công!"));

        } catch (SQLException e) {
            handleSqlException(resp, e, "Lỗi khi hoàn tất khám bệnh: " + e.getMessage());
        }
    }
}
