package cd.beapi.service.impl;

import cd.beapi.repository.jpa.SequenceRepository;
import cd.beapi.service.SequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SequenceServiceImpl implements SequenceService {
    private static final String DENTIST_SEQUENCE_NAME = "dentist";
    private static final String DENTIST_PREFIX = "NS";
    private static final String PATIENT_SEQUENCE_NAME = "patient";
    private final SequenceRepository sequenceRepository;

    @Override
    public Long getNextValue(String name) {
        return sequenceRepository.incrementAndGet(name);
    }

    @Override
    public String generateDentistCode() {
        Long seg = getNextValue(DENTIST_SEQUENCE_NAME);
        return String.format("%s%06d", DENTIST_PREFIX, seg);
    }
}
