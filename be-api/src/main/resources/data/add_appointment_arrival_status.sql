BEGIN;

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS arrival_status varchar(255) NOT NULL DEFAULT 'NOT_ARRIVED';

UPDATE appointments
SET arrival_status = 'NOT_ARRIVED'
WHERE arrival_status IS NULL;

COMMIT;
