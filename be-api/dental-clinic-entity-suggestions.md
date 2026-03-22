# 🦷 Dental Clinic Management — Entity Design Notes

> Cập nhật: 2026-03-19  
> Dựa trên phân tích toàn bộ package `cd.beapi.entity` & `cd.beapi.enumerate`

---

## 1. Tổng quan Entity hiện có

| # | Entity | Table | Mô tả |
|---|--------|-------|-------|
| 1 | `User` | users | Tài khoản đăng nhập (username, email, password) — FK đến `Role` |
| 2 | `Role` | roles | Vai trò hệ thống (ADMIN, DENTIST, RECEPTIONIST, …) |
| 3 | `Page` | pages | Trang/chức năng trong hệ thống (kèm `allowedPermissions`) |
| 4 | `RolePagePermission` | role_page_permissions | Quyền của Role trên từng Page (VIEW, CREATE, UPDATE, DELETE, PRINT) |
| 5 | `Staff` | staffs | Nhân viên phòng khám — FK đến `User`, có `StaffType` (enum) |
| 6 | `Patient` | patients | Bệnh nhân — FK đến `User` (optional) |
| 7 | `Appointment` | appointments | Lịch hẹn khám — FK đến `Staff` (nha sĩ), `Patient`, M2M đến `Service` |
| 8 | `VisitRegistration` | visit_registrations | Phiếu tiếp nhận (check-in) — 1-1 với `Appointment` |
| 9 | `MedicalRecord` | medical_records | Bệnh án — FK đến `Patient`, `Staff`, 1-1 với `Appointment` |
| 10 | `Prescription` | prescriptions | Đơn thuốc — FK đến `Patient`, `Staff`, 1-1 với `MedicalRecord` |
| 11 | `PrescriptionItem` | prescription_items | Chi tiết đơn thuốc — FK đến `Prescription`, `Medicine` |
| 12 | `Medicine` | medicines | Danh mục thuốc |
| 13 | `Service` | services | Dịch vụ nha khoa — FK đến `ServiceCategory` |
| 14 | `ServiceCategory` | service_categories | Nhóm dịch vụ |
| 15 | `Room` | rooms | Phòng khám *(hiện đang comment @Entity — chưa kích hoạt)* |
| 16 | `Sequence` | sequences | Auto-increment code (cho mã BN, NV, …) |

### Enum hiện có

| Enum | Giá trị |
|------|---------|
| `StaffType` | DENTIST, RECEPTIONIST, NURSE, ADMIN |
| `AppointmentStatus` | PENDING, CONFIRMED, IN_PROGRESS, COMPLETE, CANCELLED, NO_SHOW |
| `VisitRegistrationStatus` | WAITING, IN_PROGRESS, DONE, CANCELLED |
| `PermissionType` | VIEW, CREATE, UPDATE, DELETE, PRINT |
| `DentistStatus` | *(trống — chưa dùng)* |

---

## 2. Về Role ADMIN trong hệ thống

**Kết luận: ADMIN vừa có thể là lập trình viên (super admin) vừa là quản lý tổng phòng khám.**

Cách phân biệt phổ biến:

| Cách | Mô tả |
|------|-------|
| **Cách 1 — Tách Role** | Tạo 2 role: `SUPER_ADMIN` (dev/system) và `CLINIC_MANAGER` (quản lý phòng khám). SUPER_ADMIN full quyền, CLINIC_MANAGER chỉ quản lý nghiệp vụ. |
| **Cách 2 — Dùng chung ADMIN** ✅ | Giữ nguyên 1 role ADMIN, phân quyền chi tiết qua `RolePagePermission`. Dev khi seed data sẽ grant full quyền, quản lý phòng khám cũng dùng role ADMIN nhưng có thể bị hạn chế 1 số page nhạy cảm (quản lý hệ thống, logs, …). |

> **Khuyến nghị:** Với hệ thống hiện tại (đã có `RolePagePermission` phân quyền chi tiết theo page),
> nên giữ **ADMIN = quản lý phòng khám**, và seed 1 tài khoản `SUPER_ADMIN` riêng nếu cần.
> Hoặc đơn giản chỉ cần 1 role ADMIN — ai là dev thì tự biết quyền gì 😄

---

## 3. StaffType — Nên lưu dạng nào?

### So sánh 3 cách

| Tiêu chí | `Enum` (hiện tại) | FK → `StaffPosition` table | `String` |
|----------|-------------------|---------------------------|----------|
| **Select trên form** | ✅ FE gọi API lấy list enum | ✅ FE gọi API lấy từ DB | ⚠️ Phải hardcode hoặc lưu đâu đó |
| **Thêm chức vụ mới** | ❌ Phải sửa code + redeploy | ✅ Thêm record trong DB, không cần redeploy | ⚠️ Tự do nhưng dễ sai chính tả |
| **Type-safe** | ✅ Compile-time check | ⚠️ Runtime check | ❌ Không check được |
| **Query hiệu năng** | ✅ Nhanh (column trực tiếp) | ✅ Tốt (FK index) | ✅ Ok |
| **I18n / Hiển thị tên** | ⚠️ FE tự map label | ✅ DB lưu luôn label | ⚠️ Phải xử lý riêng |

### Khuyến nghị: **Giữ Enum** ✅

Lý do:
- Hệ thống phòng khám nha khoa có **số lượng chức vụ ít và ổn định** (Nha sĩ, Y tá, Lễ tân, Quản lý).
- Việc thêm chức vụ mới rất hiếm khi xảy ra → enum là đủ.
- FE chỉ cần gọi 1 API endpoint kiểu `GET /api/staff-types` trả về list enum → render `<select>`.

**API hỗ trợ cho thẻ Select:**
```java
// StaffTypeController.java
@GetMapping("/api/staff-types")
public List<StaffTypeResponse> getStaffTypes() {
    return Arrays.stream(StaffType.values())
            .map(e -> new StaffTypeResponse(e.name(), e.getLabel()))
            .toList();
}
```

**Nếu muốn có label hiển thị, thêm field vào enum:**
```java
public enum StaffType {
    DENTIST("Nha sĩ"),
    RECEPTIONIST("Lễ tân"),
    NURSE("Y tá"),
    ADMIN("Quản lý");

    private final String label;

    StaffType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
```

---

## 4. Các lỗi logic / quan hệ phát hiện được

### ❌ 4.1. `VisitRegistration` — Thiếu rất nhiều field

**Hiện tại:**
```java
public class VisitRegistration {
    String code;
    Appointment appointment; // @OneToOne
}
```

**Vấn đề:**
- Không có `status` (WAITING, IN_PROGRESS, DONE) → `VisitRegistrationStatus` enum đã tạo nhưng **chưa dùng**.
- Không có `deletedAt` nhưng lại khai báo `@SQLRestriction("deleted_at is null")` → **lỗi runtime** nếu DB chưa có cột `deleted_at`.
- Không có `createdAt`, `modifiedAt` → không biết khi nào bệnh nhân check-in.
- Không có FK đến `Patient` hay `Staff` (ai tiếp nhận).

**Đề xuất:**
```java
public class VisitRegistration {
    String code;

    @Enumerated(EnumType.STRING)
    VisitRegistrationStatus status;    // ← THÊM

    Integer queueNumber;               // ← Số thứ tự chờ khám (optional)
    String note;

    Instant deletedAt;                 // ← THÊM (match @SQLRestriction)

    @CreatedDate
    Instant createdAt;                 // ← THÊM

    @LastModifiedDate
    Instant modifiedAt;                // ← THÊM

    @OneToOne
    Appointment appointment;

    @ManyToOne
    Staff receptionist;                // ← Ai tiếp nhận (optional)
}
```

---

### ⚠️ 4.2. `Appointment` → `VisitRegistration` — mappedBy nhưng VisitRegistration không có `@SQLDelete`

- `Appointment` có `@OneToOne(mappedBy = "appointment") VisitRegistration visitRegistration`.
- Nhưng `VisitRegistration` dùng `@SQLRestriction("deleted_at is null")` mà **không có `@SQLDelete`** → khi gọi `delete()`, Hibernate sẽ hard-delete thay vì soft-delete.

**Fix:** Thêm `@SQLDelete` cho `VisitRegistration`:
```java
@SQLDelete(sql = "UPDATE visit_registrations SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
```

---

### ⚠️ 4.3. `Appointment` — Thiếu `deletedAt` field

- Có `@SQLRestriction("deleted_at is null")` nhưng **không khai báo `@SQLDelete`** và **không có field `deletedAt`**.
- → Không thể soft-delete appointment.

**Fix:** Thêm vào `Appointment`:
```java
@SQLDelete(sql = "UPDATE appointments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
// ...
Instant deletedAt;
```

---

### ⚠️ 4.4. `MedicalRecord` & `Prescription` — Dư thừa FK `patient_id` & `dentist_id`

**Chuỗi quan hệ hiện tại:**
```
Appointment → (patient_id, staff_id)
MedicalRecord → (patient_id, dentist_id, appointment_id)  
Prescription → (patient_id, dentist_id, medical_record_id)
```

**Vấn đề:**
- `MedicalRecord` đã có FK đến `Appointment`, mà `Appointment` đã chứa `patient_id` + `staff_id`.
  → `MedicalRecord.patient` và `MedicalRecord.staff` là **dư thừa** (denormalized).
- Tương tự, `Prescription` có FK đến `MedicalRecord`, mà `MedicalRecord` đã chứa `patient` + `staff`.
  → `Prescription.patient` và `Prescription.dentist` cũng **dư thừa**.

**2 hướng xử lý:**

| Hướng | Mô tả |
|-------|-------|
| **Giữ nguyên (denormalized)** ✅ | Chấp nhận dư thừa để query nhanh hơn, tránh phải join nhiều bảng. Nhưng **phải đảm bảo consistency** khi tạo record. |
| **Chuẩn hóa (normalized)** | Bỏ `patient` + `staff/dentist` khỏi `MedicalRecord` & `Prescription`, truy vấn qua `appointment`. Code sạch hơn nhưng query phức tạp hơn. |

> **Khuyến nghị:** Giữ denormalized (như hiện tại) là OK cho hệ thống phòng khám — query báo cáo, lịch sử bệnh nhân sẽ nhanh hơn nhiều. Nhưng nhớ validate consistency khi tạo MedicalRecord/Prescription.

---

### ⚠️ 4.5. `PrescriptionItem` — Thiếu `quantity`

```java
public class PrescriptionItem {
    Prescription prescription;
    Medicine medicine;
    String instruction;
    // ← Thiếu: Integer quantity (số lượng thuốc)
    // ← Thiếu: String dosage (liều dùng, vd: "2 viên/ngày")
}
```

---

### ⚠️ 4.6. `Service.createdDate` vs các entity khác dùng `createdAt` — Không nhất quán

| Entity | Field audit |
|--------|-----------|
| User | `createdAt`, `modifiedAt` |
| Staff | `createdAt`, `modifiedAt` |
| Patient | `createdAt`, `modifiedAt` |
| **Service** | **`createdDate`**, `modifiedAt` ← khác |
| **MedicalRecord** | **`createdDate`**, `modifiedAt` ← khác |
| **Prescription** | **`createdDate`**, `modifiedAt` ← khác |

> **Khuyến nghị:** Thống nhất tên thành `createdAt` cho tất cả entity.

---

### ⚠️ 4.7. `Invoice`, `InvoiceItem`, `Payment` — Chưa triển khai

Cả 3 entity đều là class rỗng. Gợi ý thiết kế:

```java
// Invoice — Hóa đơn
@Entity
public class Invoice extends BaseEntity {
    String code;                       // "INV-000001"
    
    @ManyToOne
    Patient patient;
    
    @ManyToOne
    Appointment appointment;           // Hóa đơn cho lần khám nào
    
    BigDecimal totalAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    
    String status;                     // UNPAID, PAID, PARTIALLY_PAID, CANCELLED
    
    Instant deletedAt;
    Instant createdAt;
    Instant modifiedAt;
}

// InvoiceItem — Chi tiết hóa đơn
@Entity
public class InvoiceItem extends BaseEntity {
    @ManyToOne
    Invoice invoice;
    
    @ManyToOne
    Service service;                   // Dịch vụ nào (nullable nếu item là thuốc)
    
    @ManyToOne
    Medicine medicine;                 // Thuốc nào (nullable nếu item là dịch vụ)
    
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal amount;                 // = quantity * unitPrice
    String description;
}

// Payment — Thanh toán
@Entity
public class Payment extends BaseEntity {
    String code;
    
    @ManyToOne
    Invoice invoice;
    
    BigDecimal amount;
    String paymentMethod;              // CASH, CARD, TRANSFER, MOMO, ...
    Instant paidAt;
    String note;
    
    @ManyToOne
    Staff cashier;                     // Ai thu tiền
    
    Instant createdAt;
    Instant modifiedAt;
}
```

---

### ⚠️ 4.8. `DentistStatus` enum — Trống, chưa sử dụng

Enum `DentistStatus` rỗng. Nếu dùng để quản lý trạng thái nha sĩ (online/offline, rảnh/bận):

```java
public enum DentistStatus {
    AVAILABLE,      // Sẵn sàng khám
    IN_SESSION,     // Đang khám bệnh nhân
    ON_BREAK,       // Nghỉ giữa ca
    OFF_DUTY        // Hết ca / nghỉ
}
```

> Nếu không dùng → nên xóa để code gọn.

---

## 5. Sơ đồ quan hệ tổng quan (Text-based ERD)

```
┌──────────┐     ┌──────────────────┐     ┌───────┐
│   Role   │────<│ RolePagePermission│>────│  Page │
└──────────┘     └──────────────────┘     └───────┘
     │
     │ 1:N
     ▼
┌──────────┐ 1:1 ┌──────────┐          ┌─────────────────┐
│   User   │─────│  Staff   │          │ ServiceCategory  │
└──────────┘     └──────────┘          └─────────────────┘
     │                │                        │ 1:N
     │ 1:1            │ 1:N                    ▼
     ▼                │                 ┌──────────┐
┌──────────┐          │                 │ Service  │
│ Patient  │          │                 └──────────┘
└──────────┘          │                      │ M:N
     │                │                      │
     │ 1:N            │ 1:N                  │
     ▼                ▼                      ▼
┌────────────────────────────────────────────────┐
│                  Appointment                    │
│  (patient_id, staff_id, M2M services)          │
└────────────────────────────────────────────────┘
        │ 1:1              │ 1:1              │ 1:1
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌─────────────┐
│VisitRegistra │  │MedicalRecord │  │   Invoice    │
│   tion       │  └──────────────┘  └─────────────┘
└──────────────┘        │ 1:1              │ 1:N
                        ▼                  ▼
                 ┌──────────────┐  ┌─────────────┐
                 │ Prescription │  │ InvoiceItem  │
                 └──────────────┘  └─────────────┘
                        │ 1:N          
                        ▼              
                ┌────────────────┐    ┌──────────┐
                │PrescriptionItem│───>│ Medicine │
                └────────────────┘    └──────────┘

                ┌──────────┐     ┌─────────────┐
                │ Payment  │────>│   Invoice    │
                └──────────┘     └─────────────┘
```

---

## 6. Gợi ý Entity bổ sung (tương lai)

| Entity | Mô tả |
|--------|-------|
| `ToothChart` | Sơ đồ răng bệnh nhân (32 răng, trạng thái từng răng) |
| `TreatmentHistory` | Lịch sử điều trị theo từng răng |
| `WorkSchedule` | Lịch làm việc nha sĩ / nhân viên |
| `TimeSlot` | Khung giờ khám có sẵn |
| `Notification` | Thông báo / nhắc lịch hẹn |
| `ClinicSetting` | Cấu hình phòng khám (giờ mở cửa, số bệnh nhân tối đa/ngày, ...) |

---

## 7. Checklist nhanh

- [ ] Fix `VisitRegistration` — thêm status, deletedAt, createdAt, @SQLDelete
- [ ] Fix `Appointment` — thêm deletedAt, @SQLDelete
- [ ] Fix `PrescriptionItem` — thêm quantity, dosage
- [ ] Thống nhất `createdDate` → `createdAt` (Service, MedicalRecord, Prescription)
- [ ] Triển khai `Invoice`, `InvoiceItem`, `Payment`
- [ ] Thêm label cho `StaffType` enum
- [ ] Xử lý `DentistStatus` (dùng hoặc xóa)
- [ ] Cân nhắc bật lại `Room` entity nếu cần quản lý phòng khám

