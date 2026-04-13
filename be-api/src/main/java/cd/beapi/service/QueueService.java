package cd.beapi.service;

import java.time.LocalDate;

public interface QueueService {
    /**
     * Trả về số thứ tự tiếp theo cho một ngày cụ thể.
     * Walk-in → truyền LocalDate.now()
     * Đặt lịch trước → truyền ngày hẹn
     * Key Redis: queue:{date}, tự hết hạn sau ngày đó 1 ngày.
     */
    int nextQueueNumberForDate(LocalDate date);
}

