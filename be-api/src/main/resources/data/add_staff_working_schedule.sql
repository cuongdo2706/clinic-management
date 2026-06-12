CREATE TABLE IF NOT EXISTS staff_working_schedules
(
    id          BIGSERIAL PRIMARY KEY,
    staff_id    BIGINT      NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    working     BOOLEAN     NOT NULL DEFAULT TRUE,
    start_time  TIME        NOT NULL DEFAULT '08:00',
    end_time    TIME        NOT NULL DEFAULT '20:00',
    CONSTRAINT fk_staff_working_schedules_staff
        FOREIGN KEY (staff_id) REFERENCES staffs (id),
    CONSTRAINT uk_staff_working_schedule_day
        UNIQUE (staff_id, day_of_week)
);

INSERT INTO staff_working_schedules (staff_id, day_of_week, working, start_time, end_time)
SELECT s.id, days.day_of_week, TRUE, '08:00', '20:00'
FROM staffs s
         CROSS JOIN (VALUES
                         ('MONDAY'),
                         ('TUESDAY'),
                         ('WEDNESDAY'),
                         ('THURSDAY'),
                         ('FRIDAY'),
                         ('SATURDAY'),
                         ('SUNDAY')) AS days(day_of_week)
WHERE NOT EXISTS (SELECT 1
                  FROM staff_working_schedules schedules
                  WHERE schedules.staff_id = s.id
                    AND schedules.day_of_week = days.day_of_week);
