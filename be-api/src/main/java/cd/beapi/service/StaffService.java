package cd.beapi.service;

import cd.beapi.dto.request.CreateStaffRequest;
import cd.beapi.dto.request.SearchStaffRequest;
import cd.beapi.dto.request.UpdateStaffRequest;
import cd.beapi.dto.request.UpdateStaffStatusRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.StaffResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StaffService {
    PageData<StaffResponse> search(SearchStaffRequest searchStaffRequest);

    List<PublicDentistResponse> findDentistOptions();

    StaffResponse findCurrentDentist(String username);

    StaffResponse findById(Long id);

    StaffResponse save(CreateStaffRequest createStaffRequest, MultipartFile file);

    StaffResponse update(Long id, UpdateStaffRequest updateStaffRequest, MultipartFile file);

    StaffResponse updateStatus(Long id, UpdateStaffStatusRequest updateStaffStatusRequest);
}
