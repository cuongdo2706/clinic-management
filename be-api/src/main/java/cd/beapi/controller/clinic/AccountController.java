package cd.beapi.controller.clinic;

import cd.beapi.dto.request.SearchAccountRequest;
import cd.beapi.dto.request.UpdateAccountRoleRequest;
import cd.beapi.dto.request.UpdateAccountStatusRequest;
import cd.beapi.dto.response.AccountResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.ResetPasswordResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/search")
    public SuccessResponse<PageData<AccountResponse>> search(@Valid @RequestBody SearchAccountRequest request) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), accountService.search(request));
    }

    @PatchMapping("/{id}/status")
    public SuccessResponse<AccountResponse> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateAccountStatusRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update account status successfully", Instant.now(), accountService.updateStatus(id, request));
    }

    @PutMapping("/{id}/role")
    public SuccessResponse<AccountResponse> updateRole(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateAccountRoleRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update account role successfully", Instant.now(), accountService.updateRole(id, request));
    }

    @PostMapping("/{id}/reset-password")
    public SuccessResponse<ResetPasswordResponse> resetPassword(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Reset password successfully", Instant.now(), accountService.resetPassword(id));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Delete account successfully", Instant.now(), null);
    }
}
