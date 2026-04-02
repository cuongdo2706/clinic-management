package cd.beapi.service;

import cd.beapi.dto.request.CreatePatientRequest;
import cd.beapi.dto.request.SearchPatientRequest;
import cd.beapi.dto.request.UpdatePatientRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.PatientResponse;

public interface PatientService {
    PatientResponse findById(Long id);
    PageData<PatientResponse>search(SearchPatientRequest filterPatientRequest);
    PatientResponse save(CreatePatientRequest createPatientRequest);
    PatientResponse update(Long id, UpdatePatientRequest updatePatientRequest);
    void delete(Long id);
}
