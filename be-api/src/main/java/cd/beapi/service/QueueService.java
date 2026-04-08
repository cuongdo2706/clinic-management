package cd.beapi.service;

public interface QueueService {
    /**
     * Trả về số thứ tự tiếp theo trong ngày hôm nay.
     * Key Redis tự hết hạn lúc 00:00 ngày hôm sau → tự động reset.
     */
    int nextQueueNumber();
}

