package cd.beapi.service;

public interface SequenceService {
    Long getNextValue(String name);

    String generateDentistCode();
}
