package cd.beapi.service;

import cd.beapi.dto.request.GuestBookingRequest;
import cd.beapi.dto.response.AppointmentResponse;

public interface AppointmentService {

    /**
     * Đặt lịch không cần tài khoản.
     * - Tra cứu Patient theo SĐT → reuse nếu đã tồn tại, tạo mới nếu chưa có.
     * - Tạo Appointment với bookingChannel = ONLINE_GUEST, status = PENDING.
     */
    AppointmentResponse guestBooking(GuestBookingRequest request);
}

