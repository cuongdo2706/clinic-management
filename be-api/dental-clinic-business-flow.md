# 🦷 Luồng Nghiệp Vụ Chính — Phòng Khám Nha Khoa

> Dựa trên entity thực tế trong project `be-api`

---

## Tổng quan: 6 bước chính

```
 ①              ②               ③              ④              ⑤              ⑥
ĐẶT LỊCH  →  TIẾP NHẬN  →  KHÁM BỆNH  →  KÊ ĐƠN THUỐC  →  TẠO HÓA ĐƠN  →  THANH TOÁN
Appointment   VisitReg.     MedicalRecord   Prescription     Invoice         Payment
 PENDING  →   WAITING   →   IN_PROGRESS →                 →   UNPAID    →    PAID
```

---

## ① Đặt lịch hẹn (Appointment)

**Ai thực hiện:** Bệnh nhân (đặt online) hoặc Lễ tân (đặt hộ qua điện thoại/trực tiếp)

### Trường hợp A — Bệnh nhân đã đăng ký (Online)
```
BN đăng nhập → Đặt lịch online
  → Hệ thống đã có Patient (isWalkIn = false, user ≠ null)
  → Chọn ngày, giờ, nha sĩ, ghi chú lý do
  → Tạo Appointment (status: PENDING)
```

### Trường hợp B — Bệnh nhân walk-in (Tại quầy)
```
BN đến trực tiếp → Lễ tân kiểm tra SĐT/Tên:
  ├── Đã có trong hệ thống → Dùng Patient cũ
  └── Chưa có → Tạo nhanh Patient mới:
        • fullName + phone (bắt buộc)
        • dob, gender, address... (tùy chọn, điền sau)
        • isWalkIn = true, user = null
  → Tạo Appointment (status: CONFIRMED — không cần chờ xác nhận)
```

> 💡 **Quy tắc:** Mọi lần khám đều phải có `Patient` record.
> Walk-in chỉ khác ở chỗ: `isWalkIn = true` và `user = null` (không có tài khoản).
> Sau này BN walk-in muốn đăng ký tài khoản → tạo `User` rồi gắn vào `Patient.user`.

**Luồng:**
```
Bệnh nhân/Lễ tân chọn:
  → Ngày khám (appointmentDate)
  → Khung giờ (startTime – endTime)
  → Nha sĩ (staff_id — StaffType = DENTIST)
  → Ghi chú lý do khám (note: "Đau răng hàm dưới bên trái", ...)
```

**Kết quả:** Tạo 1 record `Appointment` với:
- `code`: Mã tự sinh (VD: `APT-000001`)
- `status`: **PENDING** (chờ xác nhận)

**Chuyển trạng thái:**
```
PENDING ──(Lễ tân/Admin xác nhận)──→ CONFIRMED
PENDING ──(Bệnh nhân hủy)─────────→ CANCELLED
CONFIRMED ──(Đến ngày, bắt đầu)───→ IN_PROGRESS
CONFIRMED ──(Không đến khám)───────→ NO_SHOW
IN_PROGRESS ──(Khám xong)─────────→ COMPLETE
```

**Entity liên quan:**
| Entity | Quan hệ |
|--------|---------|
| `Patient` | `@ManyToOne` — Bệnh nhân nào đặt |
| `Staff` | `@ManyToOne` — Nha sĩ nào khám |

---

## ② Tiếp nhận / Check-in (VisitRegistration)

**Ai thực hiện:** Lễ tân (RECEPTIONIST)

**Khi nào:** Bệnh nhân đến phòng khám đúng ngày hẹn

**Luồng:**
```
Bệnh nhân đến phòng khám
  → Lễ tân tìm Appointment (theo code hoặc tên/SĐT bệnh nhân)
  → Xác nhận bệnh nhân đã đến
  → Tạo VisitRegistration (phiếu tiếp nhận)
  → Cấp số thứ tự chờ khám (queueNumber)
```

**Kết quả:** Tạo 1 record `VisitRegistration` với:
- `code`: Mã phiếu tiếp nhận (VD: `VR-000001`)
- `status`: **WAITING** (chờ khám)
- `queueNumber`: Số thứ tự (VD: `3` — người thứ 3 trong ngày)
- `receptionist`: Lễ tân nào tiếp nhận
- Đồng thời cập nhật `Appointment.status` → **CONFIRMED** (nếu chưa) hoặc giữ nguyên

**Chuyển trạng thái:**
```
WAITING ──(Nha sĩ gọi vào)──→ IN_PROGRESS
IN_PROGRESS ──(Khám xong)───→ DONE
WAITING ──(BN bỏ về)────────→ CANCELLED
```

**Entity liên quan:**
| Entity | Quan hệ |
|--------|---------|
| `Appointment` | `@OneToOne` — Phiếu tiếp nhận cho lịch hẹn nào |
| `Staff` (receptionist) | `@ManyToOne` — Lễ tân nào tiếp nhận |

---

## ③ Khám bệnh (MedicalRecord)

**Ai thực hiện:** Nha sĩ (DENTIST)

**Khi nào:** Nha sĩ gọi bệnh nhân vào phòng khám (theo queueNumber)

**Luồng:**
```
Nha sĩ gọi bệnh nhân tiếp theo (queueNumber nhỏ nhất có status = WAITING)
  → Cập nhật VisitRegistration.status → IN_PROGRESS
  → Cập nhật Appointment.status → IN_PROGRESS
  → Nha sĩ khám và ghi nhận:
      • chiefComplaint: Lý do đến khám ("Đau răng số 36")
      • diagnosis: Chẩn đoán ("Sâu răng độ 3, viêm tủy")
      • treatmentPlan: Kế hoạch điều trị ("Điều trị tủy + trám")
      • services: Chọn dịch vụ thực tế đã thực hiện (M2M → Service)
      • notes: Ghi chú thêm
  → Lưu MedicalRecord
```

**Kết quả:** Tạo 1 record `MedicalRecord` với:
- `code`: Mã bệnh án (VD: `MR-000001`)
- FK đến `patient`, `staff` (nha sĩ), `appointment`
- M2M đến `services` (dịch vụ thực tế đã làm)

**Khi khám xong:**
```
→ Cập nhật Appointment.status → COMPLETE
→ Cập nhật VisitRegistration.status → DONE
```

**Entity liên quan:**
| Entity | Quan hệ |
|--------|---------|
| `Patient` | `@ManyToOne` — Bệnh án của ai |
| `Staff` (dentist) | `@ManyToOne` — Nha sĩ nào khám |
| `Appointment` | `@OneToOne` — Bệnh án cho lần khám nào |
| `Service` | `@ManyToMany` — Dịch vụ thực tế đã thực hiện |

---

## ④ Kê đơn thuốc (Prescription + PrescriptionItem)

**Ai thực hiện:** Nha sĩ (DENTIST)

**Khi nào:** Sau khi khám xong, nha sĩ kê đơn (nếu cần)

> ⚠️ **Không phải lần khám nào cũng có đơn thuốc** — VD: tẩy trắng răng thì không cần.

**Luồng:**
```
Nha sĩ tạo đơn thuốc từ MedicalRecord vừa tạo:
  → Chọn thuốc từ danh mục (Medicine)
  → Nhập số lượng (quantity), liều dùng (dosage), hướng dẫn (instruction)
  → Mỗi dòng thuốc = 1 PrescriptionItem
```

**Ví dụ thực tế:**
```
Đơn thuốc: PRE-000001
├── Amoxicillin 500mg  | SL: 21 | Liều: 3 viên/ngày | HD: Uống sau ăn
├── Paracetamol 500mg  | SL: 10 | Liều: 2 viên/ngày | HD: Khi đau
└── Metronidazol 250mg | SL: 14 | Liều: 2 viên/ngày | HD: Uống sau ăn
```

**Entity liên quan:**
| Entity | Quan hệ |
|--------|---------|
| `MedicalRecord` | `@OneToOne` — Đơn thuốc thuộc bệnh án nào |
| `Patient` | `@ManyToOne` — Đơn thuốc cho ai |
| `Staff` (dentist) | `@ManyToOne` — Nha sĩ nào kê |
| `PrescriptionItem` → `Medicine` | `@ManyToOne` — Chi tiết từng loại thuốc |

---

## ⑤ Tạo hóa đơn (Invoice + InvoiceItem)

**Ai thực hiện:** Lễ tân hoặc Hệ thống (tự động tạo khi khám xong)

**Khi nào:** Sau khi nha sĩ hoàn tất khám + kê đơn

**Luồng:**
```
Hệ thống/Lễ tân tạo hóa đơn:
  → Lấy danh sách dịch vụ từ MedicalRecord.services → tạo InvoiceItem (dịch vụ)
  → Lấy danh sách thuốc từ PrescriptionItem → tạo InvoiceItem (thuốc)
  → Tính tổng:
      totalAmount   = Σ (quantity × unitPrice) cho tất cả items
      discountAmount = Giảm giá (nếu có)
      finalAmount    = totalAmount - discountAmount
```

**Ví dụ thực tế:**
```
Hóa đơn: INV-000001 | Bệnh nhân: Nguyễn Văn A
┌─────────────────────┬────┬────────────┬────────────┐
│ Mô tả               │ SL │ Đơn giá    │ Thành tiền │
├─────────────────────┼────┼────────────┼────────────┤
│ Điều trị tủy        │  1 │  1,500,000 │  1,500,000 │  ← Service
│ Trám răng composite │  1 │    500,000 │    500,000 │  ← Service
│ Amoxicillin 500mg   │ 21 │      3,000 │     63,000 │  ← Medicine
│ Paracetamol 500mg   │ 10 │      2,000 │     20,000 │  ← Medicine
├─────────────────────┼────┼────────────┼────────────┤
│ Tổng cộng           │    │            │  2,083,000 │
│ Giảm giá            │    │            │    -83,000 │
│ Thành tiền           │    │            │  2,000,000 │
└─────────────────────┴────┴────────────┴────────────┘
Status: UNPAID
```

**Chuyển trạng thái:**
```
UNPAID ──(Thanh toán đủ)────────→ PAID
UNPAID ──(Thanh toán 1 phần)───→ PARTIALLY_PAID
PARTIALLY_PAID ──(Thanh toán nốt)→ PAID
UNPAID ──(Hủy)─────────────────→ CANCELLED
```

---

## ⑥ Thanh toán (Payment)

**Ai thực hiện:** Lễ tân / Thu ngân (cashier)

**Khi nào:** Bệnh nhân ra quầy thanh toán

**Luồng:**
```
Lễ tân mở Invoice của bệnh nhân:
  → Bệnh nhân chọn phương thức thanh toán (CASH, CARD, TRANSFER, MOMO, ...)
  → Nhập số tiền thanh toán
  → Tạo Payment record
  → Nếu tổng Payment.amount >= Invoice.finalAmount → Invoice.status = PAID
  → Nếu chưa đủ → Invoice.status = PARTIALLY_PAID
```

> 💡 **1 Invoice có thể có nhiều Payment** (thanh toán nhiều lần, hoặc chia payment method)
> VD: Trả 1,000,000 tiền mặt + 1,000,000 chuyển khoản

**Entity liên quan:**
| Entity | Quan hệ |
|--------|---------|
| `Invoice` | `@ManyToOne` — Thanh toán cho hóa đơn nào |
| `Staff` (cashier) | `@ManyToOne` — Ai thu tiền |

---

## Sơ đồ tổng thể: 1 ca khám từ A → Z

```
BỆNH NHÂN ĐẶT LỊCH
        │
        ▼
┌─────────────────┐
│   Appointment    │  status: PENDING
│   APT-000001     │  patient: Nguyễn Văn A
│                  │  staff: BS. Trần B (DENTIST)
│                  │  note: "Đau răng hàm dưới bên trái"
│                  │  date: 2026-03-20, 09:00-10:00
└────────┬────────┘
         │  Lễ tân xác nhận → status: CONFIRMED
         │  BN đến phòng khám
         ▼
┌─────────────────┐
│ VisitRegistration│  status: WAITING
│   VR-000001      │  queueNumber: 3
│                  │  receptionist: Chị Lan (RECEPTIONIST)
└────────┬────────┘
         │  Nha sĩ gọi vào → status: IN_PROGRESS
         │                    Appointment.status: IN_PROGRESS
         ▼
┌─────────────────┐
│  MedicalRecord   │  chiefComplaint: "Đau răng 36"
│   MR-000001      │  diagnosis: "Sâu răng độ 3"
│                  │  treatmentPlan: "Điều trị tủy + trám"
│                  │  services: [Điều trị tủy, Trám răng]
│                  │  dentist: BS. Trần B
└────────┬────────┘
         │  Nha sĩ kê đơn (nếu cần)
         ▼
┌─────────────────┐
│  Prescription    │  note: "Tái khám sau 7 ngày"
│   PRE-000001     │  dentist: BS. Trần B
│                  │
│  Items:          │
│  ├ Amoxicillin   │  qty: 21, dosage: "3 viên/ngày"
│  ├ Paracetamol   │  qty: 10, dosage: "2 viên/ngày"
│  └ Metronidazol  │  qty: 14, dosage: "2 viên/ngày"
└────────┬────────┘
         │  Khám xong → Appointment.status: COMPLETE
         │               VisitRegistration.status: DONE
         ▼
┌─────────────────┐
│    Invoice       │  status: UNPAID
│   INV-000001     │  totalAmount: 2,083,000
│                  │  discountAmount: 83,000
│  Items:          │  finalAmount: 2,000,000
│  ├ Điều trị tủy  │  1 × 1,500,000
│  ├ Trám răng     │  1 × 500,000
│  ├ Amoxicillin   │  21 × 3,000
│  └ Paracetamol   │  10 × 2,000
└────────┬────────┘
         │  BN ra quầy thanh toán
         ▼
┌─────────────────┐
│    Payment       │  amount: 2,000,000
│   PAY-000001     │  method: CASH
│                  │  cashier: Chị Lan (RECEPTIONIST)
└─────────────────┘
         │
         ▼
   Invoice.status: PAID  ✅
   ══════════════════════
   HOÀN TẤT CA KHÁM 🎉
```

---

## Vai trò từng loại nhân viên

| StaffType | Làm gì trong luồng |
|-----------|-------------------|
| **RECEPTIONIST** (Lễ tân) | Đặt lịch hộ BN, xác nhận appointment, tiếp nhận (check-in), tạo hóa đơn, thu tiền |
| **DENTIST** (Nha sĩ) | Khám bệnh, tạo MedicalRecord, kê đơn thuốc (Prescription) |
| **NURSE** (Y tá) | Hỗ trợ nha sĩ trong phòng khám (thao tác trên hệ thống ít) |
| **ADMIN** (Quản lý) | Quản lý nhân viên, dịch vụ, thuốc, xem báo cáo, phân quyền |

---

## Luồng phụ / Quản lý danh mục

| Nghiệp vụ | Entity | Ai quản lý |
|-----------|--------|-----------|
| Quản lý nhân viên | `Staff` + `User` | ADMIN |
| Quản lý bệnh nhân | `Patient` + `User` | ADMIN, RECEPTIONIST |
| Quản lý dịch vụ | `ServiceCategory` + `Service` | ADMIN |
| Quản lý thuốc | `Medicine` | ADMIN |
| Phân quyền | `Role` + `Page` + `RolePagePermission` | ADMIN |
| Sinh mã tự động | `Sequence` | Hệ thống (tự động) |

---

## 🔗 Đồng bộ dữ liệu: Walk-in → Đăng ký tài khoản (Claim Patient)

### Vấn đề

Bệnh nhân walk-in đã khám nhiều lần (có lịch sử `MedicalRecord`, `Prescription`, `Invoice`...) nhưng chưa có tài khoản (`user = null`). Khi họ đăng ký tài khoản online, cần **liên kết (claim)** `User` mới vào `Patient` cũ để xem được toàn bộ lịch sử khám.

### Giải pháp: Claim by Phone Number

**Nguyên tắc cốt lõi:**
- `phone` là định danh duy nhất (`unique`) của mỗi `Patient`
- Khi BN đăng ký tài khoản → hệ thống kiểm tra SĐT đã tồn tại trong `Patient` chưa
- Nếu tìm thấy `Patient` cũ (walk-in, chưa có user) → **link** thay vì tạo mới

### Luồng chi tiết

```
BN walk-in (đã khám trước đó, chưa có tài khoản)
    │
    │  Patient: { code: "PT-000042", phone: "0901234567", isWalkIn: true, user: null }
    │  MedicalRecord: [MR-000001, MR-000005, MR-000012]  ← lịch sử khám
    │
    ▼
BN đăng ký tài khoản online (nhập SĐT: 0901234567)
    │
    ▼
┌───────────────────────────────────────────┐
│ Hệ thống kiểm tra: findByPhoneAndUserNull│
│                                           │
│  SELECT p FROM Patient p                  │
│  WHERE p.phone = :phone                   │
│  AND p.user IS NULL                       │
│  AND p.isWalkIn = TRUE                    │
└───────────────┬───────────────────────────┘
                │
        ┌───────┴───────┐
        │               │
   TÌM THẤY      KHÔNG TÌM THẤY
        │               │
        ▼               ▼
┌──────────────┐  ┌──────────────────┐
│ CLAIM:       │  │ TẠO MỚI:        │
│ Link User    │  │ Tạo Patient mới  │
│ vào Patient  │  │ gắn User vào     │
│ cũ           │  │ isWalkIn = false  │
│              │  │                  │
│ patient.user │  │ patient.user     │
│   = newUser  │  │   = newUser      │
│ isWalkIn     │  │                  │
│   = false    │  │                  │
└──────────────┘  └──────────────────┘
        │               │
        ▼               ▼
   BN đăng nhập → thấy toàn bộ lịch sử khám cũ ✅
```

### Các trường hợp edge case

| Case | SĐT đăng ký | Patient cũ | Kết quả |
|------|-------------|------------|---------|
| **A** — Walk-in có SĐT khớp | 0901234567 | PT-000042 (walk-in, user=null) | ✅ Link User → Patient cũ |
| **B** — Chưa khám lần nào | 0909999999 | Không tồn tại | ✅ Tạo Patient mới |
| **C** — SĐT đã có tài khoản | 0901234567 | PT-000042 (user ≠ null) | ❌ Báo lỗi "SĐT đã được liên kết" |
| **D** — Walk-in nhưng SĐT khác | 0901111111 | PT-000042 (phone=0901234567) | ❌ Không match → Tạo mới, admin merge sau |

### Xác minh quyền sở hữu (OTP)

> ⚠️ **Quan trọng:** Để tránh ai đó claim nhầm Patient của người khác, cần **xác minh OTP qua SĐT**.

```
BN đăng ký → Nhập SĐT → Hệ thống gửi OTP → BN nhập OTP đúng → Claim Patient
```

Nếu chưa làm OTP, có thể dùng flow đơn giản hơn:
- **Lễ tân/Admin xác nhận thủ công:** BN đến quầy, lễ tân verify danh tính rồi link tài khoản

### Trường hợp Admin merge thủ công

Khi BN walk-in dùng SĐT khác với SĐT đăng ký tài khoản (VD: đổi số):
```
Admin mở trang quản lý → Tìm Patient cũ → Gắn User vào → Cập nhật SĐT
→ Tất cả MedicalRecord, Invoice... vẫn giữ nguyên vì FK trỏ đến Patient.id (không đổi)
```

### Tại sao cách này hoạt động?

Tất cả dữ liệu khám bệnh đều liên kết qua `Patient.id` (FK):
```
MedicalRecord.patient_id  ─┐
Appointment.patient_id     ├── Tất cả trỏ đến Patient.id
Prescription.patient_id    │   (KHÔNG BAO GIỜ THAY ĐỔI)
Invoice (qua MedicalRecord)┘

Khi claim: chỉ cập nhật Patient.user = newUser
→ Patient.id giữ nguyên
→ Toàn bộ lịch sử tự động "thuộc về" user mới ✅
```

---

## 👶 Bệnh nhân nhỏ tuổi (Minor) — Người giám hộ (Guardian)

### Vấn đề

Trẻ em chưa đủ tuổi sở hữu SĐT → không thể dùng SĐT cá nhân làm định danh, không thể tự đăng ký tài khoản.

### Giải pháp: Guardian Relationship trên Patient

**Entity Patient thêm các trường:**

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `isMinor` | Boolean | `true` = BN chưa đủ tuổi, cần người giám hộ |
| `guardian` | `@ManyToOne Patient` | Self-referencing FK: phụ huynh CŨNG LÀ bệnh nhân |
| `guardianName` | String | Họ tên phụ huynh (nếu guardian không phải BN) |
| `guardianPhone` | String | SĐT liên hệ phụ huynh |
| `guardianRelationship` | Enum | CHA, MẸ, ÔNG, BÀ, ANH_CHỊ, CHÚ_DÌ, KHÁC |

### 2 cách ghi nhận Guardian

```
CÁC CÁCH GHI NHẬN GUARDIAN
═══════════════════════════

CÁCH 1: Phụ huynh ĐÃ CÓ trong hệ thống (đã khám, hoặc đã đăng ký)
─────────────────────────────────────────────────────────────────────
  Mẹ (BN-000010, phone: 0901234567) đưa con đi khám
  → Lễ tân tạo BN con:
     Patient {
       code: "BN-000042",
       fullName: "Nguyễn Bé An",
       isMinor: true,
       guardian: BN-000010,       ← FK trỏ đến Patient mẹ
       guardianPhone: "0901234567",
       guardianRelationship: MOTHER
     }

CÁCH 2: Phụ huynh CHƯA CÓ trong hệ thống (walk-in đầu tiên)
──────────────────────────────────────────────────────────────
  Ông nội đưa cháu đi khám (ông chưa từng khám)
  → Lễ tân tạo BN con:
     Patient {
       code: "BN-000043",
       fullName: "Nguyễn Bé Bình",
       isMinor: true,
       guardian: null,            ← chưa có Patient ông
       guardianName: "Nguyễn Văn Tám",  ← ghi text
       guardianPhone: "0909876543",
       guardianRelationship: GRANDFATHER
     }
```

### Luồng auto-link khi phụ huynh đăng ký tài khoản

```
Phụ huynh đăng ký tài khoản (SĐT: 0901234567)
    │
    ▼
① Claim Patient phụ huynh (nếu có walk-in cũ)
    │
    ▼
② Hệ thống tự tìm BN con:
   SELECT p FROM Patient p
   WHERE p.isMinor = TRUE
   AND p.guardianPhone = '0901234567'
   AND p.guardian IS NULL
    │
    ▼
③ Auto-link: minor.guardian = phụ huynh (Patient)
    │
    ▼
④ Phụ huynh đăng nhập → GET /patients/me/dependents
   → Thấy danh sách BN con + toàn bộ lịch sử khám con ✅
```

### Quy tắc SĐT cho Minor

| Loại BN | `phone` | `guardianPhone` | Unique check |
|---------|---------|-----------------|-------------|
| Người lớn | SĐT cá nhân (bắt buộc) | null | Kiểm tra trùng |
| Trẻ em | null (không có SĐT) | SĐT phụ huynh (bắt buộc) | KHÔNG kiểm tra trùng (nhiều con chung 1 SĐT) |

### API Endpoints

| Method | Path | Mô tả | Auth |
|--------|------|-------|------|
| `POST` | `/examination/walk-in-patients` | Tạo BN walk-in (hỗ trợ `isMinor` + guardian) | Staff |
| `POST` | `/auth/register` | Đăng ký + auto claim + auto link BN con | Public |
| `GET` | `/patients/me` | Xem hồ sơ bản thân | Patient |
| `GET` | `/patients/me/dependents` | Phụ huynh xem danh sách BN con | Patient |
| `POST` | `/patients/link` | Admin link thủ công Patient ↔ User | Admin |

