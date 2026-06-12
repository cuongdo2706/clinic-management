BEGIN;

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

DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS invoice_items CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;

DELETE FROM sequences WHERE name IN ('INVOICE', 'PAYMENT');

COMMIT;
