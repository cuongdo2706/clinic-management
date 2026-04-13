import {inject, Injectable} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {RoleResponse} from "../model/response/role-response";

@Injectable({
  providedIn: 'root',
})
export class RoleService {
    private readonly url = ENV.API_BASE_URL + "clinic/roles";
    private readonly http = inject(HttpClient);
    
    findAll(): Observable<SuccessResponse<RoleResponse[]>> {
        return this.http.get<SuccessResponse<RoleResponse[]>>(this.url);
    }
}
