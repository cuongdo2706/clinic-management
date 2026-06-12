package cd.beapi.service.impl;

import cd.beapi.dto.request.ClientBookingRequest;
import cd.beapi.dto.request.UpdatePatientRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.ClientHealthRecordResponse;
import cd.beapi.dto.response.ClientHealthRecordProcedureResponse;
import cd.beapi.dto.response.ClientPrescriptionItemResponse;
import cd.beapi.dto.response.ClientPrescriptionResponse;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.PublicProcedureResponse;
import cd.beapi.entity.Patient;
import cd.beapi.entity.Prescription;
import cd.beapi.entity.PrescriptionItem;
import cd.beapi.entity.Procedure;
import cd.beapi.entity.Appointment;
import cd.beapi.entity.Treatment;
import cd.beapi.entity.TreatmentProcedure;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.AppointmentMapper;
import cd.beapi.mapper.PatientMapper;
import cd.beapi.repository.jpa.AppointmentRepository;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.repository.jpa.PrescriptionItemRepository;
import cd.beapi.repository.jpa.TreatmentProcedureRepository;
import cd.beapi.repository.jpa.TreatmentRepository;
import cd.beapi.service.AppointmentService;
import cd.beapi.service.ClientPortalService;
import cd.beapi.service.PatientService;
import cd.beapi.service.PublicBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientPortalServiceImpl implements ClientPortalService {
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentRepository treatmentRepository;
    private final TreatmentProcedureRepository treatmentProcedureRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final AppointmentService appointmentService;
    private final PublicBookingService publicBookingService;
    private final PatientService patientService;
    private final PatientMapper patientMapper;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    @Override
    public PatientResponse getProfile(String username) {
        return patientMapper.toPatientResponse(findPatientByUsername(username));
    }

    @Transactional
    @Override
    public PatientResponse updateProfile(String username, UpdatePatientRequest request) {
        Patient patient = findPatientByUsername(username);
        return patientService.update(patient.getId(), request);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PublicDentistResponse> findDentists() {
        return publicBookingService.findDentists();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PublicProcedureResponse> findProcedures() {
        return publicBookingService.findProcedures();
    }

    @Transactional(readOnly = true)
    @Override
    public AvailableSlotResponse getAvailableSlots(Long dentistId, Long procedureId, LocalDate date, Integer durationMinutes) {
        return publicBookingService.getAvailableSlots(dentistId, procedureId, date, durationMinutes);
    }

    @Transactional
    @Override
    public AppointmentResponse book(String username, ClientBookingRequest request) {
        Patient patient = findPatientByUsername(username);
        return appointmentService.saveForPatient(patient.getId(), request);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AppointmentResponse> getAppointments(String username) {
        Patient patient = findPatientByUsername(username);
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patient.getId())
                .stream()
                .map(appointmentMapper::toAppointmentResponse)
                .toList();
    }

    @Transactional
    @Override
    public AppointmentResponse cancelAppointment(String username, Long appointmentId) {
        Patient patient = findPatientByUsername(username);
        Appointment appointment = appointmentRepository.findByIdWithRelations(appointmentId).orElseThrow(
                () -> new AppException("Cannot find appointment with id: " + appointmentId, HttpStatus.BAD_REQUEST)
        );
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patient.getId())) {
            throw new AppException("You are not allowed to update this appointment", HttpStatus.FORBIDDEN);
        }
        return appointmentService.cancel(appointmentId, username);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientHealthRecordResponse> getHealthRecords(String username) {
        Patient patient = findPatientByUsername(username);
        return treatmentRepository.findByPatientIdOrderByTreatmentDateDesc(patient.getId())
                .stream()
                .map(this::toHealthRecord)
                .toList();
    }

    private Patient findPatientByUsername(String username) {
        return patientRepository.findByUsername(username).orElseThrow(
                () -> new AppException("Cannot find patient profile for current account", HttpStatus.BAD_REQUEST)
        );
    }

    private ClientHealthRecordResponse toHealthRecord(Treatment treatment) {
        List<ClientHealthRecordProcedureResponse> procedures = treatmentProcedureRepository
                .findByTreatmentId(treatment.getId())
                .stream()
                .map(this::toProcedure)
                .toList();
        ClientPrescriptionResponse prescription = treatment.getPrescription() == null
                ? null
                : toPrescription(treatment.getPrescription());
        return new ClientHealthRecordResponse(
                treatment.getId(),
                resolveHealthRecordCode(treatment),
                treatment.getAppointment() == null ? null : treatment.getAppointment().getSymptom(),
                treatment.getDiagnosis(),
                treatment.getNote(),
                treatment.getNote(),
                null,
                treatment.getCreatedAt(),
                treatment.getAppointment() == null ? null : treatment.getAppointment().getAppointmentDate(),
                treatment.getDoctor() == null ? null : treatment.getDoctor().getFullName(),
                procedures,
                prescription
        );
    }

    private ClientHealthRecordProcedureResponse toProcedure(TreatmentProcedure procedure) {
        Procedure source = procedure.getProcedure();
        return new ClientHealthRecordProcedureResponse(
                source == null ? null : source.getId(),
                source == null ? null : source.getName(),
                procedure.getQuantity(),
                procedure.getUnitPrice(),
                procedure.getNote()
        );
    }

    private String resolveHealthRecordCode(Treatment treatment) {
        if (treatment.getAppointment() != null && treatment.getAppointment().getCode() != null) {
            return treatment.getAppointment().getCode();
        }
        return String.format("DT%08d", treatment.getId());
    }

    private ClientPrescriptionResponse toPrescription(Prescription prescription) {
        List<ClientPrescriptionItemResponse> items = prescriptionItemRepository
                .findByPrescriptionId(prescription.getId())
                .stream()
                .map(this::toPrescriptionItem)
                .toList();
        return new ClientPrescriptionResponse(
                prescription.getId(),
                prescription.getCode(),
                prescription.getNote(),
                prescription.getCreatedAt(),
                items
        );
    }

    private ClientPrescriptionItemResponse toPrescriptionItem(PrescriptionItem item) {
        return new ClientPrescriptionItemResponse(
                item.getMedicine() == null ? null : item.getMedicine().getId(),
                item.getMedicine() == null ? null : item.getMedicine().getName(),
                item.getQuantity(),
                item.getDosage(),
                item.getFrequency(),
                item.getDuration(),
                item.getInstruction()
        );
    }
}
