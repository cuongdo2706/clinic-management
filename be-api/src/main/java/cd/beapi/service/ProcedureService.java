package cd.beapi.service;

import cd.beapi.dto.request.CreateProcedureRequest;
import cd.beapi.dto.request.SearchProcedureRequest;
import cd.beapi.dto.request.UpdateProcedureRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.ProcedureResponse;

public interface ProcedureService {
    ProcedureResponse findById(Long id);
    PageData<ProcedureResponse> search(SearchProcedureRequest request);
    ProcedureResponse save(CreateProcedureRequest request);
    ProcedureResponse update(Long id, UpdateProcedureRequest request);
    void delete(Long id);
}

