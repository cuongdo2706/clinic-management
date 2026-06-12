-- ============================================================
-- Seed data for Pages, Actions, Permissions
-- Đảm bảo đầy đủ dữ liệu cho màn hình phân quyền
-- ============================================================

INSERT INTO roles (code, name, description)
VALUES ('PATIENT', 'Khách hàng', 'Tài khoản đăng nhập website khách hàng')
ON CONFLICT (code) DO NOTHING;

-- Pages từ PageType enum
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT pe.id
    FROM permissions pe
    JOIN pages p ON p.id = pe.page_id
    WHERE p.code IN ('INVOICE', 'PRESCRIPTION', 'USER')
);
DELETE FROM permissions
WHERE page_id IN (SELECT id FROM pages WHERE code IN ('INVOICE', 'PRESCRIPTION', 'USER'));
DELETE FROM pages WHERE code IN ('INVOICE', 'PRESCRIPTION', 'USER');

ALTER TABLE pages DROP CONSTRAINT IF EXISTS pages_code_check;
ALTER TABLE pages ADD CONSTRAINT pages_code_check
    CHECK ((code)::text = ANY (ARRAY[
        'DASHBOARD'::text,
        'PATIENT'::text,
        'STAFF'::text,
        'APPOINTMENT'::text,
        'EXAMINATION'::text,
        'MEDICINE'::text,
        'TREATMENT'::text,
        'PROCEDURE'::text
    ]));

INSERT INTO pages (code, name) VALUES ('DASHBOARD', 'Tổng quan') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('PATIENT', 'Bệnh nhân') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('STAFF', 'Nhân viên') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('APPOINTMENT', 'Lịch hẹn') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('EXAMINATION', 'Khám bệnh') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('MEDICINE', 'Thuốc') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('TREATMENT', 'Điều trị') ON CONFLICT (code) DO NOTHING;
INSERT INTO pages (code, name) VALUES ('PROCEDURE', 'Thủ thuật') ON CONFLICT (code) DO NOTHING;

-- Actions (5 loại từ ActionType enum)
INSERT INTO actions (code, name) VALUES ('VIEW', 'Xem') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('CREATE', 'Thêm mới') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('UPDATE', 'Cập nhật') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('DELETE', 'Xóa') ON CONFLICT (code) DO NOTHING;
INSERT INTO actions (code, name) VALUES ('EXPORT', 'Xuất file') ON CONFLICT (code) DO NOTHING;

-- Permissions: tạo tất cả tổ hợp page × action hợp lệ (theo PageType.allowedActions)
-- DASHBOARD: VIEW
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'DASHBOARD' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

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

-- TREATMENT: VIEW, CREATE, UPDATE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'TREATMENT' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- PROCEDURE: VIEW, CREATE, UPDATE, DELETE
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'VIEW' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'CREATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'UPDATE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;
INSERT INTO permissions (page_id, action_id) SELECT p.id, a.id FROM pages p, actions a WHERE p.code = 'PROCEDURE' AND a.code = 'DELETE' ON CONFLICT ON CONSTRAINT uk_page_action DO NOTHING;

-- Default quyền cơ bản cho các vai trò hệ thống.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, pe.id
FROM roles r
JOIN permissions pe ON TRUE
JOIN pages p ON p.id = pe.page_id
JOIN actions a ON a.id = pe.action_id
WHERE (
        (
            r.code = 'MANAGER'
            AND (
                p.code = 'DASHBOARD'
                OR (p.code IN ('PATIENT', 'STAFF', 'APPOINTMENT', 'MEDICINE', 'PROCEDURE') AND a.code IN ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'EXPORT'))
                OR (p.code = 'TREATMENT' AND a.code IN ('VIEW', 'CREATE', 'UPDATE'))
            )
        )
        OR (
            r.code = 'RECEPTIONIST'
            AND (
                p.code = 'DASHBOARD'
                OR (p.code = 'PATIENT' AND a.code IN ('VIEW', 'CREATE', 'UPDATE', 'EXPORT'))
                OR (p.code = 'APPOINTMENT' AND a.code IN ('VIEW', 'CREATE', 'UPDATE', 'DELETE'))
                OR (p.code IN ('STAFF', 'PROCEDURE') AND a.code = 'VIEW')
            )
        )
        OR (
            r.code = 'DENTIST'
            AND (
                p.code = 'DASHBOARD'
                OR (p.code = 'APPOINTMENT' AND a.code = 'VIEW')
                OR (p.code = 'EXAMINATION' AND a.code IN ('VIEW', 'UPDATE'))
                OR (p.code IN ('PATIENT', 'PROCEDURE', 'MEDICINE') AND a.code = 'VIEW')
                OR (p.code = 'TREATMENT' AND a.code IN ('VIEW', 'CREATE', 'UPDATE'))
            )
        )
    )
    AND NOT EXISTS (
        SELECT 1
        FROM role_permissions rp
        WHERE rp.role_id = r.id
          AND rp.permission_id = pe.id
    );

