package cd.beapi.service;

public interface SequenceService {
    Long getNextValue(String name);
    String generateDentistCode();
    String generatePatientCode();
    String generateAppointmentCode();
    String generateVisitRegistrationCode();
    String generateMedicalRecordCode();
    String generatePrescriptionCode();
    String generateInvoiceCode();
    String generatePaymentCode();
}
