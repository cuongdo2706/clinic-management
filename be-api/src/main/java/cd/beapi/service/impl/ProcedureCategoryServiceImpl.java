package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateProcedureCategoryRequest;
import cd.beapi.dto.request.SearchProcedureCategoryRequest;
import cd.beapi.dto.request.UpdateProcedureCategoryRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.ProcedureCategoryResponse;
import cd.beapi.entity.QProcedureCategory;
import cd.beapi.entity.ProcedureCategory;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.ProcedureCategoryMapper;
import cd.beapi.repository.jpa.ProcedureCategoryRepository;
import cd.beapi.service.SequenceService;
import cd.beapi.service.ProcedureCategoryService;
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
public class ProcedureCategoryServiceImpl implements ProcedureCategoryService {

    private final ProcedureCategoryRepository procedureCategoryRepository;
    private final ProcedureCategoryMapper procedureCategoryMapper;
    private final SequenceService sequenceService;

    @Transactional(readOnly = true)
    @Override
    public ProcedureCategoryResponse findById(Long id) {
        return procedureCategoryMapper.toProcedureCategoryResponse(
                procedureCategoryRepository.findById(id).orElseThrow(
                        () -> new AppException("Cannot find procedure category with id: " + id, HttpStatus.BAD_REQUEST)
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageData<ProcedureCategoryResponse> search(SearchProcedureCategoryRequest request) {
        QProcedureCategory tc = QProcedureCategory.procedureCategory;
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
        Page<ProcedureCategory> pages = procedureCategoryRepository.findAll(where, pageable);

        return new PageData<>(
                procedureCategoryMapper.toProcedureCategoryResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()
        );
    }

    @Transactional
    @Override
    public ProcedureCategoryResponse save(CreateProcedureCategoryRequest request) {
        ProcedureCategory entity = ProcedureCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        if (StringUtils.hasText(request.getCode())) {
            if (procedureCategoryRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            entity.setCode(request.getCode());
        } else {
            entity.setCode(sequenceService.generateProcedureCategoryCode());
        }

        return procedureCategoryMapper.toProcedureCategoryResponse(procedureCategoryRepository.save(entity));
    }

    @Transactional
    @Override
    public ProcedureCategoryResponse update(Long id, UpdateProcedureCategoryRequest request) {
        ProcedureCategory existed = procedureCategoryRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find procedure category with id: " + id, HttpStatus.BAD_REQUEST));

        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(existed.getCode())) {
            if (procedureCategoryRepository.existsByCode(request.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            existed.setCode(request.getCode());
        }

        existed.setName(request.getName());
        existed.setDescription(request.getDescription());
        existed.setVersion(request.getVersion());

        return procedureCategoryMapper.toProcedureCategoryResponse(procedureCategoryRepository.save(existed));
    }

    @Override
    public void delete(Long id) {
        if (!procedureCategoryRepository.existsById(id)) {
            throw new AppException("Cannot find procedure category with id: " + id, HttpStatus.BAD_REQUEST);
        }
        procedureCategoryRepository.deleteById(id);
    }
}



