package cd.beapi.service;

import cd.beapi.dto.request.CreateProcedureCategoryRequest;
import cd.beapi.dto.request.SearchProcedureCategoryRequest;
import cd.beapi.dto.request.UpdateProcedureCategoryRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.ProcedureCategoryResponse;

public interface ProcedureCategoryService {
    ProcedureCategoryResponse findById(Long id);
    PageData<ProcedureCategoryResponse> search(SearchProcedureCategoryRequest request);
    ProcedureCategoryResponse save(CreateProcedureCategoryRequest request);
    ProcedureCategoryResponse update(Long id, UpdateProcedureCategoryRequest request);
    void delete(Long id);
}

