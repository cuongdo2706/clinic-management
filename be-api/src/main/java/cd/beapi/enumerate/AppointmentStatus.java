package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AppointmentStatus {
    // === BOOKING STAGE ===
    PENDING("Chờ xác nhận"),       // Đặt online, chờ clinic duyệt
    CONFIRMED("Đã xác nhận"),      // Clinic đã xác nhận lịch hẹn

    // === CHECK-IN & TREATMENT STAGE ===
    IN_QUEUE("Đang chờ khám"),     // BN đã đến, được xếp số hàng chờ (walk-in bắt đầu từ đây)
    IN_PROGRESS("Đang khám"),      // Bác sĩ đang tiếp nhận khám

    // === COMPLETION STAGE ===
    DONE("Hoàn thành"),            // Khám xong, chờ thanh toán

    // === CANCELLATION ===
    CANCELLED("Đã hủy"),           // Hủy bởi bệnh nhân hoặc clinic
    NO_SHOW("Không đến");          // Quá giờ hẹn, bệnh nhân không xuất hiện

    String label;

    AppointmentStatus(String label) {
        this.label = label;
    }
}
