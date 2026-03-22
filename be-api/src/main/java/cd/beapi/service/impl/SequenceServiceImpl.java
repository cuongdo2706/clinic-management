package cd.beapi.service.impl;

import cd.beapi.repository.jpa.SequenceRepository;
import cd.beapi.service.SequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SequenceServiceImpl implements SequenceService {
    private final SequenceRepository sequenceRepository;

    @Override
    public Long getNextValue(String name) {
        return sequenceRepository.incrementAndGet(name);
    }

    @Override
    public String generateDentistCode() {
        return format("NS", "dentist");
    }

    @Override
    public String generatePatientCode() {
        return format("BN", "patient");
    }

    @Override
    public String generateAppointmentCode() {
        return format("APT", "appointment");
    }

    @Override
    public String generateVisitRegistrationCode() {
        return format("VR", "visit_registration");
    }

    @Override
    public String generateMedicalRecordCode() {
        return format("MR", "medical_record");
    }

    @Override
    public String generatePrescriptionCode() {
        return format("PRE", "prescription");
    }

    @Override
    public String generateInvoiceCode() {
        return format("INV", "invoice");
    }

    @Override
    public String generatePaymentCode() {
        return format("PAY", "payment");
    }

    private String format(String prefix, String sequenceName) {
        Long seq = getNextValue(sequenceName);
        return String.format("%s%06d", prefix, seq);
    }
}
