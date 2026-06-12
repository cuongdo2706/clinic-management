UPDATE treatments t
SET status = 'COMPLETED',
    modified_at = CURRENT_TIMESTAMP
FROM appointments a
WHERE t.appointment_id = a.id
  AND a.status = 'COMPLETED'
  AND t.status = 'IN_PROGRESS'
  AND t.deleted_at IS NULL
  AND a.deleted_at IS NULL;
