package cd.beapi.dto.response;

import java.util.List;

public record PageData<T>(List<T> content, Integer page, Integer size, Long totalElements, Integer totalPages) {
}
