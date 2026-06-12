package cd.beapi.service;

import cd.beapi.dto.request.SearchAccountRequest;
import cd.beapi.dto.request.UpdateAccountRoleRequest;
import cd.beapi.dto.request.UpdateAccountStatusRequest;
import cd.beapi.dto.response.AccountResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.ResetPasswordResponse;

public interface AccountService {
    PageData<AccountResponse> search(SearchAccountRequest request);

    AccountResponse updateStatus(Long id, UpdateAccountStatusRequest request);

    AccountResponse updateRole(Long id, UpdateAccountRoleRequest request);

    ResetPasswordResponse resetPassword(Long id);

    void delete(Long id);
}
