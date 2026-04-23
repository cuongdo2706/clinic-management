package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateTreatmentCategoryRequest;
import cd.beapi.dto.request.SearchTreatmentCategoryRequest;
import cd.beapi.dto.request.UpdateTreatmentCategoryRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.TreatmentCategoryResponse;
import cd.beapi.entity.QTreatmentCategory;
import cd.beapi.entity.TreatmentCategory;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.TreatmentCategoryMapper;
import cd.beapi.repository.jpa.TreatmentCategoryRepository;
import cd.beapi.service.SequenceService;
import cd.beapi.service.TreatmentCategoryService;
import cd.beapi.utility.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
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
public class TreatmentCategoryServiceImpl implements TreatmentCategoryService {

    private final TreatmentCategoryRepository treatmentCategoryRepository;
    private final TreatmentCategoryMapper treatmentCategoryMapper;
    private final SequenceService sequenceService;

    @Transactional(readOnly = true)
    @Override
    public TreatmentCategoryResponse findById(Long id) {
        return treatmentCategoryMapper.toTreatmentCategoryResponse(
                treatmentCategoryRepository.findById(id).orElseThrow(
                        () -> new AppException("Cannot find treatment category with id: " + id, HttpStatus.BAD_REQUEST)
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageData<TreatmentCategoryResponse> search(SearchTreatmentCategoryRequest request) {
        QTreatmentCategory tc = QTreatmentCategory.treatmentCategory;
        BooleanBuilder where = new BooleanBuilder();

        if (StringUtils.hasText(request.getCodeKeyword())) {
            where.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", tc.code)
                            .like("%" + StringUtil.normalizeKeyword(request.getCodeKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(request.getNameKeyword())) {
            where.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", tc.name)
                            .like("%" + StringUtil.normalizeKeyword(request.getNameKeyword()) + "%")
            );
        }

        Sort sort = switch (request.getSortBy()) {
            case null -> Sort.by("createdAt").descending();
            case NAME -> Sort.by("name");
            case NAME_DESC -> Sort.by("name").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<TreatmentCategory> pages = treatmentCategoryRepository.findAll(where, pageable);

        return new PageData<>(
                treatmentCategoryMapper.toTreatmentCategoryResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()
        );
    }

    @Transactional
    @Override
    public TreatmentCategoryResponse save(CreateTreatmentCategoryRequest request) {
        TreatmentCategory entity = TreatmentCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        if (StringUtils.hasText(request.getCode())) {
            if (treatmentCategoryRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            entity.setCode(request.getCode());
        } else {
            entity.setCode(sequenceService.generateTreatmentCategoryCode());
        }

        return treatmentCategoryMapper.toTreatmentCategoryResponse(treatmentCategoryRepository.save(entity));
    }

    @Transactional
    @Override
    public TreatmentCategoryResponse update(Long id, UpdateTreatmentCategoryRequest request) {
        TreatmentCategory existed = treatmentCategoryRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find treatment category with id: " + id, HttpStatus.BAD_REQUEST));

        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(existed.getCode())) {
            if (treatmentCategoryRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            existed.setCode(request.getCode());
        }

        existed.setName(request.getName());
        existed.setDescription(request.getDescription());
        existed.setVersion(request.getVersion());

        return treatmentCategoryMapper.toTreatmentCategoryResponse(treatmentCategoryRepository.save(existed));
    }

    @Override
    public void delete(Long id) {
        if (!treatmentCategoryRepository.existsById(id)) {
            throw new AppException("Cannot find treatment category with id: " + id, HttpStatus.BAD_REQUEST);
        }
        treatmentCategoryRepository.deleteById(id);
    }
}



