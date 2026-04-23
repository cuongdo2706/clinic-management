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
import cd.beapi.utility.ExcelUtil;
import cd.beapi.utility.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

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
        if (searchMedicineRequest.getPriceFrom() != null) {
            whereClause.and(m.price.goe(searchMedicineRequest.getPriceFrom()));
        }
        if (searchMedicineRequest.getPriceTo() != null) {
            whereClause.and(m.price.loe(searchMedicineRequest.getPriceTo()));
        }
        Sort sort = switch (searchMedicineRequest.getSortBy()) {
            case null -> Sort.by("createdAt").descending();
            case NAME -> Sort.by("name");
            case NAME_DESC -> Sort.by("name").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
            case PRICE -> Sort.by("price");
            case PRICE_DESC -> Sort.by("price").descending();
        };
        Pageable pageable = PageRequest.of(searchMedicineRequest.getPage(), searchMedicineRequest.getSize(), sort);
        Page<Medicine> pages = medicineRepository.findAll(whereClause, pageable);
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
        Medicine newMedicine = Medicine.builder()
                .name(createMedicineRequest.getName())
                .unit(createMedicineRequest.getUnit())
                .description(createMedicineRequest.getDescription())
                .isActive(true)
                .price(createMedicineRequest.getPrice())
                .quantity(createMedicineRequest.getQuantity() != null ? createMedicineRequest.getQuantity() : 0)
                .manufacturer(createMedicineRequest.getManufacturer())
                .origin(createMedicineRequest.getOrigin())
                .build();
        if (StringUtils.hasText(createMedicineRequest.getCode())) {
            if (medicineRepository.existsByCode(createMedicineRequest.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            newMedicine.setCode(createMedicineRequest.getCode());
        } else {
            newMedicine.setCode(sequenceService.generateMedicineCode());
        }
        Medicine saveMedicine = medicineRepository.save(newMedicine);
        return medicineMapper.toMedicineResponse(saveMedicine);
    }

    @Transactional
    @Override
    public MedicineResponse update(Long id, UpdateMedicineRequest req) {
        Medicine existed = medicineRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find medicine with id: " + id, HttpStatus.BAD_REQUEST));

        if (StringUtils.hasText(req.getCode()) && !req.getCode().equals(existed.getCode())) {
            if (medicineRepository.existsByCode(req.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            existed.setCode(req.getCode());
        }

        existed.setName(req.getName());
        existed.setUnit(req.getUnit());
        existed.setDescription(req.getDescription());
        existed.setPrice(req.getPrice());
        if (req.getQuantity() != null) {
            existed.setQuantity(req.getQuantity());
        }
        existed.setManufacturer(req.getManufacturer());
        existed.setOrigin(req.getOrigin());
        existed.setVersion(req.getVersion());

        return medicineMapper.toMedicineResponse(medicineRepository.save(existed));
    }

    @Override
    public void delete(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new AppException("Cannot find medicine with id: " + id, HttpStatus.BAD_REQUEST);
        }
        medicineRepository.deleteById(id);
    }

    @Override
    public Resource exportExcel() {
        List<Medicine> medicines = medicineRepository.findAll(Sort.by("createdAt").descending());

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Danh sách thuốc");
            CellStyle headerStyle = ExcelUtil.createHeaderStyle(wb);
            CellStyle dataStyle = ExcelUtil.createDataStyle(wb);
            CellStyle centerStyle = ExcelUtil.createDataCenterStyle(wb);

            String[] headers = {"STT", "Mã thuốc", "Tên thuốc", "Đơn vị", "Giá bán", "Tồn kho", "Nhà sản xuất", "Xuất xứ", "Mô tả", "Trạng thái", "Ngày tạo"};
            ExcelUtil.writeHeader(sheet, headerStyle, headers);

            for (int i = 0; i < medicines.size(); i++) {
                Medicine m = medicines.get(i);
                Row row = sheet.createRow(i + 1);
                row.setHeightInPoints(18);

                Object[] values = {
                        i + 1,
                        m.getCode(),
                        m.getName(),
                        m.getUnit(),
                        m.getPrice(),
                        m.getQuantity(),
                        m.getManufacturer(),
                        m.getOrigin(),
                        m.getDescription(),
                        Boolean.TRUE.equals(m.getIsActive()) ? "Đang dùng" : "Ngừng dùng",
                        m.getCreatedAt()
                };

                for (int j = 0; j < values.length; j++) {
                    Cell cell = row.createCell(j);
                    ExcelUtil.setCellValue(cell, values[j]);
                    cell.setCellStyle((j == 0 || j == 9) ? centerStyle : dataStyle);
                }
            }

            ExcelUtil.autoSizeColumns(sheet, headers.length);
            wb.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new AppException("Cannot export medicine data to Excel", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
