package cd.beapi.security.exception;

import cd.beapi.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI(),
                "Unauthorized",
                getDetailedMessage(authException));

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String getDetailedMessage(AuthenticationException ex) {
        String message = ex.getMessage();

        // Customize message cho từng loại lỗi
        if (message.contains("Jwt expired")) {
            return "Token đã hết hạn";
        } else if (message.contains("Invalid JWT")) {
            return "Token không hợp lệ";
        } else if (message.contains("token_revoked")) {
            return "Token đã bị thu hồi";
        } else if (message.contains("Full authentication is required")) {
            return "Vui lòng đăng nhập để tiếp tục";
        }

        return "Xác thực thất bại";
    }
}
