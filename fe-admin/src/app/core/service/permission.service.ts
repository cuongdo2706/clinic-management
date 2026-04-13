import {inject, Injectable} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PermissionResponse} from "../model/response/permission-response";
import {UpdatePermissionRequest} from "../model/request/update-permission-request";

@Injectable({
    providedIn: 'root',
})
export class PermissionService {
    private readonly url = ENV.API_BASE_URL + "clinic/permissions";
    private readonly http = inject(HttpClient);


    findByRoleId(roleId: number): Observable<SuccessResponse<PermissionResponse>> {
        return this.http.get<SuccessResponse<PermissionResponse>>(`${this.url}/${roleId}`);
    }

    update(request: UpdatePermissionRequest): Observable<SuccessResponse<void>> {
        return this.http.put<SuccessResponse<void>>(this.url, request);
    }
}

