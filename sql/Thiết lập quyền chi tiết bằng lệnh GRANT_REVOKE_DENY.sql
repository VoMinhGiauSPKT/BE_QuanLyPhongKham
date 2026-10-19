USE QuanLyPhongKhamDB;
GO

-- =========================================================================
-- VAI TRÒ 1: Quản trị viên (vai_tro_quan_tri)
-- Chính sách: Toàn quyền cấu hình, vận hành và quản lý trên Database
-- =========================================================================
GRANT CONTROL ON DATABASE::QuanLyPhongKhamDB TO vai_tro_quan_tri;

-- =========================================================================
-- VAI TRÒ 2: Bác sĩ (vai_tro_bac_si)
-- Chính sách: Đọc danh mục, ghi hồ sơ, kê đơn; cấm chỉnh sửa tài chính và tài khoản
-- =========================================================================
-- Quyền đọc thông tin hỗ trợ khám bệnh
GRANT SELECT ON benh_nhan TO vai_tro_bac_si;
GRANT SELECT ON dich_vu TO vai_tro_bac_si;
GRANT SELECT ON thuoc TO vai_tro_bac_si;
GRANT SELECT ON lich_hen TO vai_tro_bac_si;
GRANT SELECT ON bac_si TO vai_tro_bac_si;
GRANT SELECT ON nguoi_dung TO vai_tro_bac_si;

-- Quyền thao tác bệnh án, kê đơn, chỉ định dịch vụ và hẹn tái khám
GRANT INSERT, UPDATE, SELECT ON ho_so_kham TO vai_tro_bac_si;
GRANT INSERT, UPDATE, DELETE, SELECT ON don_thuoc TO vai_tro_bac_si;
GRANT INSERT, UPDATE, DELETE, SELECT ON chi_dinh_dich_vu TO vai_tro_bac_si;
GRANT INSERT, UPDATE ON lich_hen TO vai_tro_bac_si; -- Cho phép bác sĩ lập lịch hẹn tái khám khi hoàn tất ca khám

-- Quyền thực thi thủ tục chuyên môn và hàm tra cứu
GRANT EXECUTE ON sp_cap_nhat_benh_nhan TO vai_tro_bac_si;
GRANT EXECUTE ON sp_xoa_chi_dinh_dich_vu TO vai_tro_bac_si;
GRANT EXECUTE ON sp_gd_ke_nhieu_thuoc TO vai_tro_bac_si;
GRANT SELECT ON v_chi_tiet_kham_benh TO vai_tro_bac_si;
GRANT SELECT ON fn_tim_thuoc_kha_dung TO vai_tro_bac_si;
GRANT SELECT ON fn_lich_su_kham_benh TO vai_tro_bac_si;

-- Quyền sinh mã tự động (Sequences) khi bác sĩ thao tác bệnh án, chỉ định, đơn thuốc
GRANT UPDATE ON SEQUENCE::seq_ho_so TO vai_tro_bac_si;
GRANT UPDATE ON SEQUENCE::seq_chi_dinh TO vai_tro_bac_si;
GRANT UPDATE ON SEQUENCE::seq_don_thuoc TO vai_tro_bac_si;
GRANT UPDATE ON SEQUENCE::seq_lich_hen TO vai_tro_bac_si;

-- Cấm can thiệp tiền bạc và hệ thống người dùng
DENY INSERT, UPDATE, DELETE ON hoa_don TO vai_tro_bac_si;
DENY INSERT, UPDATE, DELETE ON nguoi_dung TO vai_tro_bac_si;

-- =========================================================================
-- VAI TRÒ 3: Lễ tân / Thu ngân (vai_tro_le_tan)
-- Chính sách: Đón tiếp, tạo lịch, lập hóa đơn; cấm xem thông tin bệnh án mật
-- =========================================================================
-- Quản lý tiếp nhận và lịch khám
GRANT SELECT, INSERT, UPDATE ON benh_nhan TO vai_tro_le_tan;
GRANT SELECT, INSERT, UPDATE ON lich_hen TO vai_tro_le_tan;
GRANT SELECT, INSERT, UPDATE ON hoa_don TO vai_tro_le_tan;
GRANT SELECT ON dich_vu TO vai_tro_le_tan;
GRANT SELECT ON bac_si TO vai_tro_le_tan;
GRANT SELECT ON nguoi_dung TO vai_tro_le_tan;
GRANT SELECT ON chi_dinh_dich_vu TO vai_tro_le_tan; -- Đọc chỉ định dịch vụ để lập hóa đơn viện phí
GRANT SELECT ON don_thuoc TO vai_tro_le_tan;        -- Đọc đơn thuốc để đối soát hóa đơn viện phí
GRANT SELECT ON nhat_ky_gia_thuoc TO vai_tro_le_tan;

-- Xem hàng đợi và báo cáo ngày
GRANT SELECT ON v_hang_cho_kham_hom_nay TO vai_tro_le_tan;
GRANT SELECT ON v_doanh_thu_theo_ngay TO vai_tro_le_tan;

-- Thực thi nghiệp vụ lễ tân và thanh toán
GRANT EXECUTE ON sp_tiep_nhan_kham TO vai_tro_le_tan;
GRANT EXECUTE ON sp_tim_kiem_benh_nhan TO vai_tro_le_tan;
GRANT EXECUTE ON sp_gd_thanh_toan_hoa_don TO vai_tro_le_tan;
GRANT EXECUTE ON sp_gd_huy_lich_hen TO vai_tro_le_tan;

-- Quyền sinh mã tự động (Sequences) khi tiếp nhận bệnh nhân, xếp lịch, lập hóa đơn
GRANT UPDATE ON SEQUENCE::seq_benh_nhan TO vai_tro_le_tan;
GRANT UPDATE ON SEQUENCE::seq_lich_hen TO vai_tro_le_tan;
GRANT UPDATE ON SEQUENCE::seq_hoa_don TO vai_tro_le_tan;

-- Bảo mật y tế: Cấm xem chẩn đoán bệnh án và cấm can thiệp đơn thuốc
DENY SELECT ON ho_so_kham(chan_doan, loi_dan) TO vai_tro_le_tan;
DENY INSERT, UPDATE, DELETE ON don_thuoc TO vai_tro_le_tan;

-- =========================================================================
-- VAI TRÒ 4: Dược sĩ (vai_tro_duoc_si)
-- Chính sách: Toàn quyền quản trị danh mục thuốc (CRUD) và xuất/nhập kho; không can thiệp khám chữa
-- =========================================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON thuoc TO vai_tro_duoc_si; -- Toàn quyền CRUD danh mục thuốc
GRANT SELECT, INSERT ON nhat_ky_gia_thuoc TO vai_tro_duoc_si;      -- Theo dõi và lưu vết biến động giá thuốc
GRANT SELECT ON don_thuoc TO vai_tro_duoc_si;
GRANT SELECT ON benh_nhan TO vai_tro_duoc_si;                     -- Đọc thông tin bệnh nhân để đối chiếu phát thuốc
GRANT SELECT ON bac_si TO vai_tro_duoc_si;                        -- Đọc thông tin bác sĩ kê đơn
GRANT SELECT ON nguoi_dung TO vai_tro_duoc_si;
GRANT SELECT ON v_canh_bao_ton_kho_thuoc TO vai_tro_duoc_si;

-- Quyền sinh mã tự động khi dược sĩ thêm mới thuốc
GRANT UPDATE ON SEQUENCE::seq_thuoc TO vai_tro_duoc_si;

-- Cấm tuyệt đối can thiệp hồ sơ y khoa, lịch hẹn và hóa đơn
DENY INSERT, UPDATE, DELETE ON lich_hen TO vai_tro_duoc_si;
DENY INSERT, UPDATE, DELETE ON ho_so_kham TO vai_tro_duoc_si;
DENY INSERT, UPDATE, DELETE ON hoa_don TO vai_tro_duoc_si;
GO