package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateTreatmentRequest;
import cd.beapi.dto.request.SearchTreatmentRequest;
import cd.beapi.dto.request.UpdateTreatmentRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.TreatmentResponse;
import cd.beapi.entity.QTreatment;
import cd.beapi.entity.Treatment;
import cd.beapi.entity.TreatmentCategory;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.TreatmentMapper;
import cd.beapi.repository.jpa.TreatmentCategoryRepository;
import cd.beapi.repository.jpa.TreatmentRepository;
import cd.beapi.service.SequenceService;
import cd.beapi.service.TreatmentService;
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
public class TreatmentServiceImpl implements TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final TreatmentCategoryRepository treatmentCategoryRepository;
    private final TreatmentMapper treatmentMapper;
    private final SequenceService sequenceService;

    @Transactional(readOnly = true)
    @Override
    public TreatmentResponse findById(Long id) {
        return treatmentMapper.toTreatmentResponse(
                treatmentRepository.findById(id).orElseThrow(
                        () -> new AppException("Cannot find treatment with id: " + id, HttpStatus.BAD_REQUEST)
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageData<TreatmentResponse> search(SearchTreatmentRequest request) {
        QTreatment t = QTreatment.treatment;
        BooleanBuilder where = new BooleanBuilder();

        if (StringUtils.hasText(request.getCodeKeyword())) {
            where.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", t.code)
                            .like("%" + StringUtil.normalizeKeyword(request.getCodeKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(request.getNameKeyword())) {
            where.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", t.name)
                            .like("%" + StringUtil.normalizeKeyword(request.getNameKeyword()) + "%")
            );
        }
        if (request.getPriceFrom() != null) {
            where.and(t.price.goe(request.getPriceFrom()));
        }
        if (request.getPriceTo() != null) {
            where.and(t.price.loe(request.getPriceTo()));
        }

        Sort sort = switch (request.getSortBy()) {
            case null -> Sort.by("createdAt").descending();
            case NAME -> Sort.by("name");
            case NAME_DESC -> Sort.by("name").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
            case PRICE -> Sort.by("price");
            case PRICE_DESC -> Sort.by("price").descending();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Treatment> pages = treatmentRepository.findAll(where, pageable);

        return new PageData<>(
                treatmentMapper.toTreatmentResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()
        );
    }

    @Transactional
    @Override
    public TreatmentResponse save(CreateTreatmentRequest request) {
        Treatment entity = Treatment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .unit(request.getUnit())
                .isActive(true)
                .build();

        if (StringUtils.hasText(request.getCode())) {
            if (treatmentRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            entity.setCode(request.getCode());
        } else {
            entity.setCode(sequenceService.generateTreatmentCode());
        }

        if (request.getTreatmentCategoryId() != null) {
            TreatmentCategory category = treatmentCategoryRepository.findById(request.getTreatmentCategoryId())
                    .orElseThrow(() -> new AppException("Cannot find treatment category with id: " + request.getTreatmentCategoryId(), HttpStatus.BAD_REQUEST));
            entity.setTreatmentCategory(category);
        }

        return treatmentMapper.toTreatmentResponse(treatmentRepository.save(entity));
    }

    @Transactional
    @Override
    public TreatmentResponse update(Long id, UpdateTreatmentRequest request) {
        Treatment existed = treatmentRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find treatment with id: " + id, HttpStatus.BAD_REQUEST));

        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(existed.getCode())) {
            if (treatmentRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            existed.setCode(request.getCode());
        }

        existed.setName(request.getName());
        existed.setDescription(request.getDescription());
        existed.setPrice(request.getPrice());
        existed.setUnit(request.getUnit());

        if (request.getTreatmentCategoryId() != null) {
            TreatmentCategory category = treatmentCategoryRepository.findById(request.getTreatmentCategoryId())
                    .orElseThrow(() -> new AppException("Cannot find treatment category with id: " + request.getTreatmentCategoryId(), HttpStatus.BAD_REQUEST));
            existed.setTreatmentCategory(category);
        } else {
            existed.setTreatmentCategory(null);
        }

        existed.setVersion(request.getVersion());

        return treatmentMapper.toTreatmentResponse(treatmentRepository.save(existed));
    }

    @Override
    public void delete(Long id) {
        if (!treatmentRepository.existsById(id)) {
            throw new AppException("Cannot find treatment with id: " + id, HttpStatus.BAD_REQUEST);
        }
        treatmentRepository.deleteById(id);
    }
}


