import {inject, Injectable} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PageData} from "../model/response/page-data";
import {SearchMedicineRequest} from "../model/request/search-medicine-request";
import {MedicineResponse} from "../model/response/medicine-response";
import {CreateMedicineRequest} from "../model/request/create-medicine-request";
import {UpdateMedicineRequest} from "../model/request/update-medicine-request";

@Injectable({
    providedIn: 'root',
})
export class MedicineService {
    private readonly url = ENV.API_BASE_URL + "clinic/medicines";
    private readonly http = inject(HttpClient);

    search(searchMedicineRequest: SearchMedicineRequest): Observable<SuccessResponse<PageData<MedicineResponse>>> {
        return this.http.post<SuccessResponse<PageData<MedicineResponse>>>(`${this.url}/search`, searchMedicineRequest);
    }

    findById(id: string): Observable<SuccessResponse<MedicineResponse>> {
        return this.http.get<SuccessResponse<MedicineResponse>>(`${this.url}/${id}`);
    }

    create(createMedicineRequest: CreateMedicineRequest): Observable<SuccessResponse<MedicineResponse>> {
        return this.http.post<SuccessResponse<MedicineResponse>>(this.url, createMedicineRequest);
    }

    update(id: string, updateMedicineRequest: UpdateMedicineRequest): Observable<SuccessResponse<MedicineResponse>> {
        return this.http.put<SuccessResponse<MedicineResponse>>(`${this.url}/${id}`, updateMedicineRequest);
    }

    delete(id: string): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }

    exportExcel(): Observable<Blob> {
        return this.http.get(`${this.url}/export`, {responseType: 'blob'});
    }
}

