-- ============================================================
-- Seed data for Pages, Actions, Permissions
-- Đảm bảo đầy đủ dữ liệu cho màn hình phân quyền
-- ============================================================

INSERT INTO roles (code, name, description)
VALUES ('PATIENT', 'Khách hàng', 'Tài khoản đăng nhập website khách hàng')
ON CONFLICT (code) DO NOTHING;

-- Pages (8 loại từ PageType enum)
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT pe.id
    FROM permissions pe
    JOIN pages p ON p.id = pe.page_id
    WHERE p.code = 'INVOICE'
);
DELETE FROM permissions
WHERE page_id IN (SELECT id FROM pages WHERE code = 'INVOICE');
DELETE FROM pages WHERE code = 'INVOICE';

ALTER TABLE pages DROP CONSTRAINT IF EXISTS pages_code_check;
ALTER TABLE pages ADD CONSTRAINT pages_code_check
    CHECK ((code)::text = ANY (ARRAY[
        'PATIENT'::text,
        'STAFF'::text,
        'APPOINTMENT'::text,
        'EXAMINATION'::text,
        'MEDICINE'::text,
        'PRESCRIPTION'::text,
        'TREATMENT'::text,
        'PROCEDURE'::text,
        'USER'::text
    ]));

INSERT INTO pages (code, name) VALUES ('PATIENT', 'Bệnh nhân') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('STAFF', 'Nhân viên') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('APPOINTMENT', 'Lịch hẹn') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('EXAMINATION', 'Khám bệnh') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('MEDICINE', 'Thuốc') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('PRESCRIPTION', 'Đơn thuốc') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('TREATMENT', 'Điều trị') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('PROCEDURE', 'Thủ thuật') ON CONFLICT (code) DO NOTHING;
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

-- EXAMINATION: VIEW, UPDATE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'EXAMINATION' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'EXAMINATION' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- MEDICINE: VIEW, CREATE, UPDATE, DELETE, EXPORT
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'MEDICINE' AND a.code = 'EXPORT' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- PRESCRIPTION: VIEW, CREATE, UPDATE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PRESCRIPTION' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PRESCRIPTION' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PRESCRIPTION' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- TREATMENT: VIEW, CREATE, UPDATE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- PROCEDURE: VIEW, CREATE, UPDATE, DELETE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- USER: VIEW, CREATE, UPDATE, DELETE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'USER' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

