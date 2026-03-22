package cd.beapi.controller.clinic;

import cd.beapi.dto.request.*;
import cd.beapi.dto.response.*;
import cd.beapi.service.ExaminationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/examination")
@RequiredArgsConstructor
public class ExaminationController {

    private final ExaminationService examinationService;

    // ──────────────────────────────────────────────
    // ① Tạo bệnh nhân walk-in
    // POST /api/v1/examination/walk-in-patients
    // ──────────────────────────────────────────────
    @PostMapping("/walk-in-patients")
    public ResponseEntity<SuccessResponse<PatientResponse>> createWalkInPatient(
            @Valid @RequestBody WalkInPatientRequest request) {
        PatientResponse response = examinationService.createWalkInPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, "Tạo bệnh nhân walk-in thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // ② Đặt lịch hẹn
    // POST /api/v1/examination/appointments
    // ──────────────────────────────────────────────
    @PostMapping("/appointments")
    public ResponseEntity<SuccessResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = examinationService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, "Đặt lịch hẹn thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // ③ Xác nhận lịch hẹn
    // PUT /api/v1/examination/appointments/{id}/confirm
    // ──────────────────────────────────────────────
    @PutMapping("/appointments/{id}/confirm")
    public ResponseEntity<SuccessResponse<AppointmentResponse>> confirmAppointment(
            @PathVariable Long id) {
        AppointmentResponse response = examinationService.confirmAppointment(id);
        return ResponseEntity.ok(
                new SuccessResponse<>(200, "Xác nhận lịch hẹn thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // ④ Tiếp nhận / Check-in
    // POST /api/v1/examination/check-in
    // ──────────────────────────────────────────────
    @PostMapping("/check-in")
    public ResponseEntity<SuccessResponse<CheckInResponse>> checkIn(
            @Valid @RequestBody CheckInRequest request) {
        CheckInResponse response = examinationService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, "Tiếp nhận bệnh nhân thành công — Số thứ tự: " + response.queueNumber(), Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // ⑤ Khám bệnh — Tạo bệnh án
    // POST /api/v1/examination/medical-records
    // ──────────────────────────────────────────────
    @PostMapping("/medical-records")
    public ResponseEntity<SuccessResponse<MedicalRecordResponse>> createMedicalRecord(
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        MedicalRecordResponse response = examinationService.createMedicalRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, "Tạo bệnh án thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // ⑥ Kê đơn thuốc
    // POST /api/v1/examination/prescriptions
    // ──────────────────────────────────────────────
    @PostMapping("/prescriptions")
    public ResponseEntity<SuccessResponse<PrescriptionResponse>> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequest request) {
        PrescriptionResponse response = examinationService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, "Kê đơn thuốc thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // ⑦ Tạo hóa đơn
    // POST /api/v1/examination/invoices
    // ──────────────────────────────────────────────
    @PostMapping("/invoices")
    public ResponseEntity<SuccessResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = examinationService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, "Tạo hóa đơn thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // ⑧ Thanh toán
    // POST /api/v1/examination/payments
    // ──────────────────────────────────────────────
    @PostMapping("/payments")
    public ResponseEntity<SuccessResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = examinationService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, "Thanh toán thành công", Instant.now(), response));
    }
}

