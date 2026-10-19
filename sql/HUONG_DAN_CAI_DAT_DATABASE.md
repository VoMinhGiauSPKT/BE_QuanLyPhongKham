# HƯỚNG DẪN KHỞI TẠO CƠ SỞ DỮ LIỆU PHÒNG KHÁM (QUANLYPHONGKHAMDB)

Tài liệu này hướng dẫn chi tiết cách cài đặt và chạy toàn bộ Cơ sở dữ liệu SQL Server cho hệ thống Phòng khám Medicare Clinic, đảm bảo tương thích 100% với cả Backend Java EE (Tomcat) và Frontend React (Vite).

---

## 🚀 CÁCH 1: KHỞI TẠO NHANH CHỈ VỚI 1 CLICK (KHUYÊN DÙNG)

Trong thư mục `sql/` đã có file tổng hợp:
👉 **`00_CHAY_TAT_CA_SETUP_ALL.sql`**

1. Mở **SQL Server Management Studio (SSMS)** và kết nối tới máy chủ SQL Server của bạn.
2. Nhấn **Ctrl + O** để mở file: `d:\ghichumonhoc\hequantricsdl\doancuoiki\sql\00_CHAY_TAT_CA_SETUP_ALL.sql`.
3. Nhấn **F5** (hoặc nút **Execute**) trên thanh công cụ.
4. Đợi thông báo: 
   ```text
   >>> CHÚC MỪNG: BẠN ĐÃ KHỞI TẠO TOÀN BỘ CƠ SỞ DỮ LIỆU QUANLYPHONGKHAMDB THÀNH CÔNG 100%! <<<
   ```
Toàn bộ Database, 10 Sequences, 11 Bảng dữ liệu, 5 Ràng buộc, 5 Functions, 5 Views, 5 Triggers, 10 Stored Procedures, 5 Indexes và Phân quyền bảo mật RBAC (GRANT/REVOKE/DENY) sẽ được thiết lập hoàn tất.

---

## 🛠️ CÁCH 2: CHẠY LẦN LƯỢT TỪNG FILE THEO THỨ TỰ CHUẨN

Nếu bạn muốn chạy từng file riêng biệt để phục vụ báo cáo hoặc chấm điểm từng phần, vui lòng chạy theo đúng thứ tự sau:

1. `quanliphongkham.sql`: Tạo CSDL, 10 Sequences và 11 Bảng dữ liệu.
2. `seed_data.sql`: Nạp dữ liệu mẫu ban đầu (admin, bác sĩ, lễ tân, dược sĩ, bệnh nhân, thuốc, dịch vụ, lịch hẹn).
3. `5 Ràng buộc.sql`: 5 Check Constraints (giới tính, tồn kho >= 0, đơn giá > 0, logic tổng tiền hóa đơn, STT khám duy nhất).
4. `5 User-Defined Functions.sql`: 5 Hàm (tính tuổi, tính tiền thuốc, tính tiền dịch vụ, xem lịch sử khám, tìm thuốc khả dụng).
5. `5 Views.sql`: 5 Khung nhìn (hàng chờ khám, chi tiết khám bệnh, cảnh báo kho, doanh thu theo bác sĩ, doanh thu theo ngày).
6. `5 Triggers.sql`: 5 Trigger tự động (trừ kho khi kê đơn, chặn thuốc hết hạn, hoàn kho khi hủy đơn, đổi trạng thái ca khám, audit log giá thuốc).
7. `5 Stored Procedures.sql`: 5 Thủ tục nghiệp vụ (tiếp nhận khám, cập nhật bệnh nhân, xóa chỉ định, tìm kiếm phân trang, thống kê khoảng ngày).
8. `5 nghiệp vụ minh họa sử dụng TRANSACTION.sql`: 5 Giao dịch ACID (thanh toán viện phí, kê nhiều thuốc, hủy lịch hẹn, hủy đơn thuốc, xóa hồ sơ sai sót).
9. `5 Index.sql`: 5 Chỉ mục tối ưu hóa hiệu năng truy vấn.
10. `Bảo mật và phân quyền.sql`: Tạo 4 Roles (`vai_tro_quan_tri`, `vai_tro_bac_si`, `vai_tro_le_tan`, `vai_tro_duoc_si`) và các Login tương ứng.
11. `Thiết lập tài khoản xác thực trong SQL Server.sql`: Tạo tài khoản đăng nhập xác thực JWT Auth `tk_xac_thuc`.
12. `Thiết lập quyền chi tiết bằng lệnh GRANT_REVOKE_DENY.sql`: Áp dụng chính sách bảo mật Principle of Least Privilege cho từng vai trò.

---

## 🔐 THÔNG TIN TÀI KHOẢN MẶC ĐỊNH

### 1. Tài khoản SQL Server kết nối từ Backend JDBC (.env)
| Vai trò | Login SQL Server | Mật khẩu | Quyền hạn chính |
| :--- | :--- | :--- | :--- |
| **Xác thực JWT** | `tk_xac_thuc` | `AuthServicePass123!` | Đọc bảng `nguoi_dung`, `bac_si` để kiểm tra đăng nhập |
| **Quản trị viên** | `tk_quan_tri` | `QuanTriMatKhau123!` | Toàn quyền kiểm soát (GRANT CONTROL) |
| **Bác sĩ** | `tk_bac_si1` | `BacSiMatKhau123!` | Khám bệnh, kê đơn, chỉ định dịch vụ; cấm sờ vào hóa đơn & tài khoản |
| **Lễ tân / Thu ngân** | `tk_le_tan1` | `LeTanMatKhau123!` | Tiếp nhận, lập lịch hẹn, thanh toán viện phí; cấm đọc chẩn đoán bệnh án mật |
| **Dược sĩ** | `tk_duoc_si1` | `DuocSiMatKhau123!` | Quản lý danh mục thuốc, xuất nhập tồn kho; cấm can thiệp khám và hóa đơn |

### 2. Tài khoản Đăng nhập trên Giao diện Web (FE)
| Vai trò | Tên đăng nhập (Username) | Mật khẩu |
| :--- | :--- | :--- |
| **Quản trị viên** | `admin` | `123456` |
| **Bác sĩ điều trị** | `bacsi1` | `123456` |
| **Lễ tân / Thu ngân** | `letan1` | `123456` |
| **Dược sĩ kho dược** | `duocsi1` | `123456` |

---

## ⚙️ CẤU HÌNH SQL SERVER BẮT BUỘC ĐỂ BACKEND KẾT NỐI ĐƯỢC
1. **Bật SQL Server Authentication (Mixed Mode):**
   - Chuột phải vào tên Server trong SSMS -> **Properties** -> **Security** -> Chọn **SQL Server and Windows Authentication mode**.
2. **Kích hoạt giao thức TCP/IP và cổng 1433:**
   - Mở **SQL Server Configuration Manager** -> **SQL Server Network Configuration** -> **Protocols for MSSQLSERVER (hoặc SQLEXPRESS)** -> Bật **TCP/IP** thành `Enabled` (cổng 1433).
3. **Restart lại dịch vụ SQL Server** sau khi đổi cấu hình.
