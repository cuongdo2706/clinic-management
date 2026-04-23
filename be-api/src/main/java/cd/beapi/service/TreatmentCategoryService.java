package cd.beapi.service;

import cd.beapi.dto.request.CreateTreatmentCategoryRequest;
import cd.beapi.dto.request.SearchTreatmentCategoryRequest;
import cd.beapi.dto.request.UpdateTreatmentCategoryRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.TreatmentCategoryResponse;

public interface TreatmentCategoryService {
    TreatmentCategoryResponse findById(Long id);
    PageData<TreatmentCategoryResponse> search(SearchTreatmentCategoryRequest request);
    TreatmentCategoryResponse save(CreateTreatmentCategoryRequest request);
    TreatmentCategoryResponse update(Long id, UpdateTreatmentCategoryRequest request);
    void delete(Long id);
}

