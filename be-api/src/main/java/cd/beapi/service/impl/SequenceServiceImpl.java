package cd.beapi.service.impl;

import cd.beapi.enumerate.SequenceName;
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
        return format("NV", SequenceName.DENTIST);
    }

    @Override
    public String generatePatientCode() {
        return format("BN", SequenceName.PATIENT);
    }

    @Override
    public String generateMedicineCode() {
        return format("TH", SequenceName.MEDICINE);
    }

    @Override
    public String generateAppointmentCode() {
        return format("LH", SequenceName.APPOINTMENT);
    }


    @Override
    public String generateMedicalRecordCode() {
        return format("BA", SequenceName.MEDICAL_RECORD);
    }

    @Override
    public String generatePrescriptionCode() {
        return format("DT", SequenceName.PRESCRIPTION);
    }

    private String format(String prefix, SequenceName sequenceName) {
        Long seq = getNextValue(sequenceName.name());
        return String.format("%s%08d", prefix, seq);
    }
}
