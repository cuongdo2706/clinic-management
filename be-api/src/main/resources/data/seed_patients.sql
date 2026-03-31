-- =====================================================
-- Seed 100 patients for dental_clinic_management
-- Run against PostgreSQL
-- =====================================================

INSERT INTO patients (code, full_name, dob, gender, phone, identity_number, email, address, medical_history, created_at, modified_at)
VALUES
-- 1-10
('BN000001', N'Nguyễn Thanh Tùng',       '1990-03-15', true,  '0901234001', '079090123001', 'thanhtung90@gmail.com',        '12 Nguyễn Huệ, Q.1, TP.HCM',             NULL, NOW(), NOW()),
('BN000002', N'Trần Thị Mai Anh',         '1985-07-22', false, '0901234002', '079085123002', 'maianh85@gmail.com',           '45 Lê Lợi, Q.1, TP.HCM',                 'Dị ứng penicillin', NOW(), NOW()),
('BN000003', N'Phạm Hoàng Long',          '1978-11-08', true,  '0901234003', '079078123003', 'hoanglong78@gmail.com',        '78 Trần Hưng Đạo, Q.5, TP.HCM',          NULL, NOW(), NOW()),
('BN000004', N'Lê Thị Hồng Nhung',        '1995-01-30', false, '0901234004', '079095123004', 'hongnhung95@gmail.com',        '23 Hai Bà Trưng, Q.3, TP.HCM',           NULL, NOW(), NOW()),
('BN000005', N'Võ Minh Quân',             '1988-06-14', true,  '0901234005', '079088123005', 'minhquan88@gmail.com',         '56 Pasteur, Q.3, TP.HCM',                'Tiểu đường type 2', NOW(), NOW()),
('BN000006', N'Đặng Thùy Linh',           '1992-09-25', false, '0901234006', '079092123006', 'thuylinh92@gmail.com',         '90 Cách Mạng Tháng 8, Q.10, TP.HCM',    NULL, NOW(), NOW()),
('BN000007', N'Bùi Đức Anh',              '1983-04-17', true,  '0901234007', '079083123007', 'ducanh83@gmail.com',           '34 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', NULL, NOW(), NOW()),
('BN000008', N'Hoàng Thị Phương Thảo',    '1997-12-03', false, '0901234008', '079097123008', 'phuongthao97@gmail.com',       '67 Nguyễn Thị Minh Khai, Q.3, TP.HCM',  NULL, NOW(), NOW()),
('BN000009', N'Ngô Quốc Bảo',             '1980-08-19', true,  '0901234009', '079080123009', 'quocbao80@gmail.com',          '101 Võ Văn Tần, Q.3, TP.HCM',            'Huyết áp cao', NOW(), NOW()),
('BN000010', N'Dương Thị Thanh Hằng',     '1991-02-11', false, '0901234010', '079091123010', 'thanhhang91@gmail.com',        '15 Lý Tự Trọng, Q.1, TP.HCM',           NULL, NOW(), NOW()),

-- 11-20
('BN000011', N'Trịnh Văn Hào',            '1986-05-28', true,  '0901234011', '079086123011', 'vanhao86@gmail.com',           '42 Nguyễn Đình Chiểu, Q.3, TP.HCM',     NULL, NOW(), NOW()),
('BN000012', N'Phan Thị Ngọc Trâm',       '1993-10-07', false, '0901234012', '079093123012', 'ngoctram93@gmail.com',         '88 Trường Chinh, Q.Tân Bình, TP.HCM',   NULL, NOW(), NOW()),
('BN000013', N'Huỳnh Tấn Phát',           '1975-03-21', true,  '0901234013', '079075123013', 'tanphat75@gmail.com',          '5 Huỳnh Tấn Phát, Q.7, TP.HCM',         'Viêm gan B', NOW(), NOW()),
('BN000014', N'Mai Thị Cẩm Tú',           '1998-08-16', false, '0901234014', '079098123014', 'camtu98@gmail.com',            '120 Nguyễn Văn Linh, Q.7, TP.HCM',      NULL, NOW(), NOW()),
('BN000015', N'Đỗ Trọng Nhân',            '1982-12-29', true,  '0901234015', '079082123015', 'trongnhan82@gmail.com',        '33 Phạm Văn Đồng, Q.Thủ Đức, TP.HCM',  NULL, NOW(), NOW()),
('BN000016', N'Lý Thị Bích Ngọc',         '1989-07-04', false, '0901234016', '079089123016', 'bichngoc89@gmail.com',         '77 Quang Trung, Q.Gò Vấp, TP.HCM',     NULL, NOW(), NOW()),
('BN000017', N'Tạ Quang Huy',             '1994-01-18', true,  '0901234017', '079094123017', 'quanghuy94@gmail.com',         '210 Xô Viết Nghệ Tĩnh, Q.Bình Thạnh',  NULL, NOW(), NOW()),
('BN000018', N'Vũ Thị Diệu Hiền',        '1987-06-09', false, '0901234018', '079087123018', 'dieuhien87@gmail.com',         '55 Bạch Đằng, Q.Tân Bình, TP.HCM',     'Hen suyễn', NOW(), NOW()),
('BN000019', N'Châu Minh Đức',            '1996-11-22', true,  '0901234019', '079096123019', 'minhduc96@gmail.com',          '8 Lê Văn Sỹ, Q.Phú Nhuận, TP.HCM',    NULL, NOW(), NOW()),
('BN000020', N'Đinh Thị Hạnh Dung',       '1981-04-05', false, '0901234020', '079081123020', 'hanhdung81@gmail.com',         '142 Phan Xích Long, Q.Phú Nhuận',       NULL, NOW(), NOW()),

-- 21-30
('BN000021', N'Lương Thế Vinh',           '1990-09-12', true,  '0901234021', '079090123021', 'thevinh90@gmail.com',          '66 Nam Kỳ Khởi Nghĩa, Q.1, TP.HCM',    NULL, NOW(), NOW()),
('BN000022', N'Tô Thị Minh Châu',         '1984-02-27', false, '0901234022', '079084123022', 'minhchau84@gmail.com',         '19 Đồng Khởi, Q.1, TP.HCM',             NULL, NOW(), NOW()),
('BN000023', N'Hồ Sỹ Hùng',              '1977-07-31', true,  '0901234023', '079077123023', 'syhung77@gmail.com',           '203 Lạc Long Quân, Q.11, TP.HCM',       'Tim mạch', NOW(), NOW()),
('BN000024', N'Nguyễn Thị Quỳnh Như',     '1999-05-19', false, '0901234024', '079099123024', 'quynhnhu99@gmail.com',         '48 Hoàng Văn Thụ, Q.Phú Nhuận, TP.HCM', NULL, NOW(), NOW()),
('BN000025', N'Trần Đại Nghĩa',           '1986-10-08', true,  '0901234025', '079086123025', 'dainghia86@gmail.com',         '91 Nguyễn Trãi, Q.5, TP.HCM',           NULL, NOW(), NOW()),
('BN000026', N'Phạm Thị Thanh Xuân',      '1993-03-14', false, '0901234026', '079093123026', 'thanhxuan93@gmail.com',        '27 Lê Đại Hành, Q.11, TP.HCM',          NULL, NOW(), NOW()),
('BN000027', N'Lê Hoàng Thiên',           '1988-08-23', true,  '0901234027', '079088123027', 'hoangthien88@gmail.com',       '163 Tô Hiến Thành, Q.10, TP.HCM',       NULL, NOW(), NOW()),
('BN000028', N'Võ Thị Mỹ Duyên',         '1995-12-01', false, '0901234028', '079095123028', 'myduyen95@gmail.com',          '74 Sư Vạn Hạnh, Q.10, TP.HCM',          NULL, NOW(), NOW()),
('BN000029', N'Đặng Quốc Trung',          '1979-06-17', true,  '0901234029', '079079123029', 'quoctrung79@gmail.com',        '38 Ba Tháng Hai, Q.10, TP.HCM',         'Dị ứng thuốc tê lidocaine', NOW(), NOW()),
('BN000030', N'Bùi Thị Kim Oanh',         '1991-01-26', false, '0901234030', '079091123030', 'kimoanh91@gmail.com',          '115 Hùng Vương, Q.5, TP.HCM',           NULL, NOW(), NOW()),

-- 31-40
('BN000031', N'Hoàng Minh Trí',           '1983-11-09', true,  '0901234031', '079083123031', 'minhtri83@gmail.com',          '52 Nguyễn Thái Học, Q.1, TP.HCM',       NULL, NOW(), NOW()),
('BN000032', N'Ngô Thị Yến Nhi',          '1997-04-20', false, '0901234032', '079097123032', 'yennhi97@gmail.com',           '200 Cộng Hòa, Q.Tân Bình, TP.HCM',     NULL, NOW(), NOW()),
('BN000033', N'Dương Hải Đăng',           '1985-09-03', true,  '0901234033', '079085123033', 'haidang85@gmail.com',          '81 Phan Đăng Lưu, Q.Phú Nhuận, TP.HCM', NULL, NOW(), NOW()),
('BN000034', N'Trịnh Thị Lan Hương',      '1992-02-14', false, '0901234034', '079092123034', 'lanhuong92@gmail.com',         '9 Nguyễn Kiệm, Q.Gò Vấp, TP.HCM',     'Dị ứng aspirin', NOW(), NOW()),
('BN000035', N'Phan Anh Tuấn',            '1976-07-28', true,  '0901234035', '079076123035', 'anhtuan76@gmail.com',          '137 Âu Cơ, Q.Tân Bình, TP.HCM',        NULL, NOW(), NOW()),
('BN000036', N'Huỳnh Thị Diễm Quỳnh',    '1998-12-11', false, '0901234036', '079098123036', 'diemquynh98@gmail.com',        '64 Thành Thái, Q.10, TP.HCM',           NULL, NOW(), NOW()),
('BN000037', N'Mai Xuân Bách',             '1989-05-06', true,  '0901234037', '079089123037', 'xuanbach89@gmail.com',         '176 Lý Thường Kiệt, Q.Tân Bình',       NULL, NOW(), NOW()),
('BN000038', N'Đỗ Thị Phương Anh',        '1994-10-30', false, '0901234038', '079094123038', 'phuonganh94@gmail.com',        '41 Nguyễn Công Trứ, Q.1, TP.HCM',      NULL, NOW(), NOW()),
('BN000039', N'Lý Quốc Khánh',            '1981-03-25', true,  '0901234039', '079081123039', 'quockhanh81@gmail.com',        '93 Bùi Viện, Q.1, TP.HCM',              'Tiểu đường type 1', NOW(), NOW()),
('BN000040', N'Tạ Thị Hồng Vân',          '1996-08-18', false, '0901234040', '079096123040', 'hongvan96@gmail.com',          '28 Đề Thám, Q.1, TP.HCM',              NULL, NOW(), NOW()),

-- 41-50
('BN000041', N'Vũ Thành Nam',             '1987-01-07', true,  '0901234041', '079087123041', 'thanhnam87@gmail.com',         '155 Hoàng Sa, Q.Tân Bình, TP.HCM',     NULL, NOW(), NOW()),
('BN000042', N'Châu Thị Bảo Trân',        '1993-06-21', false, '0901234042', '079093123042', 'baotran93@gmail.com',          '70 Trường Sa, Q.Phú Nhuận, TP.HCM',    NULL, NOW(), NOW()),
('BN000043', N'Đinh Công Danh',            '1980-11-14', true,  '0901234043', '079080123043', 'congdanh80@gmail.com',         '112 Nguyễn Văn Trỗi, Q.Phú Nhuận',     'Huyết áp cao, hay chảy máu chân răng', NOW(), NOW()),
('BN000044', N'Lương Thị Tuyết Mai',       '1990-04-02', false, '0901234044', '079090123044', 'tuyetmai90@gmail.com',         '35 Phan Kế Bính, Q.1, TP.HCM',         NULL, NOW(), NOW()),
('BN000045', N'Tô Minh Khôi',             '1984-09-16', true,  '0901234045', '079084123045', 'minhkhoi84@gmail.com',         '198 Nguyễn Xí, Q.Bình Thạnh, TP.HCM', NULL, NOW(), NOW()),
('BN000046', N'Hồ Thị Ánh Tuyết',         '1977-02-08', false, '0901234046', '079077123046', 'anhtuyet77@gmail.com',         '53 Nơ Trang Long, Q.Bình Thạnh',       NULL, NOW(), NOW()),
('BN000047', N'Nguyễn Hữu Thắng',         '1999-07-24', true,  '0901234047', '079099123047', 'huuthang99@gmail.com',         '86 Chu Văn An, Q.Bình Thạnh, TP.HCM', NULL, NOW(), NOW()),
('BN000048', N'Trần Thị Khánh Linh',      '1986-12-19', false, '0901234048', '079086123048', 'khanhlinh86@gmail.com',        '144 Đinh Tiên Hoàng, Q.Bình Thạnh',    'Mang thai tháng thứ 6', NOW(), NOW()),
('BN000049', N'Phạm Gia Bảo',             '1991-05-11', true,  '0901234049', '079091123049', 'giabao91@gmail.com',           '21 Bà Huyện Thanh Quan, Q.3, TP.HCM', NULL, NOW(), NOW()),
('BN000050', N'Lê Thị Huyền Trang',       '1988-10-04', false, '0901234050', '079088123050', 'huyentrang88@gmail.com',       '177 Võ Thị Sáu, Q.3, TP.HCM',          NULL, NOW(), NOW()),

-- 51-60
('BN000051', N'Võ Tấn Lực',               '1982-03-30', true,  '0901234051', '079082123051', 'tanluc82@gmail.com',           '62 Trần Quốc Thảo, Q.3, TP.HCM',       NULL, NOW(), NOW()),
('BN000052', N'Đặng Thị Ngọc Hân',        '1995-08-13', false, '0901234052', '079095123052', 'ngochan95@gmail.com',          '99 Kỳ Đồng, Q.3, TP.HCM',              NULL, NOW(), NOW()),
('BN000053', N'Bùi Quang Vinh',           '1978-01-27', true,  '0901234053', '079078123053', 'quangvinh78@gmail.com',        '131 Nguyễn Thượng Hiền, Q.Phú Nhuận',  'Đái tháo đường, dùng insulin', NOW(), NOW()),
('BN000054', N'Hoàng Thị Thu Hà',         '1997-06-09', false, '0901234054', '079097123054', 'thuha97@gmail.com',            '47 Lê Quang Định, Q.Bình Thạnh',       NULL, NOW(), NOW()),
('BN000055', N'Ngô Duy Khánh',            '1985-11-22', true,  '0901234055', '079085123055', 'duykhanh85@gmail.com',         '183 Phan Văn Trị, Q.Gò Vấp, TP.HCM', NULL, NOW(), NOW()),
('BN000056', N'Dương Thị Mỹ Linh',        '1992-04-15', false, '0901234056', '079092123056', 'mylinh92@gmail.com',           '26 Nguyễn Oanh, Q.Gò Vấp, TP.HCM',    NULL, NOW(), NOW()),
('BN000057', N'Trịnh Hoàng Sơn',          '1989-09-08', true,  '0901234057', '079089123057', 'hoangson89@gmail.com',         '158 Lê Đức Thọ, Q.Gò Vấp, TP.HCM',   NULL, NOW(), NOW()),
('BN000058', N'Phan Thị Thanh Thủy',      '1994-02-01', false, '0901234058', '079094123058', 'thanhthuy94@gmail.com',        '73 Phạm Văn Chiêu, Q.Gò Vấp, TP.HCM', NULL, NOW(), NOW()),
('BN000059', N'Huỳnh Nhật Minh',          '1976-07-14', true,  '0901234059', '079076123059', 'nhatminh76@gmail.com',         '109 Quang Trung, Q.Gò Vấp, TP.HCM',   'Loãng xương', NOW(), NOW()),
('BN000060', N'Mai Thị Diệu Linh',        '1998-12-28', false, '0901234060', '079098123060', 'dieulinh98@gmail.com',         '40 Lê Lợi, Q.Gò Vấp, TP.HCM',         NULL, NOW(), NOW()),

-- 61-70
('BN000061', N'Đỗ Hoàng Phúc',            '1983-05-17', true,  '0901234061', '079083123061', 'hoangphuc83@gmail.com',        '195 Tân Sơn Nhì, Q.Tân Phú, TP.HCM', NULL, NOW(), NOW()),
('BN000062', N'Lý Thị Hải Yến',           '1990-10-03', false, '0901234062', '079090123062', 'haiyen90@gmail.com',           '57 Gò Dầu, Q.Tân Phú, TP.HCM',        NULL, NOW(), NOW()),
('BN000063', N'Tạ Đình Phong',            '1987-03-19', true,  '0901234063', '079087123063', 'dinhphong87@gmail.com',        '124 Lũy Bán Bích, Q.Tân Phú, TP.HCM', 'Viêm xoang mãn tính', NOW(), NOW()),
('BN000064', N'Vũ Thị Ngọc Ánh',          '1996-08-06', false, '0901234064', '079096123064', 'ngocanh96@gmail.com',          '82 Hòa Bình, Q.Tân Phú, TP.HCM',      NULL, NOW(), NOW()),
('BN000065', N'Châu Trọng Đại',           '1979-01-21', true,  '0901234065', '079079123065', 'trongdai79@gmail.com',         '16 Độc Lập, Q.Tân Phú, TP.HCM',       NULL, NOW(), NOW()),
('BN000066', N'Đinh Thị Vân Anh',         '1993-06-14', false, '0901234066', '079093123066', 'vananh93@gmail.com',           '148 Thoại Ngọc Hầu, Q.Tân Phú',       NULL, NOW(), NOW()),
('BN000067', N'Lương Bá Đạt',             '1984-11-07', true,  '0901234067', '079084123067', 'badat84@gmail.com',            '63 Âu Cơ, Q.Tân Phú, TP.HCM',         NULL, NOW(), NOW()),
('BN000068', N'Tô Thị Kim Ngân',          '1991-04-23', false, '0901234068', '079091123068', 'kimngan91@gmail.com',          '97 Lê Trọng Tấn, Q.Tân Phú, TP.HCM', NULL, NOW(), NOW()),
('BN000069', N'Hồ Thanh Bình',            '1986-09-16', true,  '0901234069', '079086123069', 'thanhbinh86@gmail.com',        '31 Bờ Bao Tân Thắng, Q.Tân Phú',      'Dị ứng latex (găng tay cao su)', NOW(), NOW()),
('BN000070', N'Nguyễn Thị Thùy Dương',    '1997-02-09', false, '0901234070', '079097123070', 'thuyduong97@gmail.com',        '170 Tân Kỳ Tân Quý, Q.Tân Phú',       NULL, NOW(), NOW()),

-- 71-80
('BN000071', N'Trần Quốc Cường',          '1980-07-25', true,  '0901234071', '079080123071', 'quoccuong80@gmail.com',        '44 Nguyễn Sơn, Q.Tân Phú, TP.HCM',   NULL, NOW(), NOW()),
('BN000072', N'Phạm Thị Bích Phượng',     '1988-12-18', false, '0901234072', '079088123072', 'bichphuong88@gmail.com',       '106 Hương Lộ 2, Q.Bình Tân, TP.HCM', NULL, NOW(), NOW()),
('BN000073', N'Lê Anh Kiệt',              '1995-05-04', true,  '0901234073', '079095123073', 'anhkiet95@gmail.com',          '59 Kinh Dương Vương, Q.Bình Tân',     NULL, NOW(), NOW()),
('BN000074', N'Võ Thị Tường Vi',          '1982-10-29', false, '0901234074', '079082123074', 'tuongvi82@gmail.com',          '133 Tên Lửa, Q.Bình Tân, TP.HCM',    'Đang điều trị tủy răng', NOW(), NOW()),
('BN000075', N'Đặng Nhật Hào',            '1990-03-12', true,  '0901234075', '079090123075', 'nhathao90@gmail.com',          '75 Lê Văn Quới, Q.Bình Tân, TP.HCM', NULL, NOW(), NOW()),
('BN000076', N'Bùi Thị Ngọc Diệp',       '1987-08-05', false, '0901234076', '079087123076', 'ngocdiep87@gmail.com',         '18 An Dương Vương, Q.Bình Tân',       NULL, NOW(), NOW()),
('BN000077', N'Hoàng Tuấn Kiệt',          '1994-01-28', true,  '0901234077', '079094123077', 'tuankiet94@gmail.com',         '146 Mã Lò, Q.Bình Tân, TP.HCM',      NULL, NOW(), NOW()),
('BN000078', N'Ngô Thị Thanh Ngân',       '1985-06-11', false, '0901234078', '079085123078', 'thanhngan85@gmail.com',        '89 Bình Long, Q.Bình Tân, TP.HCM',   NULL, NOW(), NOW()),
('BN000079', N'Dương Văn Lâm',            '1978-11-24', true,  '0901234079', '079078123079', 'vanlam78@gmail.com',           '210 Lê Cơ, Q.Bình Tân, TP.HCM',      'Bệnh thận mãn tính', NOW(), NOW()),
('BN000080', N'Trịnh Thị Kiều Trang',     '1999-04-07', false, '0901234080', '079099123080', 'kieutrang99@gmail.com',        '52 Gò Xoài, Q.Bình Tân, TP.HCM',    NULL, NOW(), NOW()),

-- 81-90
('BN000081', N'Phan Quốc Thịnh',          '1981-09-20', true,  '0901234081', '079081123081', 'quocthinh81@gmail.com',        '127 Nguyễn Ảnh Thủ, Q.12, TP.HCM',   NULL, NOW(), NOW()),
('BN000082', N'Huỳnh Thị Cẩm Nhung',     '1996-02-13', false, '0901234082', '079096123082', 'camnhung96@gmail.com',         '68 Tô Ký, Q.12, TP.HCM',             NULL, NOW(), NOW()),
('BN000083', N'Mai Thiện Toàn',            '1983-07-06', true,  '0901234083', '079083123083', 'thientoan83@gmail.com',        '191 Lê Thị Riêng, Q.12, TP.HCM',     'Răng khôn mọc lệch', NOW(), NOW()),
('BN000084', N'Đỗ Thị Minh Nguyệt',      '1990-12-30', false, '0901234084', '079090123084', 'minhnguyet90@gmail.com',       '43 Hiệp Thành, Q.12, TP.HCM',        NULL, NOW(), NOW()),
('BN000085', N'Lý Hùng Dũng',             '1977-05-15', true,  '0901234085', '079077123085', 'hungdung77@gmail.com',         '105 Thạnh Xuân, Q.12, TP.HCM',       NULL, NOW(), NOW()),
('BN000086', N'Tạ Thị Phương Uyên',       '1992-10-08', false, '0901234086', '079092123086', 'phuonguyen92@gmail.com',       '36 An Phú Đông, Q.12, TP.HCM',       NULL, NOW(), NOW()),
('BN000087', N'Vũ Đắc Hưng',             '1988-03-21', true,  '0901234087', '079088123087', 'dachung88@gmail.com',          '159 Nguyễn Văn Quá, Q.12, TP.HCM',   'Dị ứng ibuprofen', NOW(), NOW()),
('BN000088', N'Châu Thị Thúy Hằng',      '1995-08-14', false, '0901234088', '079095123088', 'thuyhang95@gmail.com',         '72 Hà Huy Giáp, Q.12, TP.HCM',       NULL, NOW(), NOW()),
('BN000089', N'Đinh Trung Kiên',          '1984-01-07', true,  '0901234089', '079084123089', 'trungkien84@gmail.com',        '14 Lê Văn Khương, Q.12, TP.HCM',     NULL, NOW(), NOW()),
('BN000090', N'Lương Thị Hoài Thương',    '1991-06-23', false, '0901234090', '079091123090', 'hoaithuong91@gmail.com',       '188 Quốc Lộ 1A, Q.12, TP.HCM',       NULL, NOW(), NOW()),

-- 91-100
('BN000091', N'Tô Quang Hiếu',            '1986-11-16', true,  '0901234091', '079086123091', 'quanghieu86@gmail.com',        '55 Lê Duẩn, Q.1, TP.HCM',             NULL, NOW(), NOW()),
('BN000092', N'Hồ Thị Tố Uyên',           '1993-04-02', false, '0901234092', '079093123092', 'touyen93@gmail.com',           '121 Mạc Đĩnh Chi, Q.1, TP.HCM',      NULL, NOW(), NOW()),
('BN000093', N'Nguyễn Đức Thiện',         '1979-09-25', true,  '0901234093', '079079123093', 'ducthien79@gmail.com',         '84 Phùng Khắc Khoan, Q.1, TP.HCM',   'Viêm nha chu mãn tính', NOW(), NOW()),
('BN000094', N'Trần Thị Hồng Đào',        '1998-02-18', false, '0901234094', '079098123094', 'hongdao98@gmail.com',          '37 Thái Văn Lung, Q.1, TP.HCM',      NULL, NOW(), NOW()),
('BN000095', N'Phạm Thanh Phong',         '1982-07-04', true,  '0901234095', '079082123095', 'thanhphong82@gmail.com',       '162 Tôn Đức Thắng, Q.1, TP.HCM',     NULL, NOW(), NOW()),
('BN000096', N'Lê Thị Bảo Ngọc',          '1989-12-27', false, '0901234096', '079089123096', 'baongoc89@gmail.com',          '49 Nguyễn Bỉnh Khiêm, Q.1, TP.HCM', NULL, NOW(), NOW()),
('BN000097', N'Võ Hải Triều',             '1996-05-10', true,  '0901234097', '079096123097', 'haitrieu96@gmail.com',         '116 Đinh Công Tráng, Q.1, TP.HCM',   NULL, NOW(), NOW()),
('BN000098', N'Đặng Thị Thanh Tâm',       '1981-10-23', false, '0901234098', '079081123098', 'thanhtam81@gmail.com',         '28 Trần Cao Vân, Q.3, TP.HCM',       'Hay bị chảy máu nướu', NOW(), NOW()),
('BN000099', N'Bùi Minh Triết',           '1994-03-08', true,  '0901234099', '079094123099', 'minhtriet94@gmail.com',        '173 Nguyễn Thiện Thuật, Q.3, TP.HCM', NULL, NOW(), NOW()),
('BN000100', N'Hoàng Thị Phương Liên',    '1987-08-21', false, '0901234100', '079087123100', 'phuonglien87@gmail.com',       '95 Trần Quang Diệu, Q.3, TP.HCM',    NULL, NOW(), NOW());


