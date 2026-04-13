-- ============================================================
-- Seed data for Pages, Actions, Permissions
-- Đảm bảo đầy đủ dữ liệu cho màn hình phân quyền
-- ============================================================

-- Pages (8 loại từ PageType enum)
INSERT INTO pages (code, name) VALUES ('PATIENT', 'Bệnh nhân') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('STAFF', 'Nhân viên') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('APPOINTMENT', 'Lịch hẹn') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('MEDICINE', 'Thuốc') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('INVOICE', 'Hóa đơn') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('PRESCRIPTION', 'Đơn thuốc') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('TREATMENT', 'Điều trị') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('USER', 'Người dùng') ON CONFLICT (code) DO NOTHING;

-- Actions (5 loại từ ActionType enum)
INSERT INTO actions (code, name) VALUES ('VIEW', 'Xem') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('CREATE', 'Thêm mới') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('UPDATE', 'Cập nhật') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('DELETE', 'Xóa') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('EXPORT', 'Xuất file') ON CONFLICT (code) DO NOTHING;

-- Permissions: tạo tất cả tổ hợp page × action hợp lệ (theo PageType.allowedActions)
-- PATIENT: VIEW, CREATE, UPDATE, DELETE, EXPORT
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PATIENT' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PATIENT' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PATIENT' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PATIENT' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PATIENT' AND a.code = 'EXPORT' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- STAFF: VIEW, CREATE, UPDATE, DELETE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'STAFF' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'STAFF' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'STAFF' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'STAFF' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- APPOINTMENT: VIEW, CREATE, UPDATE, DELETE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'APPOINTMENT' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'APPOINTMENT' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'APPOINTMENT' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'APPOINTMENT' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- MEDICINE: VIEW, CREATE, UPDATE, DELETE, EXPORT
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'EXPORT' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- INVOICE: VIEW, EXPORT
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'INVOICE' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'INVOICE' AND a.code = 'EXPORT' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- PRESCRIPTION: VIEW, CREATE, UPDATE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PRESCRIPTION' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PRESCRIPTION' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PRESCRIPTION' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- TREATMENT: VIEW, CREATE, UPDATE, DELETE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- USER: VIEW, CREATE, UPDATE, DELETE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

