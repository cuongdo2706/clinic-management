package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateTreatmentRequest;
import cd.beapi.dto.request.PrescriptionItemRequest;
import cd.beapi.dto.request.PrescriptionRequest;
import cd.beapi.dto.request.UpdateTreatmentRequest;
import cd.beapi.dto.response.*;
import cd.beapi.entity.*;
import cd.beapi.enumerate.TreatmentStatus;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.AppointmentMapper;
import cd.beapi.repository.jpa.*;
import cd.beapi.service.SequenceService;
import cd.beapi.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TreatmentServiceImpl implements TreatmentService {
    private final TreatmentRepository treatmentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final MedicineRepository medicineRepository;
    private final ProcedureRepository procedureRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final TreatmentProcedureRepository treatmentProcedureRepository;
    private final SequenceService sequenceService;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    @Override
    public TreatmentDetailResponse findById(Long id) {
        return toDetail(findTreatment(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<TreatmentSummaryResponse> findByPatientId(Long patientId) {
        ensurePatientExists(patientId);
        return treatmentRepository.findByPatientIdOrderByTreatmentDateDesc(patientId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PrescriptionDetailResponse> findPrescriptionsByPatientId(Long patientId) {
        ensurePatientExists(patientId);
        return prescriptionRepository.findByTreatmentPatientIdOrderByPrescribedAtDesc(patientId)
                .stream()
                .map(this::toPrescriptionDetail)
                .toList();
    }

    @Transactional
    @Override
    public TreatmentDetailResponse save(CreateTreatmentRequest request) {
        Patient patient = findPatient(request.getPatientId());
        Appointment appointment = findAppointment(request.getAppointmentId(), patient);
        Staff doctor = findStaff(request.getDoctorId());

        Treatment treatment = Treatment.builder()
                .patient(patient)
                .appointment(appointment)
                .doctor(doctor)
                .diagnosis(request.getDiagnosis())
                .note(request.getNote())
                .treatmentDate(resolveTreatmentDate(request.getTreatmentDate()))
                .status(request.getStatus() == null ? TreatmentStatus.IN_PROGRESS : request.getStatus())
                .build();

        Treatment saved = treatmentRepository.save(treatment);
        upsertPrescription(saved, request.getPrescription());
        upsertProcedures(saved, request.getProcedures());
        return toDetail(findTreatment(saved.getId()));
    }

    @Transactional
    @Override
    public TreatmentDetailResponse update(Long id, UpdateTreatmentRequest request) {
        Treatment treatment = findTreatment(id);
        Patient patient = treatment.getPatient();
        Appointment appointment = findAppointment(request.getAppointmentId(), patient);
        Staff doctor = findStaff(request.getDoctorId());

        treatment.setAppointment(appointment);
        treatment.setDoctor(doctor);
        treatment.setDiagnosis(request.getDiagnosis());
        treatment.setNote(request.getNote());
        treatment.setTreatmentDate(resolveTreatmentDate(request.getTreatmentDate()));
        treatment.setStatus(request.getStatus() == null ? TreatmentStatus.IN_PROGRESS : request.getStatus());
        if (request.getVersion() != null) {
            treatment.setVersion(request.getVersion());
        }

        Treatment saved = treatmentRepository.save(treatment);
        upsertPrescription(saved, request.getPrescription());
        upsertProcedures(saved, request.getProcedures());
        return toDetail(findTreatment(saved.getId()));
    }

    private void upsertProcedures(Treatment treatment, List<cd.beapi.dto.request.TreatmentProcedureRequest> requests) {
        if (requests == null) {
            return;
        }

        treatmentProcedureRepository.deleteByTreatmentId(treatment.getId());
        List<TreatmentProcedure> procedures = requests.stream()
                .filter(Objects::nonNull)
                .map(request -> toTreatmentProcedure(treatment, request))
                .toList();
        treatmentProcedureRepository.saveAll(procedures);
    }

    private TreatmentProcedure toTreatmentProcedure(Treatment treatment, cd.beapi.dto.request.TreatmentProcedureRequest request) {
        Procedure procedure = procedureRepository.findById(request.getProcedureId()).orElseThrow(
                () -> new AppException("Cannot find procedure with id: " + request.getProcedureId(), HttpStatus.BAD_REQUEST)
        );
        Integer quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        BigDecimal unitPrice = request.getUnitPrice() == null ? procedure.getPrice() : request.getUnitPrice();
        return TreatmentProcedure.builder()
                .treatment(treatment)
                .procedure(procedure)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(request.getNote())
                .build();
    }

    private void upsertPrescription(Treatment treatment, PrescriptionRequest request) {
        if (!hasPrescriptionPayload(request)) {
            return;
        }

        Prescription prescription = prescriptionRepository.findByTreatmentId(treatment.getId())
                .orElseGet(() -> {
                    Prescription created = Prescription.builder()
                            .code(sequenceService.generatePrescriptionCode())
                            .treatment(treatment)
                            .build();
                    treatment.setPrescription(created);
                    return created;
                });

        Staff doctor = request.getDoctorId() == null
                ? treatment.getDoctor()
                : findStaff(request.getDoctorId());
        prescription.setTreatment(treatment);
        prescription.setDoctor(doctor);
        prescription.setPrescribedAt(request.getPrescribedAt() == null ? LocalDateTime.now() : request.getPrescribedAt());
        prescription.setAdvice(request.getAdvice());
        prescription.setReExaminationDate(request.getReExaminationDate());
        prescription.setNote(request.getNote());

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        treatment.setPrescription(savedPrescription);

        if (request.getItems() != null) {
            prescriptionItemRepository.deleteByPrescriptionId(savedPrescription.getId());
            List<PrescriptionItem> items = request.getItems().stream()
                    .filter(Objects::nonNull)
                    .map(item -> toPrescriptionItem(savedPrescription, item))
                    .toList();
            prescriptionItemRepository.saveAll(items);
        }
    }

    private boolean hasPrescriptionPayload(PrescriptionRequest request) {
        return request != null && (
                request.getDoctorId() != null
                        || request.getPrescribedAt() != null
                        || request.getReExaminationDate() != null
                        || StringUtils.hasText(request.getAdvice())
                        || StringUtils.hasText(request.getNote())
                        || (request.getItems() != null && !request.getItems().isEmpty())
        );
    }

    private PrescriptionItem toPrescriptionItem(Prescription prescription, PrescriptionItemRequest item) {
        Medicine medicine = medicineRepository.findById(item.getMedicineId()).orElseThrow(
                () -> new AppException("Cannot find medicine with id: " + item.getMedicineId(), HttpStatus.BAD_REQUEST)
        );
        return PrescriptionItem.builder()
                .prescription(prescription)
                .medicine(medicine)
                .dosage(item.getDosage())
                .frequency(item.getFrequency())
                .duration(item.getDuration())
                .quantity(item.getQuantity())
                .instruction(item.getInstruction())
                .build();
    }

    private Treatment findTreatment(Long id) {
        return treatmentRepository.findByIdWithRelations(id).orElseThrow(
                () -> new AppException("Cannot find treatment with id: " + id, HttpStatus.BAD_REQUEST)
        );
    }

    private Patient findPatient(Long id) {
        return patientRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find patient with id: " + id, HttpStatus.BAD_REQUEST)
        );
    }

    private void ensurePatientExists(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new AppException("Cannot find patient with id: " + id, HttpStatus.BAD_REQUEST);
        }
    }

    private Staff findStaff(Long id) {
        if (id == null) {
            return null;
        }
        return staffRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find staff with id: " + id, HttpStatus.BAD_REQUEST)
        );
    }

    private Appointment findAppointment(Long id, Patient patient) {
        if (id == null) {
            return null;
        }
        Appointment appointment = appointmentRepository.findByIdWithRelations(id).orElseThrow(
                () -> new AppException("Cannot find appointment with id: " + id, HttpStatus.BAD_REQUEST)
        );
        if (appointment.getPatient() != null && !appointment.getPatient().getId().equals(patient.getId())) {
            throw new AppException("Appointment does not belong to selected patient", HttpStatus.BAD_REQUEST);
        }
        return appointment;
    }

    private LocalDateTime resolveTreatmentDate(LocalDateTime treatmentDate) {
        return treatmentDate == null ? LocalDateTime.now() : treatmentDate;
    }

    private TreatmentSummaryResponse toSummary(Treatment treatment) {
        Prescription prescription = treatment.getPrescription();
        int itemCount = prescription == null ? 0 : prescriptionItemRepository.findByPrescriptionId(prescription.getId()).size();
        return new TreatmentSummaryResponse(
                treatment.getId(),
                treatment.getTreatmentDate(),
                treatment.getStatus(),
                treatment.getDiagnosis(),
                treatment.getNote(),
                treatment.getPatient() == null ? null : treatment.getPatient().getId(),
                treatment.getPatient() == null ? null : treatment.getPatient().getFullName(),
                treatment.getAppointment() == null ? null : treatment.getAppointment().getId(),
                treatment.getAppointment() == null ? null : treatment.getAppointment().getCode(),
                treatment.getDoctor() == null ? null : treatment.getDoctor().getId(),
                treatment.getDoctor() == null ? null : treatment.getDoctor().getFullName(),
                prescription != null,
                itemCount,
                treatment.getVersion(),
                treatment.getCreatedAt(),
                treatment.getModifiedAt()
        );
    }

    private TreatmentDetailResponse toDetail(Treatment treatment) {
        return new TreatmentDetailResponse(
                treatment.getId(),
                treatment.getTreatmentDate(),
                treatment.getStatus(),
                treatment.getDiagnosis(),
                treatment.getNote(),
                toPatientInfo(treatment.getPatient()),
                toStaffInfo(treatment.getDoctor()),
                treatment.getAppointment() == null ? null : appointmentMapper.toAppointmentResponse(treatment.getAppointment()),
                findProcedures(treatment),
                treatment.getPrescription() == null ? null : toPrescriptionDetail(treatment.getPrescription()),
                treatment.getVersion(),
                treatment.getCreatedAt(),
                treatment.getModifiedAt()
        );
    }

    private List<TreatmentProcedureResponse> findProcedures(Treatment treatment) {
        return treatmentProcedureRepository.findByTreatmentId(treatment.getId())
                .stream()
                .map(this::toProcedure)
                .toList();
    }

    private TreatmentProcedureResponse toProcedure(TreatmentProcedure procedure) {
        Procedure source = procedure.getProcedure();
        return new TreatmentProcedureResponse(
                source == null ? null : source.getId(),
                source == null ? null : source.getCode(),
                source == null ? null : source.getName(),
                procedure.getQuantity(),
                procedure.getUnitPrice(),
                procedure.getNote()
        );
    }

    private PrescriptionDetailResponse toPrescriptionDetail(Prescription prescription) {
        List<PrescriptionItemResponse> items = prescriptionItemRepository.findByPrescriptionId(prescription.getId())
                .stream()
                .map(this::toPrescriptionItemResponse)
                .toList();
        Treatment treatment = prescription.getTreatment();
        Patient patient = treatment == null ? null : treatment.getPatient();
        Staff doctor = prescription.getDoctor() != null
                ? prescription.getDoctor()
                : treatment == null ? null : treatment.getDoctor();
        return new PrescriptionDetailResponse(
                prescription.getId(),
                prescription.getCode(),
                treatment == null ? null : treatment.getId(),
                treatment == null ? null : treatment.getDiagnosis(),
                toPatientInfo(patient),
                toStaffInfo(doctor),
                prescription.getPrescribedAt(),
                prescription.getAdvice(),
                prescription.getReExaminationDate(),
                prescription.getNote(),
                items.size(),
                items,
                prescription.getCreatedAt(),
                prescription.getModifiedAt()
        );
    }

    private PrescriptionItemResponse toPrescriptionItemResponse(PrescriptionItem item) {
        Medicine medicine = item.getMedicine();
        return new PrescriptionItemResponse(
                item.getId(),
                medicine == null ? null : medicine.getId(),
                medicine == null ? null : medicine.getCode(),
                medicine == null ? null : medicine.getName(),
                medicine == null ? null : medicine.getUnit(),
                item.getQuantity(),
                item.getDosage(),
                item.getFrequency(),
                item.getDuration(),
                item.getInstruction(),
                item.getCreatedAt(),
                item.getModifiedAt()
        );
    }

    private PatientInfoResponse toPatientInfo(Patient patient) {
        if (patient == null) {
            return null;
        }
        User user = patient.getUser();
        return new PatientInfoResponse(
                patient.getId(),
                patient.getCode(),
                patient.getFullName(),
                patient.getPhone() != null ? patient.getPhone() : patient.getGuardianPhone(),
                user == null ? null : user.getUsername(),
                patient.getGender(),
                patient.getDob(),
                patient.getAddress(),
                user == null ? Boolean.TRUE : user.getIsActive(),
                patient.getGuardianName(),
                patient.getGuardianPhone()
        );
    }

    private StaffInfoResponse toStaffInfo(Staff staff) {
        if (staff == null) {
            return null;
        }
        return new StaffInfoResponse(
                staff.getId(),
                staff.getCode(),
                staff.getFullName(),
                staff.getPhone(),
                staff.getEmail()
        );
    }
}
