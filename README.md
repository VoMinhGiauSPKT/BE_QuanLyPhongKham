# MEDICARE CLINIC - BACKEND (JAVA JAKARTA EE 10 / TOMCAT 10)

Hệ thống Backend cho Đồ án **Quản Lý Phòng Khám Tư Nhân (Medicare Clinic)**, xây dựng trên nền tảng **Java Servlet / Jakarta EE 10**, chạy trên máy chủ **Apache Tomcat 10.1.x** và cơ sở dữ liệu **Microsoft SQL Server**.

---

## 📌 1. KIẾN TRÚC & CÔNG NGHỆ SỬ DỤNG

* **Ngôn ngữ & Nền tảng:** Java 17+ / Jakarta EE 10 (Servlet 6.0).
* **Web Server:** Apache Tomcat 10.1.x (Port mặc định: `8080`).
* **Hệ quản trị CSDL:** Microsoft SQL Server 2019 / 2022 (Port `1433`).
* **Quản lý Dependencies:** Apache Maven.
* **Bảo mật & Mã hóa:**
  * **JWT (JSON Web Token):** `io.jsonwebtoken` (jjwt 0.12.5) cấp phát và xác thực token không trạng thái (Stateless).
  * **BCrypt:** `org.mindrot:jbcrypt` băm mật khẩu người dùng với salt ngẫu nhiên.
  * **Database Least Privilege Security:** Không dùng tài khoản `sa`. Backend tự động chuyển đổi tài khoản kết nối CSDL theo đúng vai trò giải mã từ JWT (`tk_quan_tri`, `tk_bac_si1`, `tk_le_tan1`, `tk_duoc_si1`, `tk_xac_thuc`).
* **Xử lý JSON & Tiện ích:** `Google Gson 2.10.1`, `io.github.cdimascio:dotenv-java`.

---

## 📂 2. CẤU TRÚC THƯ MỤC BACKEND

```
QuanLyPhongKham/
├── .env                                  # File cấu hình biến môi trường CSDL & JWT
├── pom.xml                               # Quản lý thư viện Maven
├── sql/                                  # Bản sao các script SQL Server tổng hợp
│   ├── 00_CHAY_TAT_CA_SETUP_ALL.sql      # Script chạy 1-click toàn bộ CSDL
│   └── HUONG_DAN_CAI_DAT_DATABASE.md
└── src/main/java/com/mycompany/quanlyphongkham/
    ├── entity/                           # Các thực thể dữ liệu (NguoiDung, BenhNhan, HoaDon...)
    ├── middleware/                       # Bộ lọc bảo mật & lắng nghe vòng đời
    │   ├── JwtFilter.java                # Kiểm tra JWT Token, xác thực phân quyền
    │   ├── CorsFilter.java               # Cấu hình CORS cho phép React kết nối
    │   └── DatabaseCheckListener.java    # Tự động test kết nối DB khi khởi động Tomcat
    ├── module/                           # Các Controller/Servlet & DAO theo nghiệp vụ
    │   ├── admin/                        # Quản lý người dùng, dịch vụ, doanh thu bác sĩ
    │   ├── auth/                         # Đăng nhập & cấp phát JWT token
    │   ├── bacsi/                        # Khám bệnh, kê đơn thuốc, chỉ định xét nghiệm
    │   ├── duocsi/                       # Quản lý kho thuốc, cập nhật giá, cảnh báo tồn
    │   └── letan/                        # Tiếp nhận bệnh nhân, xếp số, thu viện phí
    └── util/                             # Tiện ích dùng chung
        ├── DBContext.java                # Quản lý kết nối SQL Server đa tài khoản
        ├── EnvConfig.java                # Đọc cấu hình từ file .env
        ├── JwtUtil.java                  # Tạo và verify JWT token
        └── PasswordUtil.java             # Mã hóa và kiểm tra mật khẩu BCrypt
```

---

## ⚙️ 3. YÊU CẦU MÔI TRƯỜNG (PREREQUISITES)

1. **Java Development Kit (JDK):** Phiên bản **17** trở lên (Khuyến nghị OpenJDK 17 hoặc 21).
2. **Apache Tomcat:** Phiên bản **10.1.x** *(Bắt buộc dùng Tomcat 10 trở lên vì dự án dùng gói `jakarta.*`, không dùng Tomcat 9).*
3. **Microsoft SQL Server:** 2019 / 2022 hoặc SQL Server Express.
4. **Apache Maven:** 3.8+ (đã tích hợp sẵn trong IntelliJ IDEA / NetBeans / Eclipse).

---

## 🗄️ 4. CẤU HÌNH CƠ SỞ DỮ LIỆU & FILE `.env`

### Bước 4.1: Khởi tạo Cơ sở dữ liệu trong SQL Server
Trước khi chạy Backend, mở **SQL Server Management Studio (SSMS)**:
1. Mở file [00_CHAY_TAT_CA_SETUP_ALL.sql](file:///d:/ghichumonhoc/hequantricsdl/doancuoiki/sql/00_CHAY_TAT_CA_SETUP_ALL.sql).
2. Nhấn **F5 (Execute)** để tạo Database `QuanLyPhongKhamDB`, 11 bảng, Sequence, Trigger, View, Function, Procedure, 5 Transaction và các tài khoản phân quyền.

### Bước 4.2: Cấu hình file `.env`
Kiểm tra file `.env` đặt tại thư mục gốc của project `QuanLyPhongKham/`:

```properties
# ==========================================
# CẤU HÌNH KẾT NỐI SQL SERVER LOCAL
# ==========================================
DB_HOST=localhost
DB_PORT=1433
DB_INSTANCE=
DB_NAME=QuanLyPhongKhamDB
DB_ENCRYPT=false
DB_TRUST_SERVER_CERT=true

# Tài khoản xác thực đăng nhập (SELECT trên nguoi_dung)
DB_USER_AUTH=tk_xac_thuc
DB_PASS_AUTH=AuthServicePass123!

# Tài khoản phân quyền theo từng Role CSDL
DB_USER_ADMIN=tk_quan_tri
DB_PASS_ADMIN=QuanTriMatKhau123!

DB_USER_BACSI=tk_bac_si1
DB_PASS_BACSI=BacSiMatKhau123!

DB_USER_LETAN=tk_le_tan1
DB_PASS_LETAN=LeTanMatKhau123!

DB_USER_DUOCSI=tk_duoc_si1
DB_PASS_DUOCSI=DuocSiMatKhau123!

# ==========================================
# CẤU HÌNH JWT & CORS
# ==========================================
JWT_SECRET=MedicareSecretKeyForProjectQuanLyPhongKham2026SecureKeyWithMin256BitsLength!
JWT_EXPIRATION_HOURS=24
CORS_ALLOWED_ORIGIN=http://localhost:5173
```

---

## 🚀 5. HƯỚNG DẪN KHỞI CHẠY BACKEND

### Cách 1: Chạy bằng Apache NetBeans
1. Mở NetBeans $\rightarrow$ **File** $\rightarrow$ **Open Project** $\rightarrow$ Chọn thư mục `QuanLyPhongKham`.
2. Chuột phải vào project $\rightarrow$ Chọn **Clean and Build**.
3. Cấu hình máy chủ Tomcat 10.1 trong tab **Services** $\rightarrow$ **Servers**.
4. Chuột phải vào project $\rightarrow$ Chọn **Run**.
5. Server sẽ khởi động tại: `http://localhost:8080/QuanLyPhongKham`.

### Cách 2: Chạy bằng IntelliJ IDEA Ultimate
1. Mở IntelliJ $\rightarrow$ **Open** $\rightarrow$ Chọn thư mục `QuanLyPhongKham`.
2. Vào **Run/Debug Configurations** $\rightarrow$ Bấm dấu `+` $\rightarrow$ Chọn **Tomcat Server** $\rightarrow$ **Local**.
3. Cấu hình:
   * **Tomcat Home:** Trỏ đến thư mục cài đặt Tomcat 10.1.x trên máy.
   * Tab **Deployment:** Bấm dấu `+` $\rightarrow$ Chọn **Artifact...** $\rightarrow$ Chọn `QuanLyPhongKham:war exploded`.
   * **Application context:** Điền chính xác `/QuanLyPhongKham`.
4. Bấm **Apply** và bấm nút **Run (Shift + F10)**.

### Cách 3: Chạy bằng dòng lệnh Maven & triển khai WAR thủ công
```bash
# Di chuyển vào thư mục Backend
cd QuanLyPhongKham

# Biên dịch và đóng gói thành file .war
mvn clean package

# Copy file war sang thư mục webapps của Tomcat (ví dụ Tomcat tại C:\apache-tomcat-10.1.x)
cp target/QuanLyPhongKham.war C:/apache-tomcat-10.1.x/webapps/

# Khởi động Tomcat
C:/apache-tomcat-10.1.x/bin/startup.bat
```

> [!NOTE]
> Khi Tomcat khởi động thành công, `DatabaseCheckListener` sẽ tự động in thông báo:
> `[DBContext] >>> Kết nối với database thành công! [Tài khoản: tk_xac_thuc] <<<`

---

## 🌐 6. DANH SÁCH API ENDPOINTS CHÍNH

Base URL: `http://localhost:8080/QuanLyPhongKham/api`

| Phân hệ | Phương thức | Endpoint | Chức năng | Yêu cầu Role |
| :--- | :---: | :--- | :--- | :--- |
| **Xác thực** | `POST` | `/auth/login` | Đăng nhập hệ thống & cấp JWT Token | Public |
| **Lễ tân** | `POST` | `/le-tan/tiep-nhan` | Tiếp nhận bệnh nhân & cấp số thứ tự | `LE_TAN`, `QUAN_TRI` |
| | `GET` | `/le-tan/hang-cho` | Lấy danh sách hàng chờ khám hôm nay | `LE_TAN`, `QUAN_TRI` |
| | `GET` | `/le-tan/cho-thanh-toan` | Danh sách ca khám chờ thu viện phí | `LE_TAN`, `QUAN_TRI` |
| | `POST` | `/le-tan/thanh-toan` | Thu tiền viện phí (Chạy Transaction) | `LE_TAN`, `QUAN_TRI` |
| | `POST` | `/le-tan/huy-lich` | Hủy lịch hẹn khám | `LE_TAN`, `QUAN_TRI` |
| **Bác sĩ** | `GET` | `/bac-si/danh-sach-cho` | Danh sách bệnh nhân chờ khám của bác sĩ | `BAC_SI`, `QUAN_TRI` |
| | `GET` | `/bac-si/lich-su` | Tra cứu lịch sử khám cũ của bệnh nhân | `BAC_SI`, `QUAN_TRI` |
| | `GET` | `/bac-si/thuoc-kha-dung` | Danh mục thuốc còn hàng & còn hạn dùng | `BAC_SI`, `QUAN_TRI` |
| | `POST` | `/bac-si/chi-dinh-dich-vu`| Chỉ định xét nghiệm, siêu âm, chụp X-quang | `BAC_SI`, `QUAN_TRI` |
| | `POST` | `/bac-si/hoan-tat-kham` | Kê đơn nhiều thuốc & chốt hồ sơ bệnh án | `BAC_SI`, `QUAN_TRI` |
| **Dược sĩ** | `GET` | `/duoc-si/kho` | Danh sách tồn kho dược phẩm | `DUOC_SI`, `QUAN_TRI` |
| | `POST` | `/duoc-si/nhap-kho` | Nhập thêm số lượng thuốc vào kho | `DUOC_SI`, `QUAN_TRI` |
| | `GET` | `/duoc-si/canh-bao` | Danh sách thuốc sắp hết hàng / cận date | `DUOC_SI`, `QUAN_TRI` |
| | `POST` | `/duoc-si/thuoc` | Thêm mới thuốc vào danh mục | `DUOC_SI`, `QUAN_TRI` |
| | `PUT` | `/duoc-si/thuoc` | Cập nhật thông tin/giá thuốc (Ghi audit log)| `DUOC_SI`, `QUAN_TRI` |
| **Quản trị** | `GET` | `/admin/nguoi-dung` | Danh sách tài khoản nhân viên phòng khám | `QUAN_TRI` |
| | `POST` | `/admin/nguoi-dung` | Thêm tài khoản nhân viên mới | `QUAN_TRI` |
| | `GET` | `/admin/dich-vu` | Quản lý danh mục dịch vụ cận lâm sàng | `QUAN_TRI` |
| | `GET` | `/admin/nang-suat-bac-si`| Thống kê số ca khám và doanh thu bác sĩ | `QUAN_TRI` |
| | `GET` | `/admin/doanh-thu-thoi-gian`| Báo cáo doanh thu theo khoảng ngày | `QUAN_TRI` |
