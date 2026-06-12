import {inject, Injectable} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PageData} from "../model/response/page-data";
import {ProcedureCategoryResponse} from "../model/response/procedure-category-response";

@Injectable({
    providedIn: 'root',
})
export class ProcedureCategoryService {
    private readonly url = `${ENV.API_BASE_URL}/clinic/procedure-categories`;
    private readonly http = inject(HttpClient);

    search(request: { page: number; size: number }): Observable<SuccessResponse<PageData<ProcedureCategoryResponse>>> {
        return this.http.post<SuccessResponse<PageData<ProcedureCategoryResponse>>>(`${this.url}/search`, request);
    }
}

