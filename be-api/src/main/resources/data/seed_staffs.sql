-- =========================================================
-- NHAN VIEN PHONG KHAM
-- Username format: ten + ma nhan vien viet thuong
-- Vi du: Do Manh Cuong + NV000001 -> cuongnv000001
-- Default raw password for seeded users: Nv_123
-- Stored password below is BCrypt(Nv_123).
-- =========================================================

INSERT INTO roles (code, name)
VALUES
('DENTIST', 'Nha sĩ'),
('NURSE', 'Y tá'),
('RECEPTIONIST', 'Lễ tân'),
('MANAGER', 'Quản lý')
ON CONFLICT (code) DO NOTHING;

WITH staff_seed (
    code,
    username,
    full_name,
    phone,
    email,
    dob,
    gender,
    address,
    avatar_url,
    staff_type,
    is_active,
    version
) AS (
    VALUES
    ('NV00001', 'minhnv000001',  'Nguyễn Hoàng Minh',  '0912000001', 'nguyen.hoang.minh@gmail.com',  DATE '1984-02-15', true,  '12 Lý Thường Kiệt, Hoàn Kiếm, Hà Nội',       null, 'DENTIST',      true, 0),
    ('NV00002', 'ngocnv000002',  'Trần Thị Bảo Ngọc',  '0912000002', 'tran.thi.bao.ngoc@gmail.com',  DATE '1988-07-21', false, '25 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',     null, 'DENTIST',      true, 0),
    ('NV00003', 'anhnv000003',   'Lê Quốc Anh',        '0912000003', 'le.quoc.anh@gmail.com',        DATE '1979-11-04', true,  '48 Trần Phú, Hải Châu, Đà Nẵng',             null, 'DENTIST',      true, 0),
    ('NV00004', 'hanv000004',    'Phạm Thu Hà',        '0912000004', 'pham.thu.ha@gmail.com',        DATE '1991-05-12', false, '31 Cách Mạng Tháng 8, Ninh Kiều, Cần Thơ', null, 'DENTIST',      true, 0),
    ('NV00005', 'longnv000005',  'Hoàng Đức Long',     '0912000005', 'hoang.duc.long@gmail.com',     DATE '1982-09-30', true,  '66 Lê Hồng Phong, Ngô Quyền, Hải Phòng',    null, 'DENTIST',      true, 0),
    ('NV00006', 'phuongnv000006','Huỳnh Mai Phương',   '0912000006', 'huynh.mai.phuong@gmail.com',   DATE '1990-01-18', false, '19 Bạch Đằng, Vĩnh Hải, Nha Trang',         null, 'DENTIST',      true, 0),
    ('NV00007', 'khainv000007',  'Phan Văn Khải',      '0912000007', 'phan.van.khai@gmail.com',      DATE '1986-06-25', true,  '72 Hùng Vương, Phú Xuân, Huế',              null, 'DENTIST',      true, 0),
    ('NV00008', 'tamnv000008',   'Vũ Thị Thanh Tâm',   '0912000008', 'vu.thi.thanh.tam@gmail.com',   DATE '1993-03-09', false, '8 Trần Hưng Đạo, Vũng Tàu',                 null, 'DENTIST',      true, 0),
    ('NV00009', 'quannv000009',  'Võ Minh Quân',       '0912000009', 'vo.minh.quan@gmail.com',       DATE '1981-12-17', true,  '93 Nguyễn Văn Cừ, Quy Nhơn',                null, 'DENTIST',      true, 0),
    ('NV00010', 'hannv000010',   'Đặng Ngọc Hân',      '0912000010', 'dang.ngoc.han@gmail.com',      DATE '1994-08-03', false, '39 Kim Mã, Ba Đình, Hà Nội',                null, 'DENTIST',      true, 0),

    ('NV00011', 'linhnv000011',  'Bùi Thị Mỹ Linh',    '0912000011', 'bui.thi.my.linh@gmail.com',    DATE '1995-04-20', false, '7 Võ Thị Sáu, Quận 3, TP. Hồ Chí Minh',     null, 'NURSE',        true, 0),
    ('NV00012', 'namnv000012',   'Đỗ Hữu Nam',         '0912000012', 'do.huu.nam@gmail.com',         DATE '1992-10-11', true,  '144 Ngô Gia Tự, Cẩm Lệ, Đà Nẵng',           null, 'NURSE',        true, 0),
    ('NV00013', 'diepnv000013',  'Hồ Thị Ngọc Diệp',   '0912000013', 'ho.thi.ngoc.diep@gmail.com',   DATE '1996-02-27', false, '30 Lý Tự Trọng, Ninh Kiều, Cần Thơ',        null, 'NURSE',        true, 0),
    ('NV00014', 'binhnv000014',  'Ngô Văn Bình',       '0912000014', 'ngo.van.binh@gmail.com',       DATE '1989-07-06', true,  '56 Quang Trung, Hồng Bàng, Hải Phòng',      null, 'NURSE',        true, 0),
    ('NV00015', 'nhinv000015',   'Dương Thị Yến Nhi',  '0912000015', 'duong.thi.yen.nhi@gmail.com',  DATE '1997-11-14', false, '22 Yersin, Nha Trang',                       null, 'NURSE',        true, 0),
    ('NV00016', 'khangnv000016', 'Lý Minh Khang',      '0912000016', 'ly.minh.khang@gmail.com',      DATE '1991-09-01', true,  '91 Trần Nguyên Hãn, Phú Xuân, Huế',         null, 'NURSE',        true, 0),
    ('NV00017', 'trangnv000017', 'Mai Thị Thu Trang',  '0912000017', 'mai.thi.thu.trang@gmail.com',  DATE '1998-06-19', false, '14 Lê Lợi, Vũng Tàu',                        null, 'NURSE',        true, 0),
    ('NV00018', 'vietnv000018',  'Đinh Quốc Việt',     '0912000018', 'dinh.quoc.viet@gmail.com',     DATE '1990-12-08', true,  '63 Phan Chu Trinh, Quy Nhơn',                null, 'NURSE',        true, 0),

    ('NV00019', 'anhnv000019',   'Trịnh Thị Kim Anh',  '0912000019', 'trinh.thi.kim.anh@gmail.com',  DATE '1994-01-26', false, '52 Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội',     null, 'RECEPTIONIST', true, 0),
    ('NV00020', 'baonv000020',   'Tô Gia Bảo',         '0912000020', 'to.gia.bao@gmail.com',         DATE '1993-05-31', true,  '3 Trần Nhân Tông, Quận 10, TP. Hồ Chí Minh',null, 'RECEPTIONIST', true, 0),
    ('NV00021', 'nhungnv000021', 'Cao Thị Hồng Nhung', '0912000021', 'cao.thi.hong.nhung@gmail.com', DATE '1996-08-15', false, '55 Nguyễn Chí Thanh, Hải Châu, Đà Nẵng',     null, 'RECEPTIONIST', true, 0),
    ('NV00022', 'hainv000022',   'Đào Minh Hải',       '0912000022', 'dao.minh.hai@gmail.com',       DATE '1992-03-22', true,  '47 Mậu Thân, Ninh Kiều, Cần Thơ',            null, 'RECEPTIONIST', true, 0),
    ('NV00023', 'chaunv000023',  'Hà Thị Bích Châu',   '0912000023', 'ha.thi.bich.chau@gmail.com',   DATE '1995-10-07', false, '18 Trần Bình Trọng, Lê Chân, Hải Phòng',    null, 'RECEPTIONIST', true, 0),
    ('NV00024', 'trungnv000024', 'Lâm Văn Trung',      '0912000024', 'lam.van.trung@gmail.com',      DATE '1987-04-13', true,  '36 Thái Nguyên, Nha Trang',                  null, 'RECEPTIONIST', true, 0),
    ('NV00025', 'xuannv000025',  'Lưu Thị Kim Xuân',   '0912000025', 'luu.thi.kim.xuan@gmail.com',   DATE '1999-02-05', false, '73 Phùng Khắc Khoan, Phú Xuân, Huế',        null, 'RECEPTIONIST', true, 0),
    ('NV00026', 'thangnv000026', 'Thái Văn Thắng',     '0912000026', 'thai.van.thang@gmail.com',     DATE '1991-09-17', true,  '10 Phan Đình Phùng, Vũng Tàu',               null, 'RECEPTIONIST', true, 0),

    ('NV00027', 'thynv000027',   'Tạ Thị Ngọc Thy',    '0912000027', 'ta.thi.ngoc.thy@gmail.com',    DATE '1985-12-24', false, '81 An Dương Vương, Quy Nhơn',                null, 'MANAGER',      true, 0),
    ('NV00028', 'vietnv000028',  'Triệu Thanh Việt',   '0912000028', 'trieu.thanh.viet@gmail.com',   DATE '1980-06-02', true,  '44 Giải Phóng, Đống Đa, Hà Nội',            null, 'RECEPTIONIST', true, 0),
    ('NV00029', 'linhnv000029',  'Nguyễn Khánh Linh',  '0912000029', 'nguyen.khanh.linh@gmail.com',  DATE '1989-11-16', false, '16 Bùi Thị Xuân, Quận 1, TP. Hồ Chí Minh', null, 'RECEPTIONIST', true, 0),
    ('NV00030', 'sonnv000030',   'Trần Hữu Sơn',       '0912000030', 'tran.huu.son@gmail.com',       DATE '1983-07-09', true,  '92 Hoàng Diệu, Hải Châu, Đà Nẵng',          null, 'RECEPTIONIST', true, 0)
),
inserted_users AS (
    INSERT INTO users (username, password, is_active, created_at, modified_at, role_id)
    SELECT
        s.username,
        '$2a$12$PfU1vglzhxAxEfQapiZT8u6faprdPIXZNZIQ.91X1aCA54iOK.rAe',
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        r.id
    FROM staff_seed s
    LEFT JOIN roles r ON r.code = s.staff_type
    ON CONFLICT (username) DO UPDATE
    SET password = EXCLUDED.password,
        role_id = COALESCE(users.role_id, EXCLUDED.role_id),
        modified_at = CURRENT_TIMESTAMP
    RETURNING username
)
INSERT INTO staffs (code, full_name, phone, email, dob, gender, address, avatar_url, staff_type, is_active, version, created_at, modified_at, user_id)
SELECT
    s.code,
    s.full_name,
    s.phone,
    s.email,
    s.dob,
    s.gender,
    s.address,
    s.avatar_url,
    s.staff_type,
    s.is_active,
    s.version,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    u.id
FROM staff_seed s
JOIN users u ON u.username = s.username
WHERE EXISTS (SELECT 1 FROM inserted_users)
ON CONFLICT (code) DO UPDATE
SET user_id = EXCLUDED.user_id,
    modified_at = CURRENT_TIMESTAMP;
