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
        return format("NV", SequenceName.DENTIST,8);
    }

    @Override
    public String generatePatientCode() {
        return format("BN", SequenceName.PATIENT,8);
    }

    @Override
    public String generateMedicineCode() {
        return format("TH", SequenceName.MEDICINE,8);
    }

    @Override
    public String generateAppointmentCode() {
        return format("LH", SequenceName.APPOINTMENT,8);
    }


    @Override
    public String generateMedicalRecordCode() {
        return format("BA", SequenceName.MEDICAL_RECORD,8);
    }

    @Override
    public String generatePrescriptionCode() {
        return format("DT", SequenceName.PRESCRIPTION,8);
    }

    @Override
    public String generateTreatmentCode() {
        return format("DV",SequenceName.TREATMENT,8);
    }

    @Override
    public String generateTreatmentCategoryCode() {
        return format("DMDV",SequenceName.TREATMENT_CATEGORY,3);
    }

    private String format(String prefix, SequenceName sequenceName,int numberLength) {
        if (numberLength <= 0) {
            throw new IllegalArgumentException("numberLength must be > 0");
        }
        Long seq = getNextValue(sequenceName.name());
        String pattern = "%s%0" + numberLength + "d";
        return String.format(pattern, prefix, seq);
    }
}
