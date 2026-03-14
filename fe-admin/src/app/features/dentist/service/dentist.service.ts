import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ENV} from "../../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../../../core/model/response/success-response";
import {DentistRequest, DentistResponse} from "../model/dentist.model";

@Injectable({
    providedIn: 'root',
})
export class DentistService {
    private readonly url = ENV.API_BASE_URL + "dentists";
    private readonly http = inject(HttpClient);

    getAll(page = 0, size = 10, keyword = ''): Observable<SuccessResponse<any>> {
        let params = new HttpParams()
            .set('page', page)
            .set('size', size);
        if (keyword) {
            params = params.set('keyword', keyword);
        }
        return this.http.get<SuccessResponse<any>>(this.url, {params});
    }

    getById(id: string): Observable<SuccessResponse<DentistResponse>> {
        return this.http.get<SuccessResponse<DentistResponse>>(`${this.url}/${id}`);
    }

    create(request: DentistRequest): Observable<SuccessResponse<DentistResponse>> {
        return this.http.post<SuccessResponse<DentistResponse>>(this.url, request);
    }

    update(id: string, request: DentistRequest): Observable<SuccessResponse<DentistResponse>> {
        return this.http.put<SuccessResponse<DentistResponse>>(`${this.url}/${id}`, request);
    }

    delete(id: string): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }
}

