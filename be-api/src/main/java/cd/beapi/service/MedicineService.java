package cd.beapi.service;

import cd.beapi.dto.request.CreateMedicineRequest;
import cd.beapi.dto.request.SearchMedicineRequest;
import cd.beapi.dto.request.UpdateMedicineRequest;
import cd.beapi.dto.response.MedicineResponse;
import cd.beapi.dto.response.PageData;
import org.springframework.core.io.Resource;

public interface MedicineService {
    MedicineResponse findById(Long id);
    PageData<MedicineResponse> search(SearchMedicineRequest searchMedicineRequest);
    MedicineResponse save(CreateMedicineRequest createMedicineRequest);
    MedicineResponse update(Long id, UpdateMedicineRequest updateMedicineRequest);
    void delete(Long id);
    Resource exportExcel();
}
