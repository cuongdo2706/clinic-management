package cd.beapi.dto.response;

public record ClientPrescriptionItemResponse(
        Long medicineId,
        String medicineName,
        Integer quantity,
        String dosage,
        String frequency,
        String duration,
        String instruction
) {
}
