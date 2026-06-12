import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ENV} from '../../environment';
import {
    AccountResponse,
    AccountStatus,
    ResetPasswordResponse,
    SearchAccountRequest,
} from '../model/account';
import {PageData} from '../model/response/page-data';
import {SuccessResponse} from '../model/response/success-response';

@Injectable({
    providedIn: 'root',
})
export class AccountService {
    private readonly url = `${ENV.API_BASE_URL}/clinic/accounts`;
    private readonly http = inject(HttpClient);

    search(request: SearchAccountRequest): Observable<SuccessResponse<PageData<AccountResponse>>> {
        return this.http.post<SuccessResponse<PageData<AccountResponse>>>(`${this.url}/search`, request);
    }

    updateStatus(id: number, status: AccountStatus): Observable<SuccessResponse<AccountResponse>> {
        return this.http.patch<SuccessResponse<AccountResponse>>(`${this.url}/${id}/status`, {status});
    }

    updateRole(id: number, roleCode: string): Observable<SuccessResponse<AccountResponse>> {
        return this.http.put<SuccessResponse<AccountResponse>>(`${this.url}/${id}/role`, {roleCode});
    }

    resetPassword(id: number): Observable<SuccessResponse<ResetPasswordResponse>> {
        return this.http.post<SuccessResponse<ResetPasswordResponse>>(`${this.url}/${id}/reset-password`, {});
    }

    delete(id: number): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }
}
