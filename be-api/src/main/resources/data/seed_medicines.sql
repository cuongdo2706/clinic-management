INSERT INTO medicines (code, name, unit, description, is_active, deleted_at, created_at, modified_at)
VALUES
-- =========================================================
-- THUỐC GÂY TÊ / GIẢM ĐAU
-- =========================================================
('TH000001', 'Lidocaine 2%',               'Ống',   'Thuốc gây tê cục bộ, dùng trong phẫu thuật nha khoa và tiêm tê',                                     true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000002', 'Articaine 4% + Epinephrine', 'Ống',   'Thuốc gây tê mạnh, kết hợp epinephrine để kéo dài tác dụng, dùng trong nhổ răng và tiểu phẫu',      true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000003', 'Ibuprofen 400mg',            'Viên',  'Thuốc giảm đau, kháng viêm không steroid (NSAIDs), dùng sau điều trị nha khoa',                       true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000004', 'Paracetamol 500mg',          'Viên',  'Thuốc giảm đau, hạ sốt, dùng cho bệnh nhân không dùng được NSAIDs',                                  true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000005', 'Diclofenac 50mg',            'Viên',  'Thuốc kháng viêm, giảm đau sau nhổ răng hoặc phẫu thuật implant',                                     true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000006', 'Ketorolac 30mg/ml',          'Ống',   'Thuốc giảm đau mạnh dạng tiêm, dùng trong trường hợp đau cấp tính sau phẫu thuật',                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- KHÁNG SINH
-- =========================================================
('TH000007', 'Amoxicillin 500mg',          'Viên',  'Kháng sinh nhóm penicillin, dùng điều trị nhiễm khuẩn răng miệng phổ biến',                          true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000008', 'Amoxicillin + Clavulanate',  'Viên',  'Kháng sinh phổ rộng, hiệu quả với vi khuẩn kháng penicillin, dùng trong áp xe răng',                 true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000009', 'Metronidazole 250mg',        'Viên',  'Kháng sinh kháng khuẩn kỵ khí, thường kết hợp với Amoxicillin trong viêm nha chu',                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000010', 'Clindamycin 300mg',          'Viên',  'Kháng sinh thay thế cho bệnh nhân dị ứng penicillin, phổ tác dụng tốt trên vi khuẩn miệng',          true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000011', 'Spiramycin 1.5M IU',        'Viên',  'Kháng sinh nhóm macrolide, tập trung cao ở mô xương hàm, dùng trong nhiễm khuẩn nha chu',             true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000012', 'Doxycycline 100mg',          'Viên',  'Kháng sinh nhóm tetracycline, dùng trong điều trị viêm nha chu mãn tính và hỗ trợ cạo vôi',          true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- KHÁNG VIÊM / CORTICOSTEROID
-- =========================================================
('TH000013', 'Dexamethasone 0.5mg',        'Viên',  'Corticosteroid giảm phù nề sau phẫu thuật implant hoặc nhổ răng khôn',                                true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000014', 'Methylprednisolone 4mg',     'Viên',  'Corticosteroid dạng uống, kiểm soát viêm và sưng sau phẫu thuật nha khoa lớn',                       true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000015', 'Prednisolone 5mg',           'Viên',  'Corticosteroid điều trị viêm loét miệng aphthous nặng và lichen planus miệng',                        true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- SÚC MIỆNG / SÁT KHUẨN
-- =========================================================
('TH000016', 'Chlorhexidine 0.12%',        'Chai',  'Dung dịch súc miệng kháng khuẩn, giảm mảng bám, dùng sau phẫu thuật và điều trị nha chu',            true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000017', 'Povidone Iodine 1%',         'Chai',  'Dung dịch sát khuẩn dùng để rửa vết thương miệng, áp xe và ổ nhổ răng',                              true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000018', 'Hydrogen Peroxide 3%',       'Chai',  'Dung dịch oxy già, dùng làm sạch ổ nhổ răng và khử mùi hôi miệng do nhiễm khuẩn',                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000019', 'Nước muối sinh lý 0.9%',     'Chai',  'Dùng bơm rửa ổ nhổ răng, làm sạch sau phẫu thuật và điều trị tủy',                                  true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- ĐIỀU TRỊ TỦY
-- =========================================================
('TH000020', 'Calcium Hydroxide',          'Hộp',   'Paste trám tạm điều trị tủy, kháng khuẩn và kích thích tạo cầu ngà',                                 true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000021', 'Formocresol',                'Lọ',    'Dung dịch điều trị tủy tạm thời, dùng trong lấy tủy buồng (pulpotomy) ở răng sữa',                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000022', 'Eugenol',                    'Lọ',    'Chất giảm đau và kháng khuẩn nguồn gốc tự nhiên, dùng trong trám tạm và ZOE',                        true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000023', 'EDTA 17%',                   'Chai',  'Dung dịch chelation dùng làm mềm và mở rộng ống tủy trong điều trị nội nha',                         true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000024', 'Sodium Hypochlorite 2.5%',   'Chai',  'Dung dịch bơm rửa ống tủy, tiêu diệt vi khuẩn kỵ khí và làm tan mô tủy hoại tử',                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- ĐIỀU TRỊ NHA CHU / LOÉT MIỆNG
-- =========================================================
('TH000025', 'Metronidazole Gel 25%',      'Ống',   'Gel kháng khuẩn bơm vào túi nha chu sau cạo vôi và xử lý mặt chân răng',                             true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000026', 'Doxycycline Gel 10%',        'Ống',   'Gel kháng sinh giải phóng chậm trong túi nha chu, hỗ trợ tái bám dính mô',                            true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000027', 'Triamcinolone Acetonide',    'Tuýp',  'Kem bôi corticosteroid điều trị loét áp-tơ, lichen planus và viêm lợi dị ứng',                       true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000028', 'Benzocaine Gel 20%',         'Tuýp',  'Gel gây tê bề mặt niêm mạc trước khi tiêm tê hoặc lấy cao răng',                                     true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- TRÁNG MEN / FLUOR
-- =========================================================
('TH000029', 'Fluor Varnish 5%',           'Lọ',    'Vecni fluor bôi mặt răng để ngừa sâu răng và giảm ê buốt sau điều trị',                              true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000030', 'Gel Fluor APF 1.23%',        'Hộp',   'Gel fluor acid phosphate dùng trong khay phủ fluor, phòng ngừa sâu răng',                             true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000031', 'Silver Diamine Fluoride',    'Lọ',    'Dung dịch bôi ngừng sâu răng không xâm lấn, đặc biệt hiệu quả ở trẻ em và người cao tuổi',           true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- CẦM MÁU / HỖ TRỢ PHẪU THUẬT
-- =========================================================
('TH000032', 'Tranexamic Acid 500mg',      'Viên',  'Thuốc cầm máu hệ thống, dùng trước phẫu thuật cho bệnh nhân rối loạn đông máu',                      true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000033', 'Adrenaline 1:100.000',       'Ống',   'Thuốc co mạch kết hợp trong dung dịch gây tê để giảm chảy máu và kéo dài tê',                        true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000034', 'Surgicel (Oxidized Cellulose)','Miếng','Vật liệu cầm máu tan sinh học, đặt vào ổ nhổ răng để kiểm soát chảy máu',                            true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000035', 'Gelfoam (Gelatin Sponge)',   'Miếng', 'Bọt gelatin cầm máu tiêu sinh học, dùng sau nhổ răng và phẫu thuật xương ổ',                          true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- VITAMIN / BỔ TRỢ
-- =========================================================
('TH000036', 'Vitamin C 500mg',            'Viên',  'Tăng cường miễn dịch và hỗ trợ lành thương sau phẫu thuật nha khoa',                                  true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000037', 'Vitamin B Complex',          'Viên',  'Bổ sung vitamin nhóm B hỗ trợ phục hồi niêm mạc và giảm loét miệng tái phát',                        true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000038', 'Zinc Gluconate 70mg',        'Viên',  'Khoáng chất hỗ trợ lành thương và tăng cường miễn dịch tại chỗ',                                     true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- =========================================================
-- DỰ PHÒNG / KHÁC
-- =========================================================
('TH000039', 'Nystatin Suspension',        'Chai',  'Thuốc kháng nấm dạng lỏng, điều trị nấm Candida miệng (tưa miệng)',                                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000040', 'Fluconazole 150mg',          'Viên',  'Thuốc kháng nấm hệ thống, dùng cho nhiễm Candida miệng mức độ trung bình đến nặng',                  true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000041', 'Acyclovir 200mg',            'Viên',  'Thuốc kháng virus điều trị herpes labialis và stomatitis herpetic tái phát',                          true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000042', 'Antihistamine Cetirizine',   'Viên',  'Thuốc kháng histamine điều trị phù nề dị ứng sau vật liệu nha khoa hoặc thuốc tê',                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000043', 'Epinephrine 1mg/ml',         'Ống',   'Thuốc cấp cứu sốc phản vệ, bắt buộc có trong phòng khám nha khoa',                                   true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TH000044', 'Omeprazole 20mg',            'Viên',  'Thuốc bảo vệ dạ dày, dùng kèm kháng sinh và NSAIDs để tránh kích ứng tiêu hóa',                      true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

