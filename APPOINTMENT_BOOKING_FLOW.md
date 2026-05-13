# Appointment Booking Flow

## Mục Tiêu

Flow đặt lịch cho clinic cho phép lễ tân/admin tạo lịch hẹn, phân nha sĩ, kiểm tra slot trống theo lịch làm việc, check-in bệnh nhân khi đến phòng khám, cấp số thứ tự và chuyển trạng thái khám.

## Vai Trò Chính

- **Lễ tân/admin**: tạo lịch hẹn, cập nhật lịch, xác nhận, check-in, hủy lịch, đánh dấu bệnh nhân không đến.
- **Nha sĩ**: tiếp nhận bệnh nhân đang chờ khám, bắt đầu khám và hoàn tất lịch hẹn.
- **Bệnh nhân**: được gắn vào lịch hẹn thông qua hồ sơ bệnh nhân sẵn có.

## State Machine

```text
PENDING -> CONFIRMED -> IN_QUEUE -> IN_PROGRESS -> DONE
       \             \-> CANCELLED
        \-> CANCELLED

PENDING   -> NO_SHOW
CONFIRMED -> NO_SHOW
```

Ý nghĩa trạng thái:

- `PENDING`: lịch đặt online hoặc lịch đang chờ clinic xác nhận.
- `CONFIRMED`: lịch đã được clinic xác nhận.
- `IN_QUEUE`: bệnh nhân đã đến clinic, đã check-in và được cấp số thứ tự.
- `IN_PROGRESS`: nha sĩ đang khám.
- `DONE`: khám xong.
- `CANCELLED`: lịch bị hủy.
- `NO_SHOW`: bệnh nhân không đến.

## Flow Tạo Lịch Từ FE Admin

1. Admin mở màn **Quản lý lịch hẹn**.
2. FE load danh sách bệnh nhân từ `PatientService.search`.
3. FE load danh sách nha sĩ active từ `StaffService.search` với:

```json
{
  "staffType": "DENTIST",
  "isActive": true
}
```

4. Admin chọn bệnh nhân.
5. Admin chọn nha sĩ.
6. Admin chọn ngày hẹn.
7. FE gọi API lấy slot trống:

```http
GET /api/v1/clinic/appointments/available-slots?dentistId={id}&date={yyyy-MM-dd}
```

8. Backend kiểm tra `WorkingSchedule` của nha sĩ trong ngày đó và loại các giờ đã có lịch active.
9. Admin chọn giờ hẹn.
10. Admin nhập triệu chứng/lý do khám và ghi chú.
11. FE gửi request tạo lịch:

```http
POST /api/v1/clinic/appointments
```

```json
{
  "patientId": 1,
  "dentistId": 2,
  "appointmentDate": "2026-05-12T09:00:00",
  "symptom": "Đau răng hàm",
  "note": "Ưu tiên kiểm tra răng số 6"
}
```

12. Backend tạo appointment với `status = CONFIRMED`.

## Validation Backend

Khi tạo hoặc cập nhật lịch hẹn, backend kiểm tra:

- `patientId` phải tồn tại.
- `dentistId` nếu có thì phải là staff active có `staffType = DENTIST`.
- `appointmentDate` không được null.
- Nếu có nha sĩ, giờ hẹn phải nằm trong `WorkingSchedule` của nha sĩ.
- Nếu có nha sĩ, không được trùng giờ với appointment active khác.
- Các lịch `CANCELLED` và `NO_SHOW` không được tính là lịch chiếm slot.

## Check-In Flow

1. Khi bệnh nhân đến clinic, admin bấm **Check-in**.
2. FE gọi:

```http
PATCH /api/v1/clinic/appointments/{id}/check-in
```

3. Backend chỉ cho check-in khi lịch đang ở `PENDING` hoặc `CONFIRMED`.
4. Backend cập nhật:

- `status = IN_QUEUE`
- `queueNumber` lấy từ `QueueService.nextQueueNumberForDate(LocalDate.now())`
- `snapshotPatientName`
- `snapshotPatientPhone`
- `receptionist` nếu request có `receptionistId`
- `dentist` nếu request có `dentistId`

## Flow Khám

### Bắt Đầu Khám

```http
PATCH /api/v1/clinic/appointments/{id}/start
```

Điều kiện:

- Appointment phải đang ở `IN_QUEUE`.
- Appointment phải có nha sĩ.

Kết quả:

- `status = IN_PROGRESS`

### Hoàn Tất Khám

```http
PATCH /api/v1/clinic/appointments/{id}/done
```

Điều kiện:

- Appointment phải đang ở `IN_PROGRESS`.

Kết quả:

- `status = DONE`

## API Đã Tạo

```http
GET    /api/v1/clinic/appointments/{id}
POST   /api/v1/clinic/appointments/search
POST   /api/v1/clinic/appointments
PUT    /api/v1/clinic/appointments/{id}
DELETE /api/v1/clinic/appointments/{id}

PATCH  /api/v1/clinic/appointments/{id}/confirm
PATCH  /api/v1/clinic/appointments/{id}/check-in
PATCH  /api/v1/clinic/appointments/{id}/start
PATCH  /api/v1/clinic/appointments/{id}/done
PATCH  /api/v1/clinic/appointments/{id}/cancel
PATCH  /api/v1/clinic/appointments/{id}/no-show

GET    /api/v1/clinic/appointments/available-slots?dentistId={id}&date={yyyy-MM-dd}
```

## FE Admin Mapping

Màn `fe-admin/src/app/features/appointment` đang dùng các API trên để:

- tìm kiếm lịch theo keyword và status,
- tạo/cập nhật lịch hẹn,
- load bệnh nhân và nha sĩ cho dropdown,
- load slot trống theo nha sĩ + ngày,
- chạy nhanh các action trạng thái từ bảng lịch hẹn.

Các action button trên bảng:

- `confirm`: xác nhận lịch.
- `checkIn`: check-in và cấp số thứ tự.
- `start`: bắt đầu khám.
- `done`: hoàn tất.
- `noShow`: đánh dấu không đến.
- `cancel`: hủy lịch.

## File Liên Quan

- Backend controller: `be-api/src/main/java/cd/beapi/controller/clinic/AppointmentController.java`
- Backend service: `be-api/src/main/java/cd/beapi/service/impl/AppointmentServiceImpl.java`
- Backend repository: `be-api/src/main/java/cd/beapi/repository/jpa/AppointmentRepository.java`
- Backend DTO response: `be-api/src/main/java/cd/beapi/dto/response/AppointmentResponse.java`
- FE service: `fe-admin/src/app/core/service/appointment.service.ts`
- FE screen: `fe-admin/src/app/features/appointment/appointment.ts`
- FE template: `fe-admin/src/app/features/appointment/appointment.html`
