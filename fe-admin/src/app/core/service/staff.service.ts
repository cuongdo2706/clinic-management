import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ENV} from "../../environment";
import {SuccessResponse} from "../model/response/success-response";
import {PageData} from "../model/response/page-data";
import {StaffResponse} from "../model/response/staff-response";
import {SearchStaffRequest} from "../model/request/search-staff-request";
import {CreateStaffRequest} from "../model/request/create-staff-request";
import {UpdateStaffRequest} from "../model/request/update-staff-request";

@Injectable({
    providedIn: 'root',
})
export class StaffService {
    private readonly url = ENV.API_BASE_URL + "clinic/staffs";
    private readonly http = inject(HttpClient);

    search(request: SearchStaffRequest): Observable<SuccessResponse<PageData<StaffResponse>>> {
        return this.http.post<SuccessResponse<PageData<StaffResponse>>>(`${this.url}/search`, request);
    }

    findById(id: number): Observable<SuccessResponse<StaffResponse>> {
        return this.http.get<SuccessResponse<StaffResponse>>(`${this.url}/${id}`);
    }

    create(request: CreateStaffRequest, file: File | null): Observable<SuccessResponse<StaffResponse>> {
        return this.http.post<SuccessResponse<StaffResponse>>(this.url, this.toFormData(request, file));
    }

    update(id: number, request: UpdateStaffRequest, file: File | null): Observable<SuccessResponse<StaffResponse>> {
        return this.http.put<SuccessResponse<StaffResponse>>(`${this.url}/${id}`, this.toFormData(request, file));
    }

    delete(id: number): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }

    private toFormData(request: CreateStaffRequest | UpdateStaffRequest, file: File | null): FormData {
        const formData = new FormData();
        formData.append("request", new Blob([JSON.stringify(request)], {type: "application/json"}));
        if (file) {
            formData.append("file", file);
        }
        return formData;
    }
}
