package cd.beapi.service;

import cd.beapi.dto.request.CreateTreatmentRequest;
import cd.beapi.dto.request.SearchTreatmentRequest;
import cd.beapi.dto.request.UpdateTreatmentRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.TreatmentResponse;

public interface TreatmentService {
    TreatmentResponse findById(Long id);
    PageData<TreatmentResponse> search(SearchTreatmentRequest request);
    TreatmentResponse save(CreateTreatmentRequest request);
    TreatmentResponse update(Long id, UpdateTreatmentRequest request);
    void delete(Long id);
}

