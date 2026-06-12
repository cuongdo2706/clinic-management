import {inject, Injectable} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PageData} from "../model/response/page-data";
import {ProcedureResponse} from "../model/response/procedure-response";
import {SearchProcedureRequest} from "../model/request/search-procedure-request";
import {CreateProcedureRequest} from "../model/request/create-procedure-request";
import {UpdateProcedureRequest} from "../model/request/update-procedure-request";

@Injectable({
    providedIn: 'root',
})
export class ProcedureService {
    private readonly url = `${ENV.API_BASE_URL}/clinic/procedures`;
    private readonly http = inject(HttpClient);

    search(request: SearchProcedureRequest): Observable<SuccessResponse<PageData<ProcedureResponse>>> {
        return this.http.post<SuccessResponse<PageData<ProcedureResponse>>>(`${this.url}/search`, request);
    }

    findById(id: number): Observable<SuccessResponse<ProcedureResponse>> {
        return this.http.get<SuccessResponse<ProcedureResponse>>(`${this.url}/${id}`);
    }

    create(request: CreateProcedureRequest): Observable<SuccessResponse<ProcedureResponse>> {
        return this.http.post<SuccessResponse<ProcedureResponse>>(this.url, request);
    }

    update(id: number, request: UpdateProcedureRequest): Observable<SuccessResponse<ProcedureResponse>> {
        return this.http.put<SuccessResponse<ProcedureResponse>>(`${this.url}/${id}`, request);
    }

    delete(id: number): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }
}

