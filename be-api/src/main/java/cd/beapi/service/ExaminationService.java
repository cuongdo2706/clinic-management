package cd.beapi.service;

import cd.beapi.dto.request.*;
import cd.beapi.dto.response.*;

public interface ExaminationService {

    // ① Tạo bệnh nhân walk-in
    PatientResponse createWalkInPatient(WalkInPatientRequest request);

    // ② Đặt lịch hẹn
    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    // ③ Xác nhận lịch hẹn
    AppointmentResponse confirmAppointment(Long appointmentId);

    // ④ Tiếp nhận / Check-in
    CheckInResponse checkIn(CheckInRequest request);

    // ⑤ Khám bệnh — tạo bệnh án + chọn dịch vụ
    MedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request);

    // ⑥ Kê đơn thuốc
    PrescriptionResponse createPrescription(CreatePrescriptionRequest request);

    // ⑦ Tạo hóa đơn
    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    // ⑧ Thanh toán
    PaymentResponse createPayment(CreatePaymentRequest request);
}

