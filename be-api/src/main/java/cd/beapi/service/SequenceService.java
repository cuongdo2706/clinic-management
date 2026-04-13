package cd.beapi.service;

public interface SequenceService {
    Long getNextValue(String name);
    String generateDentistCode();
    String generatePatientCode();
    String generateMedicineCode();
    String generateAppointmentCode();
    String generateMedicalRecordCode();
    String generatePrescriptionCode();
}
