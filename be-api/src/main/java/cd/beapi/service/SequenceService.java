package cd.beapi.service;

public interface SequenceService {
    Long getNextValue(String name);
    String generateStaffCode();
    String generatePatientCode();
    String generateMedicineCode();
    String generateAppointmentCode();
    String generateBookingRequestCode();
    String generateMedicalRecordCode();
    String generatePrescriptionCode();
    String generateTreatmentCode();
    String generateTreatmentCategoryCode();
}
