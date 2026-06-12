package cd.beapi.service;

public interface SequenceService {
    Long getNextValue(String name);
    String generateStaffCode();
    String generatePatientCode();
    String generateMedicineCode();
    String generateAppointmentCode();
    String generatePrescriptionCode();
    String generateProcedureCode();
    String generateProcedureCategoryCode();
}
