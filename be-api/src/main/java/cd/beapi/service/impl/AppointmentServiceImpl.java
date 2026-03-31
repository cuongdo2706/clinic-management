package cd.beapi.service.impl;

import cd.beapi.dto.request.GuestBookingRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.entity.Appointment;
import cd.beapi.entity.Patient;
import cd.beapi.entity.Staff;
import cd.beapi.enumerate.AppointmentStatus;
import cd.beapi.enumerate.BookingChannel;
import cd.beapi.repository.jpa.AppointmentRepository;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.service.AppointmentService;
import cd.beapi.service.SequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final SequenceService sequenceService;

    @Override
    @Transactional
    public AppointmentResponse guestBooking(GuestBookingRequest request) {

        // 1. Tra cứu Patient theo SĐT
        //    - Nếu đã tồn tại (từng walk-in hoặc đặt lịch trước đó): reuse
        //    - Nếu chưa có: tạo mới với user = null
        Patient patient = patientRepository.findByPhone(request.getPhone())
                .orElseGet(() -> createGuestPatient(request));

        // 2. Resolve bác sĩ nếu khách chọn (optional)
        Staff dentist = null;
        if (request.getDentistId() != null) {
            dentist = staffRepository.findById(request.getDentistId())
                    .orElse(null);
        }

        // 3. Tạo Appointment
        Appointment appointment = Appointment.builder()
                .code(sequenceService.generateAppointmentCode())
                .patient(patient)
                .staff(dentist)
                .appointmentDate(request.getAppointmentDate())
                .symptom(request.getSymptom())
                .note(request.getNote())
                .status(AppointmentStatus.PENDING)
                .bookingChannel(BookingChannel.ONLINE_GUEST)
                .build();

        appointment = appointmentRepository.save(appointment);

        return mapToResponse(appointment);
    }

    // Tạo Patient mới cho khách vãng lai điền form
    private Patient createGuestPatient(GuestBookingRequest request) {
        Patient patient = Patient.builder()
                .code(sequenceService.generatePatientCode())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dob(request.getDob())
                .gender(request.getGender())
                .user(null) // chưa có tài khoản
                .build();
        return patientRepository.save(patient);
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getCode(),
                a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : null,
                null,
                null,
                a.getStatus() != null ? a.getStatus().name() : null,
                a.getStatus() != null ? a.getStatus().getLabel() : null,
                a.getNote(),
                a.getPatient() != null ? a.getPatient().getFullName() : null,
                a.getPatient() != null ? a.getPatient().getCode() : null,
                a.getStaff() != null ? a.getStaff().getFullName() : null,
                a.getStaff() != null ? a.getStaff().getCode() : null,
                a.getCreatedAt()
        );
    }
}


