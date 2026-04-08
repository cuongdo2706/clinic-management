package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateMedicineRequest;
import cd.beapi.dto.request.SearchMedicineRequest;
import cd.beapi.dto.request.UpdateMedicineRequest;
import cd.beapi.dto.response.MedicineResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.entity.Medicine;
import cd.beapi.entity.QMedicine;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.MedicineMapper;
import cd.beapi.repository.jpa.MedicineRepository;
import cd.beapi.service.MedicineService;
import cd.beapi.service.SequenceService;
import cd.beapi.utility.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {
    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final SequenceService sequenceService;

    @Override
    public MedicineResponse findById(Long id) {
        return medicineMapper.toMedicineResponse(
                medicineRepository.findById(id).orElseThrow(
                        () -> new AppException("Cannot find medicine with id: " + id, HttpStatus.BAD_REQUEST)
                )
        );
    }

    @Override
    public PageData<MedicineResponse> search(SearchMedicineRequest searchMedicineRequest) {
        QMedicine m = QMedicine.medicine;
        BooleanBuilder whereClause = new BooleanBuilder();
        if (StringUtils.hasText(searchMedicineRequest.getCodeKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", m.code)
                            .like("%" + StringUtil.normalizeKeyword(searchMedicineRequest.getCodeKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(searchMedicineRequest.getNameKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", m.name)
                            .like("%" + StringUtil.normalizeKeyword(searchMedicineRequest.getNameKeyword()) + "%")
            );
        }
        Sort sort = switch (searchMedicineRequest.getSortBy()) {
            case null -> Sort.by("createdAt").descending();
            case NAME -> Sort.by("name");
            case NAME_DESC -> Sort.by("name").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(searchMedicineRequest.getPage(), searchMedicineRequest.getSize(), sort);
        Page<Medicine>pages=medicineRepository.findAll(whereClause,pageable);
        return new PageData<>(
                medicineMapper.toMedicineResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()
        );
    }

    @Transactional
    @Override
    public MedicineResponse save(CreateMedicineRequest createMedicineRequest) {
        Medicine medicine = Medicine.builder()
                .name(createMedicineRequest.get)
                .build();
        return null;
    }

    @Transactional
    @Override
    public MedicineResponse update(Long id, UpdateMedicineRequest updateMedicineRequest) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Resource exportExcel() {
        return null;
    }
}
