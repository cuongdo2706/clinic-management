package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateProcedureRequest;
import cd.beapi.dto.request.SearchProcedureRequest;
import cd.beapi.dto.request.UpdateProcedureRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.ProcedureResponse;
import cd.beapi.entity.QProcedure;
import cd.beapi.entity.Procedure;
import cd.beapi.entity.ProcedureCategory;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.ProcedureMapper;
import cd.beapi.repository.jpa.ProcedureCategoryRepository;
import cd.beapi.repository.jpa.ProcedureRepository;
import cd.beapi.service.SequenceService;
import cd.beapi.service.ProcedureService;
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
public class ProcedureServiceImpl implements ProcedureService {
    private static final int DEFAULT_DURATION_MINUTES = 30;

    private final ProcedureRepository procedureRepository;
    private final ProcedureCategoryRepository procedureCategoryRepository;
    private final ProcedureMapper procedureMapper;
    private final SequenceService sequenceService;

    @Transactional(readOnly = true)
    @Override
    public ProcedureResponse findById(Long id) {
        return procedureMapper.toProcedureResponse(
                procedureRepository.findById(id).orElseThrow(
                        () -> new AppException("Cannot find procedure with id: " + id, HttpStatus.BAD_REQUEST)
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageData<ProcedureResponse> search(SearchProcedureRequest request) {
        QProcedure t = QProcedure.procedure;
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
        Page<Procedure> pages = procedureRepository.findAll(where, pageable);

        return new PageData<>(
                procedureMapper.toProcedureResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()
        );
    }

    @Transactional
    @Override
    public ProcedureResponse save(CreateProcedureRequest request) {
        Procedure entity = Procedure.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .unit(request.getUnit())
                .durationMinutes(normalizeDuration(request.getDurationMinutes()))
                .isActive(true)
                .build();

        if (StringUtils.hasText(request.getCode())) {
            if (procedureRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            entity.setCode(request.getCode());
        } else {
            entity.setCode(sequenceService.generateProcedureCode());
        }

        if (request.getProcedureCategoryId() != null) {
            ProcedureCategory category = procedureCategoryRepository.findById(request.getProcedureCategoryId())
                    .orElseThrow(() -> new AppException("Cannot find procedure category with id: " + request.getProcedureCategoryId(), HttpStatus.BAD_REQUEST));
            entity.setProcedureCategory(category);
        }

        return procedureMapper.toProcedureResponse(procedureRepository.save(entity));
    }

    @Transactional
    @Override
    public ProcedureResponse update(Long id, UpdateProcedureRequest request) {
        Procedure existed = procedureRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find procedure with id: " + id, HttpStatus.BAD_REQUEST));

        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(existed.getCode())) {
            if (procedureRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            existed.setCode(request.getCode());
        }

        existed.setName(request.getName());
        existed.setDescription(request.getDescription());
        existed.setPrice(request.getPrice());
        existed.setUnit(request.getUnit());
        existed.setDurationMinutes(normalizeDuration(request.getDurationMinutes()));

        if (request.getProcedureCategoryId() != null) {
            ProcedureCategory category = procedureCategoryRepository.findById(request.getProcedureCategoryId())
                    .orElseThrow(() -> new AppException("Cannot find procedure category with id: " + request.getProcedureCategoryId(), HttpStatus.BAD_REQUEST));
            existed.setProcedureCategory(category);
        } else {
            existed.setProcedureCategory(null);
        }

        existed.setVersion(request.getVersion());

        return procedureMapper.toProcedureResponse(procedureRepository.save(existed));
    }

    @Override
    public void delete(Long id) {
        if (!procedureRepository.existsById(id)) {
            throw new AppException("Cannot find procedure with id: " + id, HttpStatus.BAD_REQUEST);
        }
        procedureRepository.deleteById(id);
    }

    private Integer normalizeDuration(Integer durationMinutes) {
        if (durationMinutes == null) {
            return DEFAULT_DURATION_MINUTES;
        }
        if (durationMinutes <= 0 || durationMinutes % 15 != 0) {
            throw new AppException("Procedure duration must be a positive multiple of 15 minutes", HttpStatus.BAD_REQUEST);
        }
        return durationMinutes;
    }
}


