DROP DATABASE IF EXISTS quanlydiem2;
CREATE DATABASE quanlydiem2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quanlydiem2;

-- =============================================
-- 1. TẠO CẤU TRÚC BẢNG (STRUCTURE)
-- =============================================

CREATE TABLE khoa (
    maKhoa VARCHAR(10) PRIMARY KEY,
    tenKhoa VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE ky_hoc (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hocKy INT NOT NULL,
    namHoc INT NOT NULL,
    ngayBatDauNhapDiem DATETIME,
    ngayKetThucNhapDiem DATETIME,
    UNIQUE KEY uk_ky_nam (hocKy, namHoc)
);

CREATE TABLE lop_hanh_chinh (
    maLop VARCHAR(20) PRIMARY KEY,
    tenLop VARCHAR(100) NULL,
    maKhoa VARCHAR(10) NULL,
    khoaHoc INT COMMENT 'Khóa học, vd: 2022',
    FOREIGN KEY (maKhoa) REFERENCES khoa(maKhoa) ON DELETE SET NULL
);

CREATE TABLE monhoc (
    maMon VARCHAR(10) PRIMARY KEY,
    tenMon VARCHAR(100) NOT NULL,
    soTinChi INT NOT NULL CHECK (soTinChi > 0),
    loaiMon ENUM('Bắt buộc', 'Tự chọn') NOT NULL,
    hocKy INT NOT NULL CHECK (hocKy > 0), 
    maKhoa VARCHAR(10) NULL, 
    monTienQuyet VARCHAR(10) NULL,
    FOREIGN KEY (maKhoa) REFERENCES khoa(maKhoa) ON DELETE SET NULL
);

CREATE TABLE giangvien (
    maGV VARCHAR(10) PRIMARY KEY,
    hoTen VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    maKhoa VARCHAR(10) NULL,
    FOREIGN KEY (maKhoa) REFERENCES khoa(maKhoa) ON DELETE SET NULL
);

CREATE TABLE sinhvien (
    maSV VARCHAR(10) PRIMARY KEY,
    hoTen VARCHAR(100) NOT NULL,
    ngaySinh DATE NULL,
    gioiTinh ENUM('Nam', 'Nữ') NULL,
    queQuan VARCHAR(200) NULL,
    maKhoa VARCHAR(10) NULL,
    maLop VARCHAR(20) NULL,
    diemTichLuy FLOAT DEFAULT 0.0,
    FOREIGN KEY (maKhoa) REFERENCES khoa(maKhoa) ON DELETE SET NULL,
    -- Quan trọng: Khóa ngoại này yêu cầu bảng lop_hanh_chinh phải có dữ liệu trước
    FOREIGN KEY (maLop) REFERENCES lop_hanh_chinh(maLop) ON DELETE SET NULL
);

CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'giangvien', 'sinhvien') NOT NULL,
    maSV VARCHAR(10) NULL UNIQUE,
    maGV VARCHAR(10) NULL UNIQUE,
    FOREIGN KEY (maSV) REFERENCES sinhvien(maSV) ON DELETE CASCADE,
    FOREIGN KEY (maGV) REFERENCES giangvien(maGV) ON DELETE CASCADE
);

CREATE TABLE lop_hoc_phan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    maGV VARCHAR(10) NOT NULL,
    maMon VARCHAR(10) NOT NULL,
    idKyHoc INT NOT NULL,
    maLopHanhChinh VARCHAR(20) NULL,
    tenLopHocPhan VARCHAR(50) NULL,
    loaiLop ENUM('HanhChinh', 'NguyenVong') DEFAULT 'HanhChinh',
    trangThai ENUM('CHO_DUYET', 'DA_DUYET', 'DA_HUY', 'DA_KHOA_DIEM') DEFAULT 'DA_DUYET',
    lyDoMoLop TEXT NULL,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayDuyet DATETIME NULL,
    FOREIGN KEY (maGV) REFERENCES giangvien(maGV),
    FOREIGN KEY (maMon) REFERENCES monhoc(maMon),
    FOREIGN KEY (maLopHanhChinh) REFERENCES lop_hanh_chinh(maLop),
    FOREIGN KEY (idKyHoc) REFERENCES ky_hoc(id)
);

CREATE TABLE ket_qua_hoc_tap (
    id INT AUTO_INCREMENT PRIMARY KEY,
    idLopHocPhan INT NOT NULL,
    maSV VARCHAR(10) NOT NULL,
    diemQT FLOAT NULL CHECK (diemQT BETWEEN 0 AND 10),
    diemThi FLOAT NULL CHECK (diemThi BETWEEN 0 AND 10),
    diemTB FLOAT GENERATED ALWAYS AS (ROUND(IFNULL(diemQT,0) * 0.4 + IFNULL(diemThi,0) * 0.6, 1)) STORED,
    trangThai ENUM('Dang_Ky', 'Dang_Hoc', 'Qua_Mon', 'Rot_Mon', 'Cam_Thi') DEFAULT 'Dang_Hoc',
    ngayDangKy TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idLopHocPhan) REFERENCES lop_hoc_phan(id) ON DELETE CASCADE,
    FOREIGN KEY (maSV) REFERENCES sinhvien(maSV) ON DELETE CASCADE,
    UNIQUE KEY unique_sv_lhp (idLopHocPhan, maSV)
);

-- =============================================
-- 2. NẠP DỮ LIỆU (DATA) - THEO THỨ TỰ CHUẨN XÁC
-- =============================================

-- Bước 1: Nạp Kỳ Học
INSERT INTO ky_hoc (hocKy, namHoc, ngayBatDauNhapDiem, ngayKetThucNhapDiem) VALUES 
(1, 2021, '2021-01-01', '2021-02-28'),
(2, 2021, '2021-06-01', '2021-07-31'),
(1, 2022, '2022-01-01', '2022-02-28'),
(2, 2022, '2022-06-01', '2022-07-31'),
(1, 2023, '2023-01-01', '2023-02-28'),
(2, 2023, '2023-06-01', '2023-07-31'),
(1, 2024, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY), 
(2, 2024, NOW() + INTERVAL 4 MONTH, NOW() + INTERVAL 5 MONTH); 

-- Bước 2: Nạp Khoa (Bắt buộc có trước Lớp và GV)
INSERT INTO khoa (maKhoa, tenKhoa) VALUES 
('CNTT', 'Công nghệ Thông tin'),
('KT', 'Kinh tế & Quản trị Kinh doanh'),
('NN', 'Ngoại ngữ'),
('XD', 'Xây dựng Dân dụng'),
('DIEN', 'Điện - Điện tử'),
('CK', 'Cơ khí');

-- Bước 3: Nạp Lớp Hành Chính (Bắt buộc có trước Sinh viên để tránh lỗi khóa ngoại)
INSERT INTO lop_hanh_chinh (maLop, tenLop, maKhoa, khoaHoc) VALUES 
('CNTT1-K15', 'CNTT K15 Lớp 1', 'CNTT', 2021),
('CNTT2-K15', 'CNTT K15 Lớp 2', 'CNTT', 2021),
('CNTT1-K16', 'CNTT K16 Lớp 1', 'CNTT', 2022),
('KTPM1-K16', 'Kỹ thuật phần mềm K16', 'CNTT', 2022),
('KT1-K16', 'Kinh tế K16 Lớp 1', 'KT', 2022),
('QKD1-K16', 'Quản trị kinh doanh K16', 'KT', 2022),
('NNA1-K17', 'Ngôn ngữ Anh K17', 'NN', 2023),
('DIEN1-K17', 'Kỹ thuật điện K17', 'DIEN', 2023),
('CK1-K17', 'Cơ khí chế tạo máy K17', 'CK', 2023);

-- Bước 4: Nạp Môn Học
INSERT INTO monhoc (maMon, tenMon, soTinChi, loaiMon, hocKy, maKhoa, monTienQuyet) VALUES
('TRIET1', 'Triết học Mác - Lênin', 3, 'Bắt buộc', 1, NULL, NULL),
('TOAN1', 'Toán Cao Cấp 1', 3, 'Bắt buộc', 1, NULL, NULL),
('ANH1', 'Tiếng Anh cơ bản 1', 3, 'Bắt buộc', 1, 'NN', NULL),
('VLDC', 'Vật lý đại cương', 3, 'Bắt buộc', 1, NULL, NULL),
('IT101', 'Nhập môn Lập trình (C)', 3, 'Bắt buộc', 1, 'CNTT', NULL),
('IT102', 'Kỹ thuật lập trình', 3, 'Bắt buộc', 2, 'CNTT', 'IT101'),
('IT103', 'Cấu trúc rời rạc', 3, 'Bắt buộc', 2, 'CNTT', NULL),
('IT201', 'Cấu trúc dữ liệu & GT', 3, 'Bắt buộc', 3, 'CNTT', 'IT102'),
('IT202', 'Kiến trúc máy tính', 3, 'Bắt buộc', 3, 'CNTT', NULL),
('IT203', 'Cơ sở dữ liệu', 3, 'Bắt buộc', 4, 'CNTT', NULL),
('IT204', 'Mạng máy tính', 3, 'Bắt buộc', 4, 'CNTT', NULL),
('IT301', 'Lập trình Java', 3, 'Bắt buộc', 5, 'CNTT', 'IT102'),
('IT302', 'Phân tích thiết kế hệ thống', 3, 'Bắt buộc', 5, 'CNTT', NULL),
('IT303', 'Công nghệ Web', 3, 'Tự chọn', 6, 'CNTT', 'IT301'),
('IT304', 'Trí tuệ nhân tạo', 3, 'Tự chọn', 6, 'CNTT', 'IT201'),
('IT401', 'Thực tập tốt nghiệp', 2, 'Bắt buộc', 7, 'CNTT', NULL),
('IT402', 'Đồ án tốt nghiệp', 10, 'Bắt buộc', 8, 'CNTT', NULL),
('KT101', 'Kinh tế vi mô', 3, 'Bắt buộc', 1, 'KT', NULL),
('KT102', 'Kinh tế vĩ mô', 3, 'Bắt buộc', 2, 'KT', 'KT101'),
('KT201', 'Nguyên lý kế toán', 3, 'Bắt buộc', 3, 'KT', NULL),
('DIEN101', 'Mạch điện 1', 3, 'Bắt buộc', 2, 'DIEN', 'VLDC'),
('CK101', 'Hình họa - Vẽ kỹ thuật', 3, 'Bắt buộc', 1, 'CK', NULL);

-- Bước 5: Nạp Giảng Viên
INSERT INTO giangvien (maGV, hoTen, email, maKhoa) VALUES
('GV01', 'Nguyễn Văn A', 'a.nv@school.edu', 'CNTT'),
('GV02', 'Trần Thị B', 'b.tt@school.edu', 'CNTT'),
('GV03', 'Lê Văn C', 'c.lv@school.edu', 'KT'),
('GV04', 'Phạm Thị D', 'd.pt@school.edu', 'NN'),
('GV05', 'Hoàng Văn E', 'e.hv@school.edu', 'DIEN'),
('GV06', 'Vũ Thị F', 'f.vt@school.edu', 'CK'),
('GV07', 'Ngô Văn G', 'g.nv@school.edu', NULL);

-- Bước 6: Nạp Sinh Viên (Bây giờ sẽ an toàn vì Lớp đã có ở Bước 3)
INSERT INTO sinhvien (maSV, hoTen, ngaySinh, gioiTinh, queQuan, maKhoa, maLop) VALUES
('SV01', 'Nguyễn Thành Đạt', '2003-01-01', 'Nam', 'Hà Nội', 'CNTT', 'CNTT1-K15'),
('SV02', 'Trần Văn Tạch', '2003-05-05', 'Nam', 'Nam Định', 'CNTT', 'CNTT1-K15'),
('SV03', 'Lê Thị Mới', '2003-09-02', 'Nữ', 'Đà Nẵng', 'CNTT', 'CNTT1-K15'),
('SV04', 'Phạm Văn Hùng', '2003-12-12', 'Nam', 'Hải Phòng', 'CNTT', 'CNTT1-K15'),
('SV05', 'Hoàng Thị Lan', '2004-03-15', 'Nữ', 'Thanh Hóa', 'CNTT', 'CNTT1-K16'),
('SV06', 'Vũ Đức Minh', '2004-07-20', 'Nam', 'Nghệ An', 'CNTT', 'CNTT1-K16'),
('SV07', 'Đặng Thu Thảo', '2004-11-11', 'Nữ', 'Cần Thơ', 'KT', 'KT1-K16'),
('SV08', 'Bùi Văn Tuấn', '2004-02-28', 'Nam', 'Quảng Ninh', 'KT', 'KT1-K16'),
('SV09', 'Lý Quốc Bảo', '2005-05-10', 'Nam', 'TP.HCM', 'DIEN', 'DIEN1-K17'),
('SV10', 'Trương Mỹ Linh', '2005-08-18', 'Nữ', 'Huế', 'NN', 'NNA1-K17');

-- Bước 7: Nạp User (Pass: 123)
INSERT INTO users (username, password, role, maSV, maGV) VALUES
('admin', '$2a$10$gvto96Wifxzi8ka/gLwgWuOpESb6SKuqBXe7EiylARskCUDAAP00q', 'admin', NULL, NULL),
('gv01', '$2a$10$gvto96Wifxzi8ka/gLwgWuOpESb6SKuqBXe7EiylARskCUDAAP00q', 'giangvien', NULL, 'GV01'),
('gv02', '$2a$10$gvto96Wifxzi8ka/gLwgWuOpESb6SKuqBXe7EiylARskCUDAAP00q', 'giangvien', NULL, 'GV02'),
('gv03', '$2a$10$gvto96Wifxzi8ka/gLwgWuOpESb6SKuqBXe7EiylARskCUDAAP00q', 'giangvien', NULL, 'GV03'),
('sv01', '$2a$10$gvto96Wifxzi8ka/gLwgWuOpESb6SKuqBXe7EiylARskCUDAAP00q', 'sinhvien', 'SV01', NULL),
('sv02', '$2a$10$gvto96Wifxzi8ka/gLwgWuOpESb6SKuqBXe7EiylARskCUDAAP00q', 'sinhvien', 'SV02', NULL),
('sv05', '$2a$10$gvto96Wifxzi8ka/gLwgWuOpESb6SKuqBXe7EiylARskCUDAAP00q', 'sinhvien', 'SV05', NULL);

-- Bước 8: Nạp Lớp Học Phần & Điểm
-- >>> KỲ 1 NĂM 2021 <<<
INSERT INTO lop_hoc_phan (id, maGV, maMon, idKyHoc, loaiLop, trangThai, maLopHanhChinh) VALUES
(10, 'GV01', 'IT101', 1, 'HanhChinh', 'DA_KHOA_DIEM', 'CNTT1-K15'),
(11, 'GV03', 'TRIET1', 1, 'HanhChinh', 'DA_KHOA_DIEM', 'CNTT1-K15'),
(12, 'GV04', 'ANH1', 1, 'HanhChinh', 'DA_KHOA_DIEM', 'CNTT1-K15');

INSERT INTO ket_qua_hoc_tap (idLopHocPhan, maSV, diemQT, diemThi, trangThai) VALUES
(10, 'SV01', 9.0, 9.0, 'Qua_Mon'), (11, 'SV01', 8.5, 8.0, 'Qua_Mon'), (12, 'SV01', 9.0, 9.5, 'Qua_Mon'),
(10, 'SV02', 4.0, 3.0, 'Rot_Mon'), (11, 'SV02', 6.0, 6.0, 'Qua_Mon'), (12, 'SV02', 5.0, 5.0, 'Qua_Mon'),
(10, 'SV03', 7.0, 7.5, 'Qua_Mon'), (11, 'SV03', 6.0, 6.5, 'Qua_Mon'),
(10, 'SV04', 8.0, 7.0, 'Qua_Mon'), (11, 'SV04', 7.5, 8.0, 'Qua_Mon');

-- >>> KỲ 2 NĂM 2021 <<<
INSERT INTO lop_hoc_phan (id, maGV, maMon, idKyHoc, loaiLop, trangThai, maLopHanhChinh) VALUES
(20, 'GV01', 'IT102', 2, 'HanhChinh', 'DA_KHOA_DIEM', 'CNTT1-K15'),
(21, 'GV01', 'IT103', 2, 'HanhChinh', 'DA_KHOA_DIEM', 'CNTT1-K15');

INSERT INTO ket_qua_hoc_tap (idLopHocPhan, maSV, diemQT, diemThi, trangThai) VALUES
(20, 'SV01', 8.5, 9.0, 'Qua_Mon'), (21, 'SV01', 9.0, 8.0, 'Qua_Mon'),
(20, 'SV02', 6.0, 5.0, 'Qua_Mon'), (21, 'SV02', 5.0, 4.0, 'Qua_Mon');

-- >>> KỲ 1 NĂM 2022 <<<
INSERT INTO lop_hoc_phan (id, maGV, maMon, idKyHoc, loaiLop, trangThai, maLopHanhChinh) VALUES
(30, 'GV01', 'IT201', 3, 'HanhChinh', 'DA_KHOA_DIEM', 'CNTT1-K15');

INSERT INTO ket_qua_hoc_tap (idLopHocPhan, maSV, diemQT, diemThi, trangThai) VALUES
(30, 'SV01', 9.5, 9.0, 'Qua_Mon'),
(30, 'SV02', 7.0, 6.0, 'Qua_Mon');

-- >>> KỲ HIỆN TẠI (Kỳ 1 Năm 2024) - Lấy ID động <<<
SET @id_ky_hien_tai = (SELECT id FROM ky_hoc WHERE hocKy = 1 AND namHoc = 2024 LIMIT 1);

-- Môn IT303
INSERT INTO lop_hoc_phan (maGV, maMon, idKyHoc, loaiLop, trangThai, maLopHanhChinh) 
VALUES ('GV01', 'IT303', @id_ky_hien_tai, 'HanhChinh', 'DA_DUYET', 'CNTT1-K15');
SET @id_lop_java = LAST_INSERT_ID();

-- Môn IT203
INSERT INTO lop_hoc_phan (maGV, maMon, idKyHoc, loaiLop, trangThai, maLopHanhChinh) 
VALUES ('GV02', 'IT203', @id_ky_hien_tai, 'HanhChinh', 'DA_DUYET', 'CNTT1-K16');
SET @id_lop_csdl = LAST_INSERT_ID();

-- Lớp Nguyện Vọng (Học lại)
INSERT INTO lop_hoc_phan (maGV, maMon, idKyHoc, loaiLop, trangThai, lyDoMoLop, tenLopHocPhan) 
VALUES ('GV01', 'IT101', @id_ky_hien_tai, 'NguyenVong', 'DA_DUYET', 'Mở cho sinh viên K15 trả nợ môn', 'NV_IT101_HocLai');
SET @id_lop_hoclai = LAST_INSERT_ID();

-- Đăng ký sinh viên
INSERT INTO ket_qua_hoc_tap (idLopHocPhan, maSV, diemQT, diemThi, trangThai) VALUES
(@id_lop_java, 'SV01', 8.5, NULL, 'Dang_Hoc'),
(@id_lop_java, 'SV03', 7.0, NULL, 'Dang_Hoc'),
(@id_lop_java, 'SV04', 6.5, NULL, 'Dang_Hoc'),
(@id_lop_csdl, 'SV05', 8.0, NULL, 'Dang_Hoc'),
(@id_lop_csdl, 'SV06', 7.5, NULL, 'Dang_Hoc'),
(@id_lop_hoclai, 'SV02', 6.0, NULL, 'Dang_Hoc'); 

-- Yêu cầu mở lớp (Chờ duyệt)
INSERT INTO lop_hoc_phan (maGV, maMon, idKyHoc, loaiLop, trangThai, lyDoMoLop, tenLopHocPhan) 
VALUES ('GV02', 'IT204', @id_ky_hien_tai, 'NguyenVong', 'CHO_DUYET', 'Sinh viên yêu cầu học sớm Mạng máy tính', 'NV_IT204_HocSom');