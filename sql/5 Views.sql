USE QuanLyPhongKhamDB;
GO

-- View 1: Danh sách bệnh nhân chờ khám trong ngày (Cho màn hình lễ tân và bác sĩ)
CREATE OR ALTER VIEW v_hang_cho_kham_hom_nay AS
SELECT 
    lh.ma_lich_hen,
    lh.so_thu_tu,
    bn.ho_ten AS ten_benh_nhan,
    bn.so_dien_thoai,
    bs_user.ho_ten AS ten_bac_si,
    bs.phong_kham,
    lh.thoi_gian_hen,
    lh.trang_thai
FROM lich_hen lh
JOIN benh_nhan bn ON lh.ma_benh_nhan = bn.ma_benh_nhan
JOIN bac_si bs ON lh.ma_bac_si = bs.ma_bac_si
JOIN nguoi_dung bs_user ON bs.ma_bac_si = bs_user.ma_nguoi_dung
WHERE CAST(lh.thoi_gian_hen AS DATE) = CAST(GETDATE() AS DATE);
GO

-- View 2: Tổng hợp chi tiết khám bệnh, thuốc và dịch vụ cận lâm sàng
CREATE OR ALTER VIEW v_chi_tiet_kham_benh AS
SELECT 
    hs.ma_ho_so,
    bn.ho_ten AS ten_benh_nhan,
    bs_user.ho_ten AS ten_bac_si,
    hs.chan_doan,
    hs.ngay_kham,
    ISNULL(t.ten_thuoc, N'Không kê thuốc') AS ten_thuoc,
    dt.so_luong AS so_luong_thuoc,
    ISNULL(dv.ten_dich_vu, N'Không chỉ định') AS ten_dich_vu
FROM ho_so_kham hs
JOIN benh_nhan bn ON hs.ma_benh_nhan = bn.ma_benh_nhan
JOIN nguoi_dung bs_user ON hs.ma_bac_si = bs_user.ma_nguoi_dung
LEFT JOIN don_thuoc dt ON hs.ma_ho_so = dt.ma_ho_so
LEFT JOIN thuoc t ON dt.ma_thuoc = t.ma_thuoc
LEFT JOIN chi_dinh_dich_vu cd ON hs.ma_ho_so = cd.ma_ho_so
LEFT JOIN dich_vu dv ON cd.ma_dich_vu = dv.ma_dich_vu;
GO

-- View 3: Cảnh báo dược phẩm sắp hết hàng (< 20 đơn vị) hoặc cận hạn sử dụng (< 30 ngày)
CREATE OR ALTER VIEW v_canh_bao_ton_kho_thuoc AS
SELECT 
    ma_thuoc,
    ten_thuoc,
    so_luong_ton,
    don_vi_tinh,
    han_su_dung,
    DATEDIFF(day, GETDATE(), han_su_dung) AS so_ngay_con_han,
    CASE 
        WHEN so_luong_ton < 20 AND han_su_dung < DATEADD(day, 30, GETDATE()) THEN N'Tồn ít & Sắp hết hạn'
        WHEN so_luong_ton < 20 THEN N'Sắp hết hàng'
        WHEN han_su_dung < DATEADD(day, 30, GETDATE()) THEN N'Sắp hết hạn sử dụng'
        ELSE N'Bình thường'
    END AS trang_thai_canh_bao
FROM thuoc
WHERE trang_thai = 1 AND (so_luong_ton < 20 OR han_su_dung < DATEADD(day, 30, GETDATE()));
GO

-- View 4: Báo cáo số ca khám và tổng doanh thu mang lại theo từng bác sĩ
CREATE OR ALTER VIEW v_doanh_thu_theo_bac_si AS
SELECT 
    bs.ma_bac_si,
    nd.ho_ten AS ten_bac_si,
    bs.chuyen_khoa,
    bs.phong_kham,
    (SELECT COUNT(DISTINCT hs1.ma_ho_so) 
     FROM ho_so_kham hs1 
     WHERE hs1.ma_bac_si = bs.ma_bac_si) AS so_ca_kham,
    (SELECT ISNULL(SUM(hd1.tong_tien), 0) 
     FROM hoa_don hd1 
     JOIN ho_so_kham hs2 ON hd1.ma_ho_so = hs2.ma_ho_so 
     WHERE hs2.ma_bac_si = bs.ma_bac_si) AS tong_doanh_thu
FROM bac_si bs
JOIN nguoi_dung nd ON bs.ma_bac_si = nd.ma_nguoi_dung
WHERE nd.hoat_dong = 1;
GO

-- View 5: Tổng hợp doanh thu hóa đơn phòng khám theo từng ngày
CREATE OR ALTER VIEW v_doanh_thu_theo_ngay AS
SELECT 
    CAST(ngay_lap AS DATE) AS ngay_thanh_toan,
    COUNT(ma_hoa_don) AS so_luong_hoa_don,
    SUM(tien_dich_vu) AS tong_tien_dich_vu,
    SUM(tien_thuoc) AS tong_tien_thuoc,
    SUM(tien_giam_gia) AS tong_tien_giam_gia,
    SUM(tong_tien) AS thuc_thu
FROM hoa_don
WHERE trang_thai = N'Đã thanh toán'
GROUP BY CAST(ngay_lap AS DATE);
GO