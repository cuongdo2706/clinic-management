import {inject, Injectable} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PageData} from "../model/response/page-data";
import {TreatmentCategoryResponse} from "../model/response/treatment-category-response";

@Injectable({
    providedIn: 'root',
})
export class TreatmentCategoryService {
    private readonly url = ENV.API_BASE_URL + "clinic/treatment-categories";
    private readonly http = inject(HttpClient);

    search(request: { page: number; size: number }): Observable<SuccessResponse<PageData<TreatmentCategoryResponse>>> {
        return this.http.post<SuccessResponse<PageData<TreatmentCategoryResponse>>>(`${this.url}/search`, request);
    }
}

