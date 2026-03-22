package cd.beapi.service.impl;

import cd.beapi.dto.request.*;
import cd.beapi.dto.response.*;
import cd.beapi.entity.*;
import cd.beapi.enumerate.*;
import cd.beapi.exception.AppException;
import cd.beapi.repository.jpa.*;
import cd.beapi.service.ExaminationService;
import cd.beapi.service.SequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExaminationServiceImpl implements ExaminationService {

    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final VisitRegistrationRepository visitRegistrationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final MedicineRepository medicineRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SequenceService sequenceService;

    // ──────────────────────────────────────────────
    // ① TẠO BỆNH NHÂN WALK-IN
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public PatientResponse createWalkInPatient(WalkInPatientRequest request) {
        boolean isMinor = Boolean.TRUE.equals(request.isMinor());

        // ── Xác định SĐT liên hệ chính ──
        String contactPhone = isMinor ? request.guardianPhone() : request.phone();
        if (contactPhone == null || contactPhone.isBlank()) {
            throw new AppException(
                    isMinor ? "SĐT người giám hộ không được để trống khi bệnh nhân là trẻ em"
                            : "Số điện thoại không được để trống",
                    HttpStatus.BAD_REQUEST);
        }

        // ── Kiểm tra SĐT trùng (chỉ cho BN người lớn — BN nhỏ tuổi dùng SĐT guardian, có thể trùng) ──
        if (!isMinor) {
            patientRepository.findByPhone(request.phone()).ifPresent(p -> {
                throw new AppException("Số điện thoại đã tồn tại trong hệ thống: " + p.getCode(), HttpStatus.CONFLICT);
            });
        }

        // ── Build Patient ──
        Patient patient = Patient.builder()
                .code(sequenceService.generatePatientCode())
                .fullName(request.fullName())
                .phone(isMinor ? null : request.phone())   // Trẻ em không có SĐT riêng
                .dob(request.dob() != null ? LocalDate.parse(request.dob()) : null)
                .gender(request.gender())
                .email(request.email())
                .identityNumber(request.identityNumber())
                .address(request.address())
                .isWalkIn(true)
                .isMinor(isMinor)
                .build();

        // ── Gắn thông tin Guardian nếu BN nhỏ tuổi ──
        if (isMinor) {
            if (request.guardianId() != null) {
                // Phụ huynh đã có trong hệ thống → link qua FK
                Patient guardian = patientRepository.findById(request.guardianId())
                        .orElseThrow(() -> new AppException(
                                "Không tìm thấy người giám hộ với ID: " + request.guardianId(),
                                HttpStatus.NOT_FOUND));
                patient.setGuardian(guardian);
            } else {
                // Phụ huynh chưa có trong hệ thống → lưu thông tin text
                if (request.guardianName() == null || request.guardianName().isBlank()) {
                    throw new AppException("Họ tên người giám hộ không được để trống", HttpStatus.BAD_REQUEST);
                }
                patient.setGuardianName(request.guardianName());
            }

            patient.setGuardianPhone(request.guardianPhone());

            if (request.guardianRelationship() != null) {
                patient.setGuardianRelationship(
                        cd.beapi.enumerate.GuardianRelationship.valueOf(request.guardianRelationship()));
            }
        }

        patient = patientRepository.save(patient);
        return toPatientResponse(patient);
    }

    // ──────────────────────────────────────────────
    // ② ĐẶT LỊCH HẸN
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new AppException("Không tìm thấy bệnh nhân", HttpStatus.NOT_FOUND));

        Staff dentist = staffRepository.findById(request.dentistId())
                .orElseThrow(() -> new AppException("Không tìm thấy nha sĩ", HttpStatus.NOT_FOUND));

        if (dentist.getStaffType() != StaffType.DENTIST) {
            throw new AppException("Nhân viên được chọn không phải nha sĩ", HttpStatus.BAD_REQUEST);
        }

        LocalDate date = LocalDate.parse(request.appointmentDate());
        LocalTime start = LocalTime.parse(request.startTime());
        LocalTime end = LocalTime.parse(request.endTime());

        if (!end.isAfter(start)) {
            throw new AppException("Giờ kết thúc phải sau giờ bắt đầu", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra nha sĩ có bị trùng lịch không (JPQL kiểm tra overlap trực tiếp trong DB)
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                dentist.getId(), date, start, end,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS));

        if (!conflicts.isEmpty()) {
            throw new AppException(
                    "Nha sĩ đã có lịch hẹn trong khung giờ này: " + conflicts.getFirst().getCode(),
                    HttpStatus.CONFLICT);
        }

        // Walk-in → CONFIRMED luôn, Online → PENDING
        AppointmentStatus initialStatus = Boolean.TRUE.equals(patient.getIsWalkIn())
                ? AppointmentStatus.CONFIRMED
                : AppointmentStatus.PENDING;

        Appointment appointment = Appointment.builder()
                .code(sequenceService.generateAppointmentCode())
                .appointmentDate(date)
                .startTime(start)
                .endTime(end)
                .note(request.note())
                .status(initialStatus)
                .patient(patient)
                .staff(dentist)
                .build();

        appointment = appointmentRepository.save(appointment);
        return toAppointmentResponse(appointment);
    }

    // ──────────────────────────────────────────────
    // ③ XÁC NHẬN LỊCH HẸN
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponse confirmAppointment(Long appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppException(
                    "Chỉ có thể xác nhận lịch hẹn đang ở trạng thái CHỜ XÁC NHẬN. Hiện tại: " + appointment.getStatus().getLabel(),
                    HttpStatus.BAD_REQUEST);
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);
        return toAppointmentResponse(appointment);
    }

    // ──────────────────────────────────────────────
    // ④ TIẾP NHẬN / CHECK-IN
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public CheckInResponse checkIn(CheckInRequest request) {
        Appointment appointment = findAppointmentById(request.appointmentId());

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(
                    "Chỉ có thể tiếp nhận lịch hẹn đã XÁC NHẬN. Hiện tại: " + appointment.getStatus().getLabel(),
                    HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra đã check-in chưa
        visitRegistrationRepository.findByAppointmentId(appointment.getId()).ifPresent(vr -> {
            throw new AppException("Lịch hẹn này đã được tiếp nhận: " + vr.getCode(), HttpStatus.CONFLICT);
        });

        // Lấy số thứ tự tiếp theo trong ngày
        Integer maxQueue = visitRegistrationRepository.findMaxQueueNumberByDate(appointment.getAppointmentDate());
        int nextQueue = (maxQueue != null ? maxQueue : 0) + 1;

        // Tìm lễ tân
        Staff receptionist = null;
        if (request.receptionistId() != null) {
            receptionist = staffRepository.findById(request.receptionistId()).orElse(null);
        }

        VisitRegistration vr = VisitRegistration.builder()
                .code(sequenceService.generateVisitRegistrationCode())
                .status(VisitRegistrationStatus.WAITING)
                .queueNumber(nextQueue)
                .note(request.note())
                .appointment(appointment)
                .receptionist(receptionist)
                .build();

        vr = visitRegistrationRepository.save(vr);
        return toCheckInResponse(vr, appointment);
    }

    // ──────────────────────────────────────────────
    // ⑤ KHÁM BỆNH — TẠO BỆNH ÁN + CHỌN DỊCH VỤ
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public MedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request) {
        Appointment appointment = findAppointmentById(request.appointmentId());

        // Phải có visit registration (đã check-in)
        VisitRegistration vr = visitRegistrationRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new AppException("Bệnh nhân chưa được tiếp nhận (check-in)", HttpStatus.BAD_REQUEST));

        // Kiểm tra đã có bệnh án chưa
        medicalRecordRepository.findByAppointmentId(appointment.getId()).ifPresent(mr -> {
            throw new AppException("Lịch hẹn này đã có bệnh án: " + mr.getCode(), HttpStatus.CONFLICT);
        });

        // Chuyển trạng thái
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointmentRepository.save(appointment);

        vr.setStatus(VisitRegistrationStatus.IN_PROGRESS);
        visitRegistrationRepository.save(vr);

        // Lấy danh sách dịch vụ
        Set<cd.beapi.entity.Service> services = new HashSet<>();
        if (request.serviceIds() != null && !request.serviceIds().isEmpty()) {
            List<cd.beapi.entity.Service> found = clinicServiceRepository.findAllByIdIn(request.serviceIds());
            if (found.size() != request.serviceIds().size()) {
                throw new AppException("Một hoặc nhiều dịch vụ không tồn tại", HttpStatus.NOT_FOUND);
            }
            services.addAll(found);
        }

        MedicalRecord record = MedicalRecord.builder()
                .code(sequenceService.generateMedicalRecordCode())
                .chiefComplaint(request.chiefComplaint())
                .diagnosis(request.diagnosis())
                .treatmentPlan(request.treatmentPlan())
                .notes(request.notes())
                .patient(appointment.getPatient())
                .staff(appointment.getStaff())
                .appointment(appointment)
                .services(services)
                .build();

        record = medicalRecordRepository.save(record);
        return toMedicalRecordResponse(record);
    }

    // ──────────────────────────────────────────────
    // ⑥ KÊ ĐƠN THUỐC
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.medicalRecordId())
                .orElseThrow(() -> new AppException("Không tìm thấy bệnh án", HttpStatus.NOT_FOUND));

        // Kiểm tra đã có đơn thuốc chưa
        prescriptionRepository.findByMedicalRecordId(record.getId()).ifPresent(p -> {
            throw new AppException("Bệnh án này đã có đơn thuốc: " + p.getCode(), HttpStatus.CONFLICT);
        });

        Prescription prescription = Prescription.builder()
                .code(sequenceService.generatePrescriptionCode())
                .note(request.note())
                .patient(record.getPatient())
                .dentist(record.getStaff())
                .medicalRecord(record)
                .build();

        prescription = prescriptionRepository.save(prescription);

        // Tạo chi tiết đơn thuốc
        List<PrescriptionItem> items = new ArrayList<>();
        if (request.items() != null) {
            for (CreatePrescriptionRequest.PrescriptionItemRequest itemReq : request.items()) {
                Medicine medicine = medicineRepository.findById(itemReq.medicineId())
                        .orElseThrow(() -> new AppException("Không tìm thấy thuốc ID: " + itemReq.medicineId(), HttpStatus.NOT_FOUND));

                PrescriptionItem item = PrescriptionItem.builder()
                        .prescription(prescription)
                        .medicine(medicine)
                        .quantity(itemReq.quantity())
                        .dosage(itemReq.dosage())
                        .instruction(itemReq.instruction())
                        .build();

                items.add(item);
            }
            prescriptionItemRepository.saveAll(items);
        }

        return toPrescriptionResponse(prescription, items);
    }

    // ──────────────────────────────────────────────
    // ⑦ TẠO HÓA ĐƠN
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Appointment appointment = findAppointmentById(request.appointmentId());

        // Phải có bệnh án
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new AppException("Chưa có bệnh án cho lịch hẹn này", HttpStatus.BAD_REQUEST));

        // Kiểm tra đã có hóa đơn chưa
        invoiceRepository.findByAppointmentId(appointment.getId()).ifPresent(inv -> {
            throw new AppException("Lịch hẹn này đã có hóa đơn: " + inv.getCode(), HttpStatus.CONFLICT);
        });

        // Hoàn tất khám
        appointment.setStatus(AppointmentStatus.COMPLETE);
        appointmentRepository.save(appointment);

        VisitRegistration vr = visitRegistrationRepository.findByAppointmentId(appointment.getId()).orElse(null);
        if (vr != null) {
            vr.setStatus(VisitRegistrationStatus.DONE);
            visitRegistrationRepository.save(vr);
        }

        // === Tính toán items ===
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Items từ dịch vụ (MedicalRecord.services)
        if (record.getServices() != null) {
            for (cd.beapi.entity.Service svc : record.getServices()) {
                BigDecimal price = svc.getPrice() != null ? svc.getPrice() : BigDecimal.ZERO;
                InvoiceItem item = InvoiceItem.builder()
                        .service(svc)
                        .quantity(1)
                        .unitPrice(price)
                        .amount(price)
                        .description(svc.getName())
                        .build();
                invoiceItems.add(item);
                totalAmount = totalAmount.add(price);
            }
        }

        // Items từ đơn thuốc (nếu có)
        Optional<Prescription> prescriptionOpt = prescriptionRepository.findByMedicalRecordId(record.getId());
        if (prescriptionOpt.isPresent()) {
            List<PrescriptionItem> prescriptionItems = prescriptionItemRepository
                    .findByPrescriptionId(prescriptionOpt.get().getId());

            for (PrescriptionItem pi : prescriptionItems) {
                // Giá thuốc: tạm lấy 0 nếu Medicine chưa có price
                // (có thể mở rộng thêm field price cho Medicine sau)
                BigDecimal unitPrice = BigDecimal.ZERO;
                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(pi.getQuantity()));

                InvoiceItem item = InvoiceItem.builder()
                        .medicine(pi.getMedicine())
                        .quantity(pi.getQuantity())
                        .unitPrice(unitPrice)
                        .amount(amount)
                        .description(pi.getMedicine().getName() + " (" + pi.getMedicine().getUnit() + ")")
                        .build();
                invoiceItems.add(item);
                totalAmount = totalAmount.add(amount);
            }
        }

        BigDecimal discountAmount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        Invoice invoice = Invoice.builder()
                .code(sequenceService.generateInvoiceCode())
                .patient(appointment.getPatient())
                .appointment(appointment)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(InvoiceStatus.UNPAID)
                .items(new HashSet<>())
                .build();

        invoice = invoiceRepository.save(invoice);

        // Gắn invoice vào items rồi save
        for (InvoiceItem item : invoiceItems) {
            item.setInvoice(invoice);
        }
        invoice.getItems().addAll(invoiceItems);
        invoice = invoiceRepository.save(invoice);

        return toInvoiceResponse(invoice, invoiceItems);
    }

    // ──────────────────────────────────────────────
    // ⑧ THANH TOÁN
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn", HttpStatus.NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException("Hóa đơn đã được thanh toán đầy đủ", HttpStatus.BAD_REQUEST);
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new AppException("Hóa đơn đã bị hủy", HttpStatus.BAD_REQUEST);
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.paymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Phương thức thanh toán không hợp lệ: " + request.paymentMethod(), HttpStatus.BAD_REQUEST);
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Số tiền thanh toán phải lớn hơn 0", HttpStatus.BAD_REQUEST);
        }

        // Tìm cashier
        Staff cashier = null;
        if (request.cashierId() != null) {
            cashier = staffRepository.findById(request.cashierId()).orElse(null);
        }

        Payment payment = Payment.builder()
                .code(sequenceService.generatePaymentCode())
                .invoice(invoice)
                .amount(request.amount())
                .paymentMethod(method)
                .paidAt(Instant.now())
                .note(request.note())
                .cashier(cashier)
                .build();

        payment = paymentRepository.save(payment);

        // Cập nhật trạng thái hóa đơn
        BigDecimal totalPaid = paymentRepository.sumAmountByInvoiceId(invoice.getId());
        if (totalPaid.compareTo(invoice.getFinalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        BigDecimal remaining = invoice.getFinalAmount().subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        return toPaymentResponse(payment, invoice, remaining);
    }

    // ══════════════════════════════════════════════
    //  MAPPER HELPERS
    // ══════════════════════════════════════════════

    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException("Không tìm thấy lịch hẹn", HttpStatus.NOT_FOUND));
    }

    private PatientResponse toPatientResponse(Patient p) {
        Patient g = p.getGuardian();
        return new PatientResponse(
                p.getId(), p.getCode(), p.getFullName(), p.getPhone(),
                p.getDob() != null ? p.getDob().toString() : null,
                p.getGender(), p.getEmail(), p.getAddress(),
                p.getIsWalkIn(), p.getIsMinor(),
                // Guardian info
                g != null ? g.getId() : null,
                g != null ? g.getCode() : null,
                g != null ? g.getFullName() : p.getGuardianName(),
                g != null ? g.getPhone() : p.getGuardianPhone(),
                p.getGuardianRelationship() != null ? p.getGuardianRelationship().name() : null,
                p.getGuardianRelationship() != null ? p.getGuardianRelationship().getLabel() : null,
                p.getCreatedAt());
    }

    private AppointmentResponse toAppointmentResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(), a.getCode(),
                a.getAppointmentDate().toString(),
                a.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                a.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                a.getStatus().name(), a.getStatus().getLabel(),
                a.getNote(),
                a.getPatient().getFullName(), a.getPatient().getCode(),
                a.getStaff().getFullName(), a.getStaff().getCode(),
                a.getCreatedAt());
    }

    private CheckInResponse toCheckInResponse(VisitRegistration vr, Appointment a) {
        return new CheckInResponse(
                vr.getId(), vr.getCode(), vr.getQueueNumber(),
                vr.getStatus().name(), vr.getStatus().getLabel(),
                vr.getNote(), a.getCode(),
                a.getPatient().getFullName(),
                vr.getReceptionist() != null ? vr.getReceptionist().getFullName() : null,
                vr.getCreatedAt());
    }

    private MedicalRecordResponse toMedicalRecordResponse(MedicalRecord mr) {
        List<MedicalRecordResponse.ServiceItem> serviceItems = mr.getServices().stream()
                .map(s -> new MedicalRecordResponse.ServiceItem(
                        s.getId(), s.getCode(), s.getName(),
                        s.getPrice() != null ? s.getPrice().toPlainString() : "0"))
                .toList();

        return new MedicalRecordResponse(
                mr.getId(), mr.getCode(),
                mr.getChiefComplaint(), mr.getDiagnosis(),
                mr.getTreatmentPlan(), mr.getNotes(),
                mr.getPatient().getFullName(), mr.getPatient().getCode(),
                mr.getStaff().getFullName(), mr.getAppointment().getCode(),
                serviceItems, mr.getCreatedAt());
    }

    private PrescriptionResponse toPrescriptionResponse(Prescription p, List<PrescriptionItem> items) {
        List<PrescriptionResponse.PrescriptionItemDetail> details = items.stream()
                .map(i -> new PrescriptionResponse.PrescriptionItemDetail(
                        i.getId(),
                        i.getMedicine().getName(),
                        i.getMedicine().getUnit(),
                        i.getQuantity(), i.getDosage(), i.getInstruction()))
                .toList();

        return new PrescriptionResponse(
                p.getId(), p.getCode(), p.getNote(),
                p.getPatient().getFullName(),
                p.getDentist().getFullName(),
                p.getMedicalRecord().getCode(),
                details, p.getCreatedAt());
    }

    private InvoiceResponse toInvoiceResponse(Invoice inv, List<InvoiceItem> items) {
        List<InvoiceResponse.InvoiceItemDetail> details = items.stream()
                .map(i -> new InvoiceResponse.InvoiceItemDetail(
                        i.getId(),
                        i.getService() != null ? "SERVICE" : "MEDICINE",
                        i.getDescription(),
                        i.getQuantity(), i.getUnitPrice(), i.getAmount(),
                        i.getDescription()))
                .toList();

        return new InvoiceResponse(
                inv.getId(), inv.getCode(),
                inv.getStatus().name(), inv.getStatus().getLabel(),
                inv.getPatient().getFullName(), inv.getAppointment().getCode(),
                inv.getTotalAmount(), inv.getDiscountAmount(), inv.getFinalAmount(),
                details, inv.getCreatedAt());
    }

    private PaymentResponse toPaymentResponse(Payment pay, Invoice inv, BigDecimal remaining) {
        return new PaymentResponse(
                pay.getId(), pay.getCode(),
                pay.getAmount(),
                pay.getPaymentMethod().name(), pay.getPaymentMethod().getLabel(),
                pay.getNote(),
                pay.getCashier() != null ? pay.getCashier().getFullName() : null,
                inv.getCode(),
                inv.getStatus().name(), inv.getStatus().getLabel(),
                remaining,
                pay.getPaidAt(), pay.getCreatedAt());
    }
}




